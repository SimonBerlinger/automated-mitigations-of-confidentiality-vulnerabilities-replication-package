package de.uulm.masterthesis.eval;

import java.io.IOException;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;

import de.uulm.masterthesis.*;

public class EvalRunner {

	static AnalysisConstraint analysisConstraint = new ConstraintDSL().ofData().withLabel("Sensitivity", "Personal")
			.withoutLabel("Encryption", "Encrypted").neverFlows().toVertex().withCharacteristic("Location", "NonEU")
			.create();

	static AnalysisConstraint analysisConstraint2 = new ConstraintDSL().ofData().withLabel("Sensitivity", "Personal")
			.neverFlows().toVertex().withCharacteristic("Location", "NonEU")
			.withoutCharacteristic("Verification", "verified").create();

	public static void main(String[] args) {

		runAccuracyPipelineBehaviour();
		runAccuracyPipelineComponents();
		runAccuracyPipelineDataflow1();
		runAccuracyPipelineDataflow2();
		runAccuracyPipelineDataflow3();
		runAccuracyPipelineDataflow4();
		runAccuracyPipelineDataflow5();
		runAccuracyPipelineDataflow6();
		runAccuracyPipelineDataflowBehaviour1();
		runAccuracyPipelineDataflowBehaviour2();
		runAccuracyPipelineDataflowBehaviour3();
		runAccuracyPipelineDataflowBehaviour4();
		runAccuracyPipelineStaticLabels();
		runAccuracySystemAdaptationI1();
		runAccuracySystemAdaptationI2();
		runAccuracySystemAdaptationI3();
		runAccuracySystemAdaptationI4();
		runAccuracySystemAdaptationM1N2();
		runAccuracySystemAdaptationM2();
		runAccuracySystemAdaptationM3();
		runAccuracySystemAdaptationM4();
		runAccuracySystemAdaptationM5();
		runAccuracySystemAdaptationM6();
		runAccuracySystemAdaptationM7();
		runAccuracySystemAdaptationN1U1();
		runAccuracySystemAdaptationU2();
		runAccuracySystemAdaptationU3();
		runAccuracySystemAdaptationU4();
		runAccuracySystemAdaptationU5();

//		runScalabilityPipelineComponents(100);
//		runScalabilityPipelineRecords(100);
//		runScalabilityPipelineDataflows(100);

//		runScalabilitySystemComponents(100);
//		 runScalabilitySystemMitigations(100);
//		 runScalabilitySystemMitigationsUnique(100);

	}

