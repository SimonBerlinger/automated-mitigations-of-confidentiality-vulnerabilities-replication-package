package de.uulm.masterthesis;

import java.util.Map;

import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.parameter.VariableUsage;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.seff.ExternalCallAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.ServiceEffectSpecification;
import org.palladiosimulator.pcm.seff.SetVariableAction;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;

import de.uulm.masterthesis.util.PinDirection;
import de.uulm.masterthesis.util.TransformationUtils;

public class BehaviourCreator {

	/**
	 * Adds the behaviours to the dfd using the annotations in the pcm
	 */
	public DataFlowDiagram addBehaviours(DataFlowDiagram dfd, DataDictionary dataDictionary,
	        AnnotatedPCM annotatedPcm) {
	    Map<String, Node> nodesById = TransformationUtils.indexNodesById(dfd);

	    for (int i = 0; i < annotatedPcm.repositoryComponents.size(); i++) {
	        BasicComponent component = annotatedPcm.repositoryComponents.get(i);
	        AssemblyContext assemblyContext = annotatedPcm.assemblyContexts.get(i);

	        Node node = nodesById.get(assemblyContext.getId());
	        if (node == null || node.getBehavior() == null) {
	            continue;
	        }

	        ComponentBehaviourBuilder builder = new ComponentBehaviourBuilder(node.getBehavior(), dataDictionary,
	                assemblyContext.getId());

	        collectUsageModelCharacteristics(annotatedPcm, component, builder);
	        collectSeffCharacteristics(annotatedPcm, assemblyContext, component, builder);
	        builder.installAssignments(assemblyContext.getEntityName());
	    }

	    return dfd;
	}

	/**
	 * Checks all specifications made in the usage model and adds the specified
	 * behaviour to the dfd nodes
	 */
	private void collectUsageModelCharacteristics(AnnotatedPCM annotatedPcm, BasicComponent component,
			ComponentBehaviourBuilder builder) {
		UsageModel usageModel = annotatedPcm.usageModel;
		org.palladiosimulator.pcm.system.System system = annotatedPcm.system;
		if (usageModel == null || system == null) {
			return;
		}

		for (UsageScenario usageScenario : usageModel.getUsageScenario_UsageModel()) {
			ScenarioBehaviour scenarioBehaviour = usageScenario.getScenarioBehaviour_UsageScenario();
			if (scenarioBehaviour == null) {
				continue;
			}

			for (AbstractUserAction action : scenarioBehaviour.getActions_ScenarioBehaviour()) {
				if (!(action instanceof EntryLevelSystemCall entryCall)) {
					continue;
				}

				String targetComponentId = TransformationUtils.resolveRepositoryComponentIdForEntryCall(system,
						entryCall);
				if (!component.getId().equals(targetComponentId)) {
					continue;
				}

				for (VariableUsage usage : entryCall.getInputParameterUsages_EntryLevelSystemCall()) {
					builder.processUsageEntryCharacterisation(usage);
				}
				for (VariableUsage usage : entryCall.getOutputParameterUsages_EntryLevelSystemCall()) {
					builder.processVariableUsage(usage, PinDirection.INPUT, "User");
				}
			}
		}
	}

	/**
	 * Checks all specifications made in the SEFF and adds the specified behaviour
	 * to the dfd nodes. Pins are keyed by the providing assembly entity name, once
	 * per assembly connector of the external calls required role.
	 */
	private void collectSeffCharacteristics(AnnotatedPCM annotatedPcm, AssemblyContext assemblyContext,
			BasicComponent component, ComponentBehaviourBuilder builder) {
		for (ServiceEffectSpecification specification : component.getServiceEffectSpecifications__BasicComponent()) {
			if (!(specification instanceof ResourceDemandingSEFF seff)) {
				continue;
			}

			for (org.palladiosimulator.pcm.seff.AbstractAction step : seff.getSteps_Behaviour()) {
				if (step instanceof ExternalCallAction externalCall) {
					for (String calleeName : TransformationUtils.resolveProvidingAssemblyNames(annotatedPcm.system,
							assemblyContext, externalCall)) {
						 
						for (VariableUsage usage : externalCall.getInputVariableUsages__CallAction()) {
							builder.processVariableUsage(usage, PinDirection.OUTPUT, calleeName);
						}
						
						for (VariableUsage usage : externalCall.getReturnVariableUsage__CallReturnAction()) {
							builder.processVariableUsage(usage, PinDirection.INPUT, calleeName);
						}
					}
				} else if (step instanceof SetVariableAction setVariableAction) {
					// No neighbour: apply to every existing out-pin
					for (VariableUsage usage : setVariableAction.getLocalVariableUsages_SetVariableAction()) {
						builder.processUsageEntryCharacterisation(usage);
					}
				}
			}
		}
	}
	
}
