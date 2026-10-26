package de.uulm.masterthesis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.LabelType;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.dataflowanalysis.pcm.extension.model.confidentiality.characteristics.EnumCharacteristic;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.AssemblyAssignee;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.ResourceAssignee;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.UsageAssignee;
import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.composition.Connector;
import org.palladiosimulator.pcm.core.composition.ProvidedDelegationConnector;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.Branch;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;
import org.palladiosimulator.pcm.usagemodel.Loop;
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;

import de.uulm.masterthesis.util.TransformationUtils;

public class LabelCreator {

	/**
	 * Adds the labels specified in the nodecharacteristics of the pcm to the passed
	 * dfd and returns it
	 */
	public DataFlowDiagram addLabels(DataFlowDiagram dfd, DataDictionary dataDictionary, AnnotatedPCM annotatedPcm) {
		Map<String, Node> nodesById = TransformationUtils.indexNodesById(dfd);

		applyAssemblyLabels(dataDictionary, annotatedPcm, nodesById);
		applyResourceLabels(dataDictionary, annotatedPcm, nodesById);
		applyUsageLabels(dataDictionary, annotatedPcm, nodesById);

		return dfd;
	}

	/**
	 * Applies the labels that are directly specified for a component with an
	 * assembly assignment to a node
	 */
	private void applyAssemblyLabels(DataDictionary dataDictionary, AnnotatedPCM annotatedPcm,
			Map<String, Node> nodesById) {
		for (AssemblyAssignee assignee : annotatedPcm.assemblyCharacteristics) {
			
			AssemblyContext component = annotatedPcm.componentsById
					.get(assignee.getAssemblycontext().getId());

			if (component == null) {
				continue;
			}

			Node node = nodesById.get(component.getId());
			System.out.println(node.getEntityName());
			
			if (node != null) {
				addCharacteristicsToNode(node, dataDictionary, assignee.getCharacteristics());
			}
		}
	}

	/**
	 * Checks for all resource label assignments, which components are allocated
	 */
	private void applyResourceLabels(DataDictionary dataDictionary, AnnotatedPCM annotatedPcm,
			Map<String, Node> nodesById) {
		// for all resource assignments
		for (ResourceAssignee assignee : annotatedPcm.resourceCharacteristics) {
			// for each of the allocations
			for (AllocationContext allocationContext : annotatedPcm.allocation.getAllocationContexts_Allocation()) {
				
				// skip if the current assignee of the assignment is not contained in the
				// allocation context
				if (!assignee.getResourcecontainer()
						.equals(allocationContext.getResourceContainer_AllocationContext())) {
					continue;
				}
				
				AssemblyContext assemblyContext = allocationContext.getAssemblyContext_AllocationContext();
				// get the node for the current component
				Node node = nodesById.get(assemblyContext.getId());
							
				if (node != null) {
					addCharacteristicsToNode(node, dataDictionary, assignee.getCharacteristics());
				}
			}
		}
	}

	private void applyUsageLabels(DataDictionary dataDictionary, AnnotatedPCM annotatedPcm, Map<String, Node> nodesById) {
	    for (UsageAssignee assignee : annotatedPcm.usageCharacteristics) {
	        UsageScenario usageScenario = assignee.getUsagescenario();
	        if (usageScenario == null) {
	            continue;
	        }

	        List<EntryLevelSystemCall> calls = collectEntryLevelSystemCalls(usageScenario);

	        for (EntryLevelSystemCall call : calls) {
	            OperationProvidedRole providedRole = call.getProvidedRole_EntryLevelSystemCall();
	            if (providedRole == null) {
	                continue;
	            }

	            List<AssemblyContext> assemblyContexts = resolveProvidingAssemblyContexts(providedRole, annotatedPcm.system);

	            for (AssemblyContext assemblyContext : assemblyContexts) {
	                Node node = nodesById.get(assemblyContext.getId());
	                if (node == null) {
	                    continue;
	                }
	                addCharacteristicsToNode(node, dataDictionary, assignee.getCharacteristics());
	            }
	        }
	    }
	}

	private List<EntryLevelSystemCall> collectEntryLevelSystemCalls(UsageScenario scenario) {
	    List<EntryLevelSystemCall> result = new ArrayList<>();
	    collectEntryLevelSystemCalls(scenario.getScenarioBehaviour_UsageScenario(), result);
	    return result;
	}

	private void collectEntryLevelSystemCalls(ScenarioBehaviour behaviour, List<EntryLevelSystemCall> result) {
	    if (behaviour == null) {
	        return;
	    }
	    
	    for (AbstractUserAction action : behaviour.getActions_ScenarioBehaviour()) {
	    	
	        if (action instanceof EntryLevelSystemCall entryLevelSystemCall) {
	            result.add(entryLevelSystemCall);
	        } else if (action instanceof Loop loop) {
	            collectEntryLevelSystemCalls(loop.getBodyBehaviour_Loop(), result);
	        } else if (action instanceof Branch branch) {
	            for (var transition : branch.getBranchTransitions_Branch()) {
	                collectEntryLevelSystemCalls(transition.getBranchedBehaviour_BranchTransition(), result);
	            }
	        }
	    }
	}

	private List<AssemblyContext> resolveProvidingAssemblyContexts(OperationProvidedRole systemRole,
	        org.palladiosimulator.pcm.system.System system) {
	    List<AssemblyContext> result = new ArrayList<>();
	    
	    for (Connector connector : system.getConnectors__ComposedStructure()) {
	        if (connector instanceof ProvidedDelegationConnector delegation) {
	            OperationProvidedRole outerRole = delegation.getOuterProvidedRole_ProvidedDelegationConnector();
	            
	            if (outerRole != null && outerRole.equals(systemRole)) {
	                result.add(delegation.getAssemblyContext_ProvidedDelegationConnector());
	            }
	        }
	    }
	    return result;
	}

	/**
	 * Adds a list of characteristics to a specified node in the datadictionary
	 */
	private void addCharacteristicsToNode(Node node, DataDictionary dataDictionary,
	        List<EnumCharacteristic> characteristics) {
	    for (EnumCharacteristic characteristic : characteristics) {
	        if (characteristic.getType() == null) {
	            continue;
	        }
	        String typeName = characteristic.getType().getName();
	        for (Literal literal : characteristic.getValues()) {
	            LabelType labelType = TransformationUtils.getOrCreateLabelType(dataDictionary, typeName,
	                    characteristic.getType().getId());
	            Label label = TransformationUtils.getOrCreateLabel(labelType, literal.getName(), literal.getId());
	            if (!node.getProperties().contains(label)) {
	                node.getProperties().add(label);
	            }
	        }
	    }
	}

}