	public static void runAccuracyPipelineBehaviour() {

		try {
			new TransformationPipeline().pipelineRun("../systems/accuracy/transformation_pipeline/behaviour/pcm",
					"../systems/accuracy/transformation_pipeline/behaviour",
					"../systems/accuracy/transformation_pipeline/behaviour/kieker-logs/kieker_current", "dfd",
					analysisConstraint);
			System.out.print("runAccuracyPipelineBehaviour executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracyPipelineComponents() {

		try {
			new TransformationPipeline().pipelineRun("../systems/accuracy/transformation_pipeline/components/pcm",
					"../systems/accuracy/transformation_pipeline/components",
					"../systems/accuracy/transformation_pipeline/components/kieker-logs/kieker_current", "dfd",
					analysisConstraint);
			System.out.print("runAccuracyPipelineComponents executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracyPipelineDataflow1() {

		try {
			new TransformationPipeline().pipelineRun("../systems/accuracy/transformation_pipeline/dataflow/case_1/pcm",
					"../systems/accuracy/transformation_pipeline/dataflow/case_1",
					"../systems/accuracy/transformation_pipeline/dataflow/case_1/kieker-logs/kieker_current", "dfd",
					analysisConstraint);
			System.out.print("runAccuracyPipelineDataflow1 executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracyPipelineDataflow2() {

		try {
			new TransformationPipeline().pipelineRun("../systems/accuracy/transformation_pipeline/dataflow/case_2/pcm",
					"../systems/accuracy/transformation_pipeline/dataflow/case_2",
					"../systems/accuracy/transformation_pipeline/dataflow/case_2/kieker-logs/kieker_current", "dfd",
					analysisConstraint);
			System.out.print("runAccuracyPipelineDataflow2 executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracyPipelineDataflow3() {

		try {
			new TransformationPipeline().pipelineRun("../systems/accuracy/transformation_pipeline/dataflow/case_3/pcm",
					"../systems/accuracy/transformation_pipeline/dataflow/case_3",
					"../systems/accuracy/transformation_pipeline/dataflow/case_3/kieker-logs/kieker_current", "dfd",
					analysisConstraint);
			System.out.print("runAccuracyPipelineDataflow3 executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracyPipelineDataflow4() {

		try {
			new TransformationPipeline().pipelineRun("../systems/accuracy/transformation_pipeline/dataflow/case_4/pcm",
					"../systems/accuracy/transformation_pipeline/dataflow/case_4",
					"../systems/accuracy/transformation_pipeline/dataflow/case_4/kieker-logs/kieker_current", "dfd",
					analysisConstraint);
			System.out.print("runAccuracyPipelineDataflow4 executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracyPipelineDataflow5() {

		try {
			new TransformationPipeline().pipelineRun("../systems/accuracy/transformation_pipeline/dataflow/case_5/pcm",
					"../systems/accuracy/transformation_pipeline/dataflow/case_5",
					"../systems/accuracy/transformation_pipeline/dataflow/case_5/kieker-logs/kieker_current", "dfd",
					analysisConstraint);
			System.out.print("runAccuracyPipelineDataflow5 executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracyPipelineDataflow6() {

		try {
			new TransformationPipeline().pipelineRun("../systems/accuracy/transformation_pipeline/dataflow/case_6/pcm",
					"../systems/accuracy/transformation_pipeline/dataflow/case_6",
					"../systems/accuracy/transformation_pipeline/dataflow/case_6/kieker-logs/kieker_current", "dfd",
					analysisConstraint);
			System.out.print("runAccuracyPipelineDataflow6 executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracyPipelineDataflowBehaviour1() {

		try {
			new TransformationPipeline().pipelineRun("../systems/accuracy/transformation_pipeline/dataflow_and_behaviour/case_1/pcm",
					"../systems/accuracy/transformation_pipeline/dataflow_and_behaviour/case_1",
					"../systems/accuracy/transformation_pipeline/dataflow_and_behaviour/case_1/kieker-logs/kieker_current",
					"dfd", analysisConstraint);
			System.out.print("runAccuracyPipelineDataflowBehaviour1 executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracyPipelineDataflowBehaviour2() {

		try {
			new TransformationPipeline().pipelineRun("../systems/accuracy/transformation_pipeline/dataflow_and_behaviour/case_2/pcm",
					"../systems/accuracy/transformation_pipeline/dataflow_and_behaviour/case_2",
					"../systems/accuracy/transformation_pipeline/dataflow_and_behaviour/case_2/kieker-logs/kieker_current",
					"dfd", analysisConstraint);
			System.out.print("runAccuracyPipelineDataflowBehaviour2 executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracyPipelineDataflowBehaviour3() {

		try {
			new TransformationPipeline().pipelineRun("../systems/accuracy/transformation_pipeline/dataflow_and_behaviour/case_3/pcm",
					"../systems/accuracy/transformation_pipeline/dataflow_and_behaviour/case_3",
					"../systems/accuracy/transformation_pipeline/dataflow_and_behaviour/case_3/kieker-logs/kieker_current",
					"dfd", analysisConstraint);
			System.out.print("runAccuracyPipelineDataflowBehaviour3 executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracyPipelineDataflowBehaviour4() {

		try {
			new TransformationPipeline().pipelineRun("../systems/accuracy/transformation_pipeline/dataflow_and_behaviour/case_4/pcm",
					"../systems/accuracy/transformation_pipeline/dataflow_and_behaviour/case_4",
					"../systems/accuracy/transformation_pipeline/dataflow_and_behaviour/case_4/kieker-logs/kieker_current",
					"dfd", analysisConstraint);
			System.out.print("runAccuracyPipelineDataflowBehaviour4 executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracyPipelineStaticLabels() {

		try {
			new TransformationPipeline().pipelineRun("../systems/accuracy/transformation_pipeline/static_labels/pcm",
					"../systems/accuracy/transformation_pipeline/static_labels",
					"../systems/accuracy/transformation_pipeline/static_labels/kieker-logs/kieker_current", "dfd",
					analysisConstraint);
			System.out.print("runAccuracyPipelineStaticLabels executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracySystemAdaptationI1() {

		try {
			String pcmDirectory = "../systems/accuracy/system_adaptations/case_I1/pcm";
			TransformationPipeline pipeline = new TransformationPipeline();
			pipeline.resetRepository(pcmDirectory);
			pipeline.pipelineRun(pcmDirectory,
					"../systems/accuracy/system_adaptations/case_I1",
					"../systems/accuracy/system_adaptations/case_I1/kieker-logs/kieker_current", "dfd",
					analysisConstraint);
			System.out.print("runAccuracySystemAdaptationI1 executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracySystemAdaptationI2() {

		try {
			String pcmDirectory = "../systems/accuracy/system_adaptations/case_I2/pcm";
			TransformationPipeline pipeline = new TransformationPipeline();
			pipeline.resetRepository(pcmDirectory);
			pipeline.pipelineRun(pcmDirectory,
					"../systems/accuracy/system_adaptations/case_I2",
					"../systems/accuracy/system_adaptations/case_I2/kieker-logs/kieker_current", "dfd",
					analysisConstraint);
			System.out.print("runAccuracySystemAdaptationI2 executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracySystemAdaptationI3() {

		try {
			String pcmDirectory = "../systems/accuracy/system_adaptations/case_I3/pcm";
			TransformationPipeline pipeline = new TransformationPipeline();
			pipeline.resetRepository(pcmDirectory);
			pipeline.pipelineRun(pcmDirectory,
					"../systems/accuracy/system_adaptations/case_I3",
					"../systems/accuracy/system_adaptations/case_I3/kieker-logs/kieker_current", "dfd",
					analysisConstraint);
			System.out.print("runAccuracySystemAdaptationI3 executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracySystemAdaptationI4() {

		try {
			String pcmDirectory = "../systems/accuracy/system_adaptations/case_I4/pcm";
			TransformationPipeline pipeline = new TransformationPipeline();
			pipeline.resetRepository(pcmDirectory);
			pipeline.pipelineRun(pcmDirectory,
					"../systems/accuracy/system_adaptations/case_I4",
					"../systems/accuracy/system_adaptations/case_I4/kieker-logs/kieker_current", "dfd",
					analysisConstraint);
			System.out.print("runAccuracySystemAdaptationI4 executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracySystemAdaptationM1N2() {

		try {
			String pcmDirectory = "../systems/accuracy/system_adaptations/case_M1_N2/pcm";
			TransformationPipeline pipeline = new TransformationPipeline();
			pipeline.resetRepository(pcmDirectory);
			pipeline.pipelineRun(pcmDirectory,
					"../systems/accuracy/system_adaptations/case_M1_N2",
					"../systems/accuracy/system_adaptations/case_M1_N2/kieker-logs/kieker_current", "dfd",
					analysisConstraint);
			System.out.print("runAccuracySystemAdaptationM1N2 executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracySystemAdaptationM2() {

		try {
			String pcmDirectory = "../systems/accuracy/system_adaptations/case_M2/pcm";
			TransformationPipeline pipeline = new TransformationPipeline();
			pipeline.resetRepository(pcmDirectory);
			pipeline.pipelineRun(pcmDirectory,
					"../systems/accuracy/system_adaptations/case_M2",
					"../systems/accuracy/system_adaptations/case_M2/kieker-logs/kieker_current", "dfd",
					analysisConstraint);
			System.out.print("runAccuracySystemAdaptationM2 executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracySystemAdaptationM3() {

		try {
			String pcmDirectory = "../systems/accuracy/system_adaptations/case_M3/pcm";
			TransformationPipeline pipeline = new TransformationPipeline();
			pipeline.resetRepository(pcmDirectory);
			pipeline.pipelineRun(pcmDirectory,
					"../systems/accuracy/system_adaptations/case_M3",
					"../systems/accuracy/system_adaptations/case_M3/kieker-logs/kieker_current", "dfd",
					analysisConstraint);
			System.out.print("runAccuracySystemAdaptationM3 executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracySystemAdaptationM4() {

		try {
			String pcmDirectory = "../systems/accuracy/system_adaptations/case_M4/pcm";
			TransformationPipeline pipeline = new TransformationPipeline();
			pipeline.resetRepository(pcmDirectory);
			pipeline.pipelineRun(pcmDirectory,
					"../systems/accuracy/system_adaptations/case_M4",
					"../systems/accuracy/system_adaptations/case_M4/kieker-logs/kieker_current", "dfd",
					analysisConstraint);
			System.out.print("runAccuracySystemAdaptationM4 executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracySystemAdaptationM5() {

		try {
			String pcmDirectory = "../systems/accuracy/system_adaptations/case_M5/pcm";
			TransformationPipeline pipeline = new TransformationPipeline();
			pipeline.resetRepository(pcmDirectory);
			pipeline.pipelineRun(pcmDirectory,
					"../systems/accuracy/system_adaptations/case_M5",
					"../systems/accuracy/system_adaptations/case_M5/kieker-logs/kieker_current", "dfd",
					analysisConstraint);
			System.out.print("runAccuracySystemAdaptationM5 executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracySystemAdaptationM6() {

		try {
			String pcmDirectory = "../systems/accuracy/system_adaptations/case_M6/pcm";
			TransformationPipeline pipeline = new TransformationPipeline();
			pipeline.resetRepository(pcmDirectory);
			pipeline.pipelineRun(pcmDirectory,
					"../systems/accuracy/system_adaptations/case_M6",
					"../systems/accuracy/system_adaptations/case_M6/kieker-logs/kieker_current", "dfd",
					analysisConstraint);
			System.out.print("runAccuracySystemAdaptationM6 executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracySystemAdaptationM7() {

		try {
			String pcmDirectory = "../systems/accuracy/system_adaptations/case_M7/pcm";
			TransformationPipeline pipeline = new TransformationPipeline();
			pipeline.resetRepository(pcmDirectory);
			pipeline.pipelineRun(pcmDirectory,
					"../systems/accuracy/system_adaptations/case_M7",
					"../systems/accuracy/system_adaptations/case_M7/kieker-logs/kieker_current", "dfd",
					analysisConstraint);
			System.out.print("runAccuracySystemAdaptationM7 executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracySystemAdaptationN1U1() {

		try {
			String pcmDirectory = "../systems/accuracy/system_adaptations/case_N1_U1/pcm";
			TransformationPipeline pipeline = new TransformationPipeline();
			pipeline.resetRepository(pcmDirectory);
			pipeline.pipelineRun(pcmDirectory,
					"../systems/accuracy/system_adaptations/case_N1_U1",
					"../systems/accuracy/system_adaptations/case_N1_U1/kieker-logs/kieker-20261026-182908-29666425987484-UTC--KIEKER-SINGLETON",
					"dfd", analysisConstraint);
			System.out.print("runAccuracySystemAdaptationN1U1 executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracySystemAdaptationU2() {

		try {
			String pcmDirectory = "../systems/accuracy/system_adaptations/case_U2/pcm";
			TransformationPipeline pipeline = new TransformationPipeline();
			pipeline.resetRepository(pcmDirectory);
			pipeline.pipelineRun(pcmDirectory,
					"../systems/accuracy/system_adaptations/case_U2",
					"../systems/accuracy/system_adaptations/case_U2/kieker-logs/kieker-20261026-150005-17122934059866-UTC--KIEKER-SINGLETON",
					"dfd", analysisConstraint);
			System.out.print("runAccuracySystemAdaptationU2 executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracySystemAdaptationU3() {

		try {
			String pcmDirectory = "../systems/accuracy/system_adaptations/case_U3/pcm";
			TransformationPipeline pipeline = new TransformationPipeline();
			pipeline.resetRepository(pcmDirectory);
			pipeline.pipelineRun(pcmDirectory,
					"../systems/accuracy/system_adaptations/case_U3",
					"../systems/accuracy/system_adaptations/case_U3/kieker-logs/kieker_current", "dfd",
					analysisConstraint);
			System.out.print("runAccuracySystemAdaptationU3 executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracySystemAdaptationU4() {

		try {
			String pcmDirectory = "../systems/accuracy/system_adaptations/case_U4/pcm";
			TransformationPipeline pipeline = new TransformationPipeline();
			pipeline.resetRepository(pcmDirectory);
			pipeline.pipelineRun(pcmDirectory,
					"../systems/accuracy/system_adaptations/case_U4",
					"../systems/accuracy/system_adaptations/case_U4/kieker-logs/kieker_current", "dfd",
					analysisConstraint);
			System.out.print("runAccuracySystemAdaptationU4 executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runAccuracySystemAdaptationU5() {

		try {
			String pcmDirectory = "../systems/accuracy/system_adaptations/case_U5/pcm";
			TransformationPipeline pipeline = new TransformationPipeline();
			pipeline.resetRepository(pcmDirectory);
			pipeline.pipelineRun(pcmDirectory,
					"../systems/accuracy/system_adaptations/case_U5",
					"../systems/accuracy/system_adaptations/case_U5/kieker-logs/kieker_current", "dfd",
					analysisConstraint);
			System.out.print("runAccuracySystemAdaptationU5 executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runScalabilityPipelineComponents(int repetitions) {

		int[] counts = { 0, 15, 30, 150, 300, 600, 900, 1200 };
		try {
			for (int count : counts) {
				String base = "../systems/scalability/components/Scalability_Components_" + count + "_Additional";
				new TransformationPipeline().pipelineRunTimed(base + "/pcm", base, base + "/kieker-logs/kieker_current",
						"dfd", analysisConstraint, "scalability-pipeline-components-" + count, repetitions, 0);
			}
			System.out.print("runScalabilityPipelineComponents executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runScalabilityPipelineRecords(int repetitions) {

		int[] counts = { 1, 10, 50, 100, 500, 1000, 5000, 10000, 25000, 50000 };
		try {
			for (int count : counts) {
				String suffix = count == 1 ? "run" : "runs";
				new TransformationPipeline().pipelineRunTimed("../systems/scalability/records/pcm",
						"../systems/scalability/records",
						"../systems/scalability/records/kieker-logs-archive/kieker_" + count + "_" + suffix, "dfd",
						analysisConstraint, "scalability-pipeline-records-" + count, repetitions, 0);
			}
			System.out.print("runScalabilityPipelineRecords executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runScalabilityPipelineDataflows(int repetitions) {

		int[] counts = { 0, 200, 400, 800, 1600, 3200, 6400, 12800, 25600, 51200 };
		try {
			for (int count : counts) {
				String base = "../systems/scalability/dataflows/Scalability_Dataflows_" + count;
				new TransformationPipeline().pipelineRunTimed(base + "/pcm", base, base + "/kieker-logs/kieker_current",
						"dfd", analysisConstraint, "scalability-pipeline-dataflows-" + count, repetitions, 0);
			}
			System.out.print("runScalabilityPipelineDataflows executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runScalabilitySystemComponents(int repetitions) {

		int[] counts = { 0, 15, 30, 150, 300, 600, 900, 1200 };
		try {
			TransformationPipeline pipeline = new TransformationPipeline();
			for (int count : counts) {
				String base = "../systems/scalability/components/Scalability_Components_" + count + "_Additional";
				String pcmDirectory = base + "/pcm";
				
				pipeline.pipelineRunTimed(pcmDirectory, base, base + "/kieker-logs/kieker_current", "dfd",
						analysisConstraint, "scalability-system-components-" + count, 1, repetitions);
			}
			System.out.print("runScalabilitySystemComponents executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runScalabilitySystemMitigations(int repetitions) {

		int[] counts = { 1, 15, 30, 150, 300, 600, 900, 1200 };
		try {
			TransformationPipeline pipeline = new TransformationPipeline();
			for (int count : counts) {
				String base = "../systems/scalability/mitigations/Scalability_Mitigations_" + count;
				String pcmDirectory = base + "/pcm";

				
				pipeline.pipelineRunTimed(pcmDirectory, base, base + "/kieker-logs/kieker_current", "dfd",
						analysisConstraint, "scalability-system-mitigations-" + count, 1, repetitions);

			}
			System.out.print("runScalabilitySystemMitigations executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runScalabilitySystemMitigationsUnique(int repetitions) {

		int[] counts = { 1, 15, 30, 150, 300, 600, 900, 12000 };
		try {
			TransformationPipeline pipeline = new TransformationPipeline();
			for (int count : counts) {
				String base = "../systems/scalability/mitigations_unique_types/Scalability_Mitigations_Vulnerabilities_Unique_"
						+ count;
				String pcmDirectory = base + "/pcm";
				
				pipeline.pipelineRunTimed(pcmDirectory, base, base + "/kieker-logs/kieker_current", "dfd",
						analysisConstraint, "scalability-system-mitigations-unique-" + count, 1, repetitions);
			}
			System.out.print("runScalabilitySystemMitigationsUnique executed\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
