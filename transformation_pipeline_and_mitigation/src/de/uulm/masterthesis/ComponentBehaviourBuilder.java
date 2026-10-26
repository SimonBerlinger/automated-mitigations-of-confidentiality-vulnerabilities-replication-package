package de.uulm.masterthesis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.dfd.datadictionary.AbstractAssignment;
import org.dataflowanalysis.dfd.datadictionary.Assignment;
import org.dataflowanalysis.dfd.datadictionary.Behavior;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.parameter.VariableUsage;

import de.uka.ipd.sdq.stoex.AbstractNamedReference;
import de.uka.ipd.sdq.stoex.VariableReference;
import de.uulm.masterthesis.util.DfaBehaviourUtils;
import de.uulm.masterthesis.util.PinDirection;
import de.uulm.masterthesis.util.TransformationUtils;

/**
 * Builds DFD behaviour from PCM confidentiality annotations.
 */
public class ComponentBehaviourBuilder {

	private final Behavior behavior;
	private final DataDictionary dataDictionary;
	private final String componentId;
	private final Map<String, Pin> inputPinsByName = new HashMap<>();
	private final Map<String, Pin> outputPinsByName = new HashMap<>();
	private final List<AbstractAssignment> assignments = new ArrayList<>();

	public ComponentBehaviourBuilder(Behavior behavior, DataDictionary dataDictionary, String componentId) {
		this.behavior = behavior;
		this.dataDictionary = dataDictionary;
		this.componentId = componentId;
		// Index pins already created by DataFlowCreator
		for (Pin pin : behavior.getInPin()) {
			inputPinsByName.put(pin.getEntityName(), pin);
			if (pin.getEntityName() != null && pin.getEntityName().startsWith(TransformationUtils.IN_PREFIX)) {
				String neighbour = pin.getEntityName().substring(TransformationUtils.IN_PREFIX.length());
				inputPinsByName.putIfAbsent(neighbour, pin);
			}
		}
		for (Pin pin : behavior.getOutPin()) {
			outputPinsByName.put(pin.getEntityName(), pin);
			if (pin.getEntityName() != null && pin.getEntityName().startsWith(TransformationUtils.OUT_PREFIX)) {
				String neighbour = pin.getEntityName().substring(TransformationUtils.OUT_PREFIX.length());
				outputPinsByName.putIfAbsent(neighbour, pin);
			}
		}
		if (!behavior.getInPin().isEmpty()) {
			inputPinsByName.putIfAbsent("", behavior.getInPin().get(0));
		}
	}

	/**
	 * @param neighbourComponentName entity name of the neighbour component this pin
	 *                               relates to
	 */
	public void processVariableUsage(VariableUsage usage, PinDirection direction, String neighbourComponentName) {
		if (neighbourComponentName == null || neighbourComponentName.isBlank()) {
			return;
		}
		AbstractNamedReference namedReference = usage.getNamedReference__VariableUsage();
		if (!(namedReference instanceof VariableReference)) {
			return;
		}

		Pin targetPin;
		if (direction == PinDirection.INPUT) {
			targetPin = TransformationUtils.getOrCreateInPinFrom(behavior, componentId, neighbourComponentName);
			inputPinsByName.put(neighbourComponentName, targetPin);
			inputPinsByName.put(targetPin.getEntityName(), targetPin);
			inputPinsByName.putIfAbsent("", targetPin);
		} else {
			targetPin = TransformationUtils.getOrCreateOutPinTo(behavior, componentId, neighbourComponentName);
			outputPinsByName.put(neighbourComponentName, targetPin);
			outputPinsByName.put(targetPin.getEntityName(), targetPin);
		}

		for (VariableCharacterisation characterisation : usage.getVariableCharacterisation_VariableUsage()) {
			if (characterisation instanceof ConfidentialityVariableCharacterisation confidentialityUsage) {
				assignments.addAll(DfaBehaviourUtils.processCharacterization(confidentialityUsage, targetPin,
						dataDictionary, inputPinsByName, outputPinsByName, componentId));
			}
		}
	}

	/**
	 * Usage-model entry: apply to every existing out-pin of this boundary
	 */
	public void processUsageEntryCharacterisation(VariableUsage usage) {
		AbstractNamedReference namedReference = usage.getNamedReference__VariableUsage();
		if (!(namedReference instanceof VariableReference)) {
			return;
		}
		List<Pin> outPins = new ArrayList<>(behavior.getOutPin());
		if (outPins.isEmpty()) {

			Pin userOut = TransformationUtils.getOrCreateOutPinTo(behavior, componentId, "User");
			outPins.add(userOut);
			outputPinsByName.put("User", userOut);
			outputPinsByName.put(userOut.getEntityName(), userOut);
		}
		for (Pin outPin : outPins) {
			for (VariableCharacterisation characterisation : usage.getVariableCharacterisation_VariableUsage()) {
				if (characterisation instanceof ConfidentialityVariableCharacterisation confidentialityUsage) {
					assignments.addAll(DfaBehaviourUtils.processCharacterization(confidentialityUsage, outPin,
							dataDictionary, inputPinsByName, outputPinsByName, componentId));
				}
			}
		}
	}

	public void installAssignments(String componentName) {
		behavior.getAssignment().clear();

		List<AbstractAssignment> faulty = new ArrayList<>();
		for (AbstractAssignment assignment : assignments) {
			if (assignment.getOutputPin() == null) {
				faulty.add(assignment);
			}
		}
		faulty.forEach(assignments::remove);

		addAssignments(behavior.getAssignment(), assignments);

		System.out.println("Behaviour for [" + componentName + "]: " + assignments.size() + " assignment(s), "
				+ behavior.getInPin().size() + " in-pin(s), " + behavior.getOutPin().size() + " out-pin(s)");
	}

	public void addAssignments(List<AbstractAssignment> target, List<AbstractAssignment> incoming) {
		for (AbstractAssignment candidate : incoming) {
			if (candidate instanceof Assignment forward && isConditional(forward)
					&& target.stream().anyMatch(existing -> shadows(existing, forward))) {
				continue;
			}
			if (candidate instanceof Assignment set && isConstantSet(set)) {
				target.removeIf(existing -> existing instanceof Assignment forward && isConditional(forward)
						&& shadows(set, forward));
			}
			target.add(candidate);
		}
	}

	private boolean isConstantSet(AbstractAssignment assignment) {
		return assignment instanceof Assignment a
				&& a.getTerm() instanceof org.dataflowanalysis.dfd.datadictionary.TRUE;
	}

	private boolean isConditional(Assignment assignment) {
		return assignment.getTerm() != null
				&& !(assignment.getTerm() instanceof org.dataflowanalysis.dfd.datadictionary.TRUE);
	}

	/**
	 * Same output pin, and the conditional assignment writes a label the set
	 * already introduces.
	 */
	private boolean shadows(AbstractAssignment set, Assignment forward) {
		if (!(set instanceof Assignment constant) || !isConstantSet(constant)) {
			return false;
		}
		if (constant.getOutputPin() == null || !constant.getOutputPin().equals(forward.getOutputPin())) {
			return false;
		}
		return forward.getOutputLabels().stream().anyMatch(constant.getOutputLabels()::contains);
	}
}
