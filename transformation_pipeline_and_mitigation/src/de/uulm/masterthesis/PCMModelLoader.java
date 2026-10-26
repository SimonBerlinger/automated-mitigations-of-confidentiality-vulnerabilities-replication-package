package de.uulm.masterthesis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.dataflowanalysis.analysis.pcm.resource.PCMURIResourceProvider;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.AssemblyAssignee;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.Assignments;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.ResourceAssignee;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.UsageAssignee;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryComponent;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

import de.uulm.masterthesis.util.TransformationUtils;

public final class PCMModelLoader {

	private static final String NODE_CHARACTERISTICS = "My.nodecharacteristics";

	/**
	 * Find pcm files in the directory and creates the AnnotatedPCM object
	 */
	public AnnotatedPCM load(String pcmDirectory) throws IOException {
		Path pcmPath = Path.of(pcmDirectory).toAbsolutePath().normalize();
		Path nodeCharacteristicsFile = pcmPath.resolve(NODE_CHARACTERISTICS);
		if (!Files.isRegularFile(nodeCharacteristicsFile)) {
			throw new IOException("Required file not found: " + nodeCharacteristicsFile);
		}

		String usageModelPath = TransformationUtils.findPcmModelFile(pcmPath, ".usagemodel");
		String allocationPath = TransformationUtils.findPcmModelFile(pcmPath, ".allocation");
		URI nodeCharacteristicsUri = URI.createFileURI(nodeCharacteristicsFile.toString());

		PCMURIResourceProvider provider = new PCMURIResourceProvider(URI.createFileURI(usageModelPath),
				URI.createFileURI(allocationPath), nodeCharacteristicsUri);

		provider.setupResources();
		provider.loadRequiredResources();			

		Resource nodeCharacteristics = provider.getResources().stream()
				.filter(resource -> nodeCharacteristicsUri.equals(resource.getURI())).findFirst()
				.orElseThrow(() -> new IOException("Failed to load " + NODE_CHARACTERISTICS));

		UsageModel usageModel = provider.getUsageModel();
		Allocation allocation = provider.getAllocation();
		Repository repository = findRepository(provider);
		System system = getSystemFromAllocation(allocation);

		List<AssemblyContext> assemblyContexts = new ArrayList<>();
		List<BasicComponent> repositoryComponents = new ArrayList<>();
		loadComponentsFromSystem(system, assemblyContexts, repositoryComponents);

		return new AnnotatedPCM(usageModel, repository, system, allocation, assemblyContexts, repositoryComponents,
				getAssignees(nodeCharacteristics, AssemblyAssignee.class),
				getAssignees(nodeCharacteristics, ResourceAssignee.class),
				getAssignees(nodeCharacteristics, UsageAssignee.class));
	}

	private System getSystemFromAllocation(Allocation allocation) {
		return allocation.getSystem_Allocation();
	}

	private Repository findRepository(PCMURIResourceProvider provider) throws IOException {
		for (Resource resource : provider.getResources()) {
			for (EObject root : resource.getContents()) {
				if (root instanceof Repository repository) {
					return repository;
				}
			}
		}
		throw new IOException("PCM repository model not found in loaded resources");
	}

	/**
	 * Adds all components from the system in the passed List
	 */
	private void loadComponentsFromSystem(System system, List<AssemblyContext> assemblyContexts,
			List<BasicComponent> repositoryComponents) {
		for (AssemblyContext assemblyContext : system.getAssemblyContexts__ComposedStructure()) {
			RepositoryComponent repositoryComponent = assemblyContext.getEncapsulatedComponent__AssemblyContext();
			if (repositoryComponent instanceof BasicComponent basicComponent) {
				assemblyContexts.add(assemblyContext);
				repositoryComponents.add(basicComponent);
			}
		}
	}

	/**
	 * Reads all assignments of a specified type and returns them in a list
	 */
	private <T extends EObject> List<T> getAssignees(Resource nodeCharacteristics, Class<T> assigneeType) {
		if (nodeCharacteristics.getContents().isEmpty()
				|| !(nodeCharacteristics.getContents().get(0) instanceof Assignments assignments)) {
			return List.of();
		}

		return assignments.getAssignee().stream().filter(assigneeType::isInstance).map(assigneeType::cast).toList();
	}
}