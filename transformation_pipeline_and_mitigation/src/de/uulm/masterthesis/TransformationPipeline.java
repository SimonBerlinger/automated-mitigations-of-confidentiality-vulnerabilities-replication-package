package de.uulm.masterthesis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dfd.resource.DFDModelResourceProvider;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;
import org.dataflowanalysis.converter.dfd2web.DataFlowDiagramAndDictionary;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.dataflowdiagramFactory;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.sat4j.specs.ContradictionException;

import de.uulm.masterthesis.adaptation.PcmAdaptation;
import de.uulm.masterthesis.util.FullMethodRecord;
import dev.arcovia.mitigation.sat.Mechanic;
import dev.arcovia.mitigation.sat.dsl.CNFTranslation;

public class TransformationPipeline {

	static DataFlowDiagram dfd;
	static DataDictionary dataDictionary;
	static PCMModelLoader pcmModelLoader = new PCMModelLoader();
	static NodeCreator nodeCreator = new NodeCreator();
	static LabelCreator labelCreator = new LabelCreator();
	static DataFlowCreator dataFlowCreator = new DataFlowCreator();
	static BehaviourCreator behaviourCreator = new BehaviourCreator();

	static AnalysisConstraint analysisConstraint = new ConstraintDSL().ofData().withLabel("Sensitivity", "Personal")
			.withoutLabel("Encryption", "Encrypted").neverFlows().toVertex().withCharacteristic("Location", "NonEU")
			.create();

