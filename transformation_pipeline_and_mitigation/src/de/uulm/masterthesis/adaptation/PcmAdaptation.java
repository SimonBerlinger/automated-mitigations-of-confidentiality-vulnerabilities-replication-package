package de.uulm.masterthesis.adaptation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.dataflowanalysis.converter.dfd2web.DataFlowDiagramAndDictionary;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.CharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.ExpressionsFactory;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.True;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityFactory;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.dataflowanalysis.pcm.extension.model.confidentiality.characteristics.CharacteristicsFactory;
import org.dataflowanalysis.pcm.extension.model.confidentiality.characteristics.EnumCharacteristic;
import org.dataflowanalysis.pcm.extension.model.confidentiality.dictionary.PCMDataDictionary;
import org.dataflowanalysis.pcm.extension.model.confidentiality.expression.ExpressionFactory;
import org.dataflowanalysis.pcm.extension.model.confidentiality.expression.LhsEnumCharacteristicReference;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.AbstractAssignee;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.AssemblyAssignee;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.Assignments;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.NodeCharacteristicsFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.parameter.VariableUsage;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.Repository;

import de.uulm.masterthesis.util.TransformationUtils;
import org.palladiosimulator.pcm.seff.ServiceEffectSpecification;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.AbstractBranchTransition;
import org.palladiosimulator.pcm.seff.BranchAction;
import org.palladiosimulator.pcm.seff.ExternalCallAction;
import org.palladiosimulator.pcm.seff.LoopAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingBehaviour;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;

/**
 * Writes mitigation repairs back into the PCM. A new node property becomes an
 * assembly assignment. A new label on an out-pin becomes a constant set operation
 */
public final class PcmAdaptation {

	private PcmAdaptation() {
	}

	/**
	 * Apply the mitigation to the PCM using the original and repaired DFD
	 */
	public static void apply(DataFlowDiagramAndDictionary original, DataFlowDiagramAndDictionary repaired,
			org.palladiosimulator.pcm.system.System system, Repository repository,
			Map<String, AssemblyContext> assemblyContextsById) throws IOException {
		// create the diff if not already passed
		apply(RepairDiff.between(original, repaired), system, repository, assemblyContextsById);
	}

	/**
	 * Apply the mitigation to the PCM using the RepairDiff
	 */
	public static void apply(RepairDiff diff, org.palladiosimulator.pcm.system.System system, Repository repository,
			Map<String, AssemblyContext> assemblyContextsById) throws IOException {
		if (diff.isEmpty()) {
			System.out.println("PCM adaptation: no changes");
			return;
		}
		Set<Resource> modified = new LinkedHashSet<>();
		applyNodeProperties(diff, repository, assemblyContextsById, modified);
		applyOutgoingLabels(diff, system, repository, assemblyContextsById, modified);
		for (Resource resource : modified) {
			resource.save(Collections.emptyMap());
			System.out.println("PCM adaptation: saved " + resource.getURI());
		}
	}

	/**
	 * Adds the node labels from the diff to the respective assembly contexts
	 */
	private static void applyNodeProperties(RepairDiff diff, Repository repository,
			Map<String, AssemblyContext> assemblyContextsById, Set<Resource> modified) {
		if (diff.nodeProperties().isEmpty()) {
			return;
		}
		Assignments assignments = findAssignments(repository);
		if (assignments == null) {
			System.out.println("PCM adaptation: skipped node characteristics, model has no Assignments root");
			return;
		}
		for (NodePropertyChange change : diff.nodeProperties()) {
			AssemblyContext assemblyContext = assemblyContextsById.get(change.assemblyContextId());
			if (assemblyContext == null) {
				System.out.println("PCM adaptation: skipped " + change.assemblyName()
						+ ": no assembly context for node property " + label(change.typeName(), change.valueName()));
				continue;
			}
			DictionaryLiteral literal = findLiteral(repository, change.typeName(), change.valueName());
			if (literal == null) {
				System.out.println("PCM adaptation: skipped " + change.assemblyName()
						+ ": PCM dictionary has no literal " + label(change.typeName(), change.valueName()));
				continue;
			}
			AssemblyAssignee assignee = findOrCreateAssignee(assignments, assemblyContext);
			EnumCharacteristic characteristic = CharacteristicsFactory.eINSTANCE.createEnumCharacteristic();
			characteristic.setId(UUID.randomUUID().toString());
			characteristic.setEntityName(change.valueName());
			characteristic.setType(literal.type());
			characteristic.getValues().add(literal.literal());
			assignee.getCharacteristics().add(characteristic);
			if (assignee.eResource() != null) {
				modified.add(assignee.eResource());
			}
			System.out.println("PCM adaptation: " + change.assemblyName() + ": added assembly characteristic "
					+ label(change.typeName(), change.valueName()));
		}
	}