	public static void main(String[] args) {
		System.out.println(Arrays.toString(args));
		if (args.length < 4) {
			System.err.println(
					"Usage: TransformationPipeline <pcm-directory> <output-directory> <kieker-folder> <output-base-name>");
			System.exit(1);
		}

		String pcmDirectory = args[0];
		String outputDirectory = args[1];
		String kiekerFolder = args[2];
		String outputBaseName = args[3];

		try {
			new TransformationPipeline().pipelineRun(pcmDirectory, outputDirectory, kiekerFolder, outputBaseName,
					analysisConstraint);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/*
	 * A standard run of the pipeline without time measurement
	 */
	public void pipelineRun(String pcmDirectory, String outputDirectory, String kiekerFolder, String outputBaseName,
			AnalysisConstraint constraint) throws IOException {

		AnnotatedPCM annotatedPcm = initialiseModels(pcmDirectory);

		DataFlowDiagramAndDictionary model = transformationExecution(outputDirectory, kiekerFolder, outputBaseName,
				annotatedPcm);

		performDfaAnalysis(model, analysisConstraint);

		DataFlowDiagramAndDictionary original = copyModel(model);
		performMitigations(outputBaseName, model, analysisConstraint);

		applyMitigation(pcmDirectory, outputDirectory, outputBaseName, annotatedPcm, original, model);
	}

	/**
	 * A run of the pipeline with time measurements in place, measuring both times
	 * for transformation to DFD as well as the system-level application of
	 * mitigations
	 */
	public void pipelineRunTimed(String pcmDirectory, String outputDirectory, String kiekerFolder,
			String outputBaseName, AnalysisConstraint constraint, String fileName, int pipelineRuns, int mitigationRuns)
			throws IOException {

		DataFlowDiagramAndDictionary model = null;
		AnnotatedPCM annotatedPcm = null;

		for (int i = 0; i < pipelineRuns; i++) {

			long startNanos = System.nanoTime();

			annotatedPcm = initialiseModels(pcmDirectory);

			long loadTime = Math.round((System.nanoTime() - startNanos) / 1_000_000.0);

			startNanos = System.nanoTime();

			model = transformationExecution(outputDirectory, kiekerFolder, outputBaseName, annotatedPcm);

			long duration = Math.round((System.nanoTime() - startNanos) / 1_000_000.0);
			System.out.println("Created " + dfd.getNodes().size() + " node(s) from components.");
			System.out.println("Saved model to:");
			System.out.println("  " + outputDirectory + "/" + outputBaseName + ".dataflowdiagram");
			System.out.println("  " + outputDirectory + "/" + outputBaseName + ".datadictionary");
			System.out.println("PCM Load duration: " + loadTime + " ms)");
			System.out.println("Pipeline duration: " + duration + " ms)");
			System.out.println("Total duration: " + (duration + loadTime) + " ms)");
			
			if (pipelineRuns > 1) {
				writeNumberForEval(loadTime + duration, fileName + "-pipeline");
			}
		}

		if (model == null || annotatedPcm == null || mitigationRuns == 0) {
			return;
		}

		performDfaAnalysis(model, analysisConstraint);

		DataFlowDiagramAndDictionary original = copyModel(model);
		performMitigations(outputBaseName, model, analysisConstraint);

		for (int i = 0; i < mitigationRuns; i++) {

			if (i > 0) {
				resetRepository(pcmDirectory);
				annotatedPcm = pcmModelLoader.load(pcmDirectory);
			}

			long mitigationStartTimeNanos = System.nanoTime();

			applyMitigation(pcmDirectory, outputDirectory, outputBaseName, annotatedPcm, original, model);
			long mitigationDuration = Math.round((System.nanoTime() - mitigationStartTimeNanos) / 1_000_000.0);

			System.out.println("Mitigation duration: " + (mitigationDuration) + " ms");
			writeNumberForEval(mitigationDuration, fileName + "-mitigation");
		}
	}

	private DataFlowDiagramAndDictionary transformationExecution(String outputDirectory, String kiekerFolder,
			String outputBaseName, AnnotatedPCM annotatedPcm) throws IOException {
		dfd.setId(annotatedPcm.allocation.getId());
		dataDictionary.setId(annotatedPcm.system.getId());

		dfd = nodeCreator.createNodes(dfd, dataDictionary, annotatedPcm);
		dfd = labelCreator.addLabels(dfd, dataDictionary, annotatedPcm);

		Map<Long, List<FullMethodRecord>> fullRecords = dataFlowCreator.getFullMethodRecordsForTraces(kiekerFolder);
		dfd = dataFlowCreator.createDataflows(dfd, fullRecords);

		dfd = behaviourCreator.addBehaviours(dfd, dataDictionary, annotatedPcm);

		// Create Model for saving
		DataFlowDiagramAndDictionary model = new DataFlowDiagramAndDictionary(dfd, dataDictionary);
		model.save(outputDirectory, outputBaseName);
		return model;
	}

	private AnnotatedPCM initialiseModels(String pcmDirectory) throws IOException {
		dfd = dataflowdiagramFactory.eINSTANCE.createDataFlowDiagram();
		dataDictionary = datadictionaryFactory.eINSTANCE.createDataDictionary();
		AnnotatedPCM annotatedPcm = pcmModelLoader.load(pcmDirectory);
		return annotatedPcm;
	}

	private void performDfaAnalysis(DataFlowDiagramAndDictionary model, AnalysisConstraint analysisConstraint) {
		var resourceProvider = new DFDModelResourceProvider(model.dataDictionary(), model.dataFlowDiagram());

		var analysis = new DFDDataFlowAnalysisBuilder().standalone().useCustomResourceProvider(resourceProvider)
				.build();

		analysis.initializeAnalysis();
		var flowGraphs = analysis.findFlowGraphs();
		flowGraphs.evaluate();

		List<DSLResult> violations = analysisConstraint.findViolations(flowGraphs);

		System.out.println("TFGs: " + flowGraphs.getTransposeFlowGraphs().size());
		System.out.println("Violating TFGs: " + violations.size());

		for (var violation : violations) {
			System.out.println(" " + analysisConstraint + " violated at " + violation.getMatchedVertices());
		}
	}

	private void applyMitigation(String pcmDirectory, String outputDirectory, String outputBaseName,
			AnnotatedPCM annotatedPcm, DataFlowDiagramAndDictionary model, DataFlowDiagramAndDictionary repairedModel)
			throws IOException {

		model.save(outputDirectory, outputBaseName + "-repaired");

		PcmAdaptation.apply(model, repairedModel, annotatedPcm.system, annotatedPcm.repository,
				annotatedPcm.componentsById);

	}

	private void performMitigations(String outputBaseName, DataFlowDiagramAndDictionary model,
			AnalysisConstraint analysisConstraint) {
		var translatedConstraint = new CNFTranslation(analysisConstraint);

		Mechanic mechanic = new Mechanic(model, outputBaseName + "-repaired", translatedConstraint.constructCNF());

		try {
			mechanic.repair();
		} catch (ContradictionException | IOException | org.sat4j.specs.TimeoutException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Replaces the model in {@code pcmDirectory} with the files from the sibling
	 * directory {@code originalPcm}.
	 */
	public void resetRepository(String pcmDirectory) throws IOException {
		Path pcmPath = Path.of(pcmDirectory).toAbsolutePath().normalize();
		Path original = pcmPath.getParent().resolve("originalPcm");
		if (!Files.isDirectory(original)) {
			throw new IOException("No original model directory at " + original);
		}
		try (var files = Files.list(original)) {
			for (Path file : files.filter(Files::isRegularFile).toList()) {
				Files.copy(file, pcmPath.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}

	/**
	 * Writes the time measurement into a specified file
	 */
	public void writeNumberForEval(long number, String fileName) throws IOException {
		Path file = Path.of("../eval_run_output/" + fileName + ".txt");
		Files.writeString(file, number + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
	}

	/**
	 * Copies the diagram and the dictionary and returns the copy
	 */
	private static DataFlowDiagramAndDictionary copyModel(DataFlowDiagramAndDictionary model) {
		EcoreUtil.Copier copier = new EcoreUtil.Copier();
		DataFlowDiagram diagram = (DataFlowDiagram) copier.copy(model.dataFlowDiagram());
		DataDictionary dictionary = (DataDictionary) copier.copy(model.dataDictionary());
		copier.copyReferences();
		return new DataFlowDiagramAndDictionary(diagram, dictionary);
	}

}