	/**
	 * Adds the data label changes from the diff to the PCM
	 */
	private static void applyOutgoingLabels(RepairDiff diff, org.palladiosimulator.pcm.system.System system,
			Repository repository, Map<String, AssemblyContext> assemblyContextsById, Set<Resource> modified) {
		for (OutgoingLabelChange change : diff.outgoingLabels()) {
			AssemblyContext assemblyContext = assemblyContextsById.get(change.assemblyContextId());
			if (assemblyContext == null) {
				System.out.println("PCM adaptation: skipped " + change.assemblyName() + " -> " + change.neighbourName()
						+ ": no assembly context");
				continue;
			}
			if (!(assemblyContext.getEncapsulatedComponent__AssemblyContext() instanceof BasicComponent component)) {
				System.out.println("PCM adaptation: skipped " + change.assemblyName()
						+ ": assembly does not encapsulate a basic component");
				continue;
			}
			DictionaryLiteral literal = findLiteral(repository, change.typeName(), change.valueName());
			if (literal == null) {
				System.out.println("PCM adaptation: skipped " + change.assemblyName() + " -> " + change.neighbourName()
						+ ": PCM dictionary has no literal " + label(change.typeName(), change.valueName()));
				continue;
			}
			List<ExternalCallAction> calls = externalCallsToward(system, assemblyContext, component,
					change.neighbourName());
			if (calls.isEmpty()) {
				System.out.println("PCM adaptation: skipped " + change.assemblyName() + " -> " + change.neighbourName()
						+ ": no external call for " + label(change.typeName(), change.valueName()));
				continue;
			}
			int writtenCalls = 0;
			for (ExternalCallAction call : calls) {
				if (call.getInputVariableUsages__CallAction().isEmpty()) {
					System.out
							.println("PCM adaptation: skipped " + callName(change, call) + ": no input variable usage");
					continue;
				}
				if (hasConstantSet(call, literal)) {
					System.out.println("PCM adaptation: skipped " + callName(change, call) + ": "
							+ label(change.typeName(), change.valueName()) + " already set");
					continue;
				}
				VariableUsage usage = call.getInputVariableUsages__CallAction().get(0);
				usage.getVariableCharacterisation_VariableUsage().add(constantSet(literal));
				if (usage.eResource() != null) {
					modified.add(usage.eResource());
				}
				writtenCalls++;
			}
			if (writtenCalls > 0) {
				System.out.println("PCM adaptation: " + change.assemblyName() + " -> " + change.neighbourName()
						+ ": set " + label(change.typeName(), change.valueName()) + " on " + writtenCalls
						+ " external call(s)");
			}
		}
	}

	/**
	 * True when this call already has a constant set of {@code literal}. A forward
	 * of the same literal does not count.
	 */
	private static boolean hasConstantSet(ExternalCallAction call, DictionaryLiteral literal) {
		for (VariableUsage usage : call.getInputVariableUsages__CallAction()) {
			for (VariableCharacterisation characterisation : usage.getVariableCharacterisation_VariableUsage()) {
				if (!(characterisation instanceof ConfidentialityVariableCharacterisation confidentiality)) {
					continue;
				}
				if (!(confidentiality.getLhs() instanceof LhsEnumCharacteristicReference lhs)) {
					continue;
				}
				if (literal.type().equals(lhs.getCharacteristicType()) && literal.literal().equals(lhs.getLiteral())
						&& confidentiality.getRhs() instanceof True) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Create a constant set behaviour for a specific literal
	 */
	private static ConfidentialityVariableCharacterisation constantSet(DictionaryLiteral literal) {
		LhsEnumCharacteristicReference lhs = ExpressionFactory.eINSTANCE.createLhsEnumCharacteristicReference();
		lhs.setCharacteristicType(literal.type());
		lhs.setLiteral(literal.literal());
		ConfidentialityVariableCharacterisation characterisation = ConfidentialityFactory.eINSTANCE
				.createConfidentialityVariableCharacterisation();
		characterisation.setLhs(lhs);
		characterisation.setRhs(ExpressionsFactory.eINSTANCE.createTrue());
		return characterisation;
	}

	/**
	 * Ensures, there is an assignee for adding node label assignments
	 */
	private static AssemblyAssignee findOrCreateAssignee(Assignments assignments, AssemblyContext assemblyContext) {
		for (AbstractAssignee assignee : assignments.getAssignee()) {
			if (assignee instanceof AssemblyAssignee assemblyAssignee
					&& assemblyContext.equals(assemblyAssignee.getAssemblycontext())) {
				return assemblyAssignee;
			}
		}
		AssemblyAssignee created = NodeCharacteristicsFactory.eINSTANCE.createAssemblyAssignee();
		created.setId(UUID.randomUUID().toString());
		created.setEntityName(assemblyContext.getEntityName());
		created.setAssemblycontext(assemblyContext);
		assignments.getAssignee().add(created);
		return created;
	}

	/**
	 * List all external call actions which point to the neighbor
	 */
	private static List<ExternalCallAction> externalCallsToward(org.palladiosimulator.pcm.system.System system,
			AssemblyContext callerContext, BasicComponent caller, String neighbourName) {
		List<ExternalCallAction> matches = new ArrayList<>();
		for (ServiceEffectSpecification specification : caller.getServiceEffectSpecifications__BasicComponent()) {
			if (specification instanceof ResourceDemandingSEFF seff) {
				collectCalls(seff, system, callerContext, neighbourName, matches);
			}
		}
		return matches;
	}

	private static void collectCalls(ResourceDemandingBehaviour behaviour,
			org.palladiosimulator.pcm.system.System system, AssemblyContext callerContext, String neighbourName,
			List<ExternalCallAction> matches) {
		if (behaviour == null) {
			return;
		}
		for (AbstractAction step : behaviour.getSteps_Behaviour()) {
			if (step instanceof ExternalCallAction externalCall) {
				if (TransformationUtils.resolveProvidingAssemblyNames(system, callerContext, externalCall)
						.contains(neighbourName)) {
					matches.add(externalCall);
				}
			} else if (step instanceof BranchAction branch) {
				for (AbstractBranchTransition transition : branch.getBranches_Branch()) {
					collectCalls(transition.getBranchBehaviour_BranchTransition(), system, callerContext, neighbourName,
							matches);
				}
			} else if (step instanceof LoopAction loop) {
				collectCalls(loop.getBodyBehaviour_Loop(), system, callerContext, neighbourName, matches);
			}
		}
	}

	private static DictionaryLiteral findLiteral(Repository repository, String typeName, String valueName) {
		Resource resource = repository.eResource();
		if (resource == null || resource.getResourceSet() == null) {
			return null;
		}
		for (Resource candidate : resource.getResourceSet().getResources()) {
			for (EObject root : candidate.getContents()) {
				if (!(root instanceof PCMDataDictionary dictionary)) {
					continue;
				}
				for (CharacteristicType characteristicType : dictionary.getCharacteristicTypes()) {
					if (!(characteristicType instanceof EnumCharacteristicType enumType)
							|| !typeName.equals(enumType.getName()) || enumType.getType() == null) {
						continue;
					}
					for (Literal literal : enumType.getType().getLiterals()) {
						if (valueName.equals(literal.getName())) {
							return new DictionaryLiteral(enumType, literal);
						}
					}
				}
			}
		}
		return null;
	}

	private static Assignments findAssignments(Repository repository) {
		Resource resource = repository.eResource();
		if (resource == null || resource.getResourceSet() == null) {
			return null;
		}
		for (Resource candidate : resource.getResourceSet().getResources()) {
			for (EObject root : candidate.getContents()) {
				if (root instanceof Assignments assignments) {
					return assignments;
				}
			}
		}
		return null;
	}

	private static String label(String typeName, String valueName) {
		return typeName + "." + valueName;
	}

	private static String callName(OutgoingLabelChange change, ExternalCallAction call) {
		String name = call.getEntityName() != null ? call.getEntityName() : call.getId();
		return change.assemblyName() + " call " + name + " toward " + change.neighbourName();
	}

	private record DictionaryLiteral(EnumCharacteristicType type, Literal literal) {
	}
}
