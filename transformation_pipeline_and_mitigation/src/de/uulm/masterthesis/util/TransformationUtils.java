package de.uulm.masterthesis.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.dfd.datadictionary.Assignment;
import org.dataflowanalysis.dfd.datadictionary.Behavior;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.ForwardingAssignment;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.LabelReference;
import org.dataflowanalysis.dfd.datadictionary.LabelType;
import org.dataflowanalysis.dfd.datadictionary.NOT;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.CharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.palladiosimulator.pcm.core.composition.AssemblyConnector;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.composition.ProvidedDelegationConnector;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationRequiredRole;
import org.palladiosimulator.pcm.seff.ExternalCallAction;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;

public final class TransformationUtils {

	public static final String IN_PREFIX = "in-";
	public static final String OUT_PREFIX = "out-";

	private TransformationUtils() {
	}

	public static Pin getOrCreateInPinFrom(Node node, String neighbourComponentName) {
		return getOrCreatePin(node.getBehavior(), node.getId(), IN_PREFIX + neighbourComponentName, true);
	}

	public static Pin getOrCreateOutPinTo(Node node, String neighbourComponentName) {
		return getOrCreatePin(node.getBehavior(), node.getId(), OUT_PREFIX + neighbourComponentName, false);
	}

	public static Pin getOrCreateInPinFrom(Behavior behavior, String ownerComponentId, String neighbourComponentName) {
		return getOrCreatePin(behavior, ownerComponentId, IN_PREFIX + neighbourComponentName, true);
	}

	public static Pin getOrCreateOutPinTo(Behavior behavior, String ownerComponentId, String neighbourComponentName) {
		return getOrCreatePin(behavior, ownerComponentId, OUT_PREFIX + neighbourComponentName, false);
	}

	private static Pin getOrCreatePin(Behavior behavior, String ownerComponentId, String pinName, boolean input) {
		if (behavior == null) {
			throw new IllegalStateException("Missing behaviour for pin " + pinName);
		}
		var pins = input ? behavior.getInPin() : behavior.getOutPin();
		for (Pin pin : pins) {
			if (pinName.equals(pin.getEntityName())) {
				return pin;
			}
		}
		Pin pin = datadictionaryFactory.eINSTANCE.createPin();
		pin.setEntityName(pinName);
		pin.setId(ownerComponentId + "-" + pinName);
		pins.add(pin);
		return pin;
	}

	/**
	 * Providing assembly entity names for every assembly connector of
	 * {@code externalCall}'s required role that leaves {@code callerContext}. Names
	 * match the DFD pins created from assembly entity names.
	 */
	public static List<String> resolveProvidingAssemblyNames(System system, AssemblyContext callerContext,
			ExternalCallAction externalCall) {
		List<String> names = new ArrayList<>();
		if (system == null || callerContext == null || externalCall == null
				|| externalCall.getRole_ExternalService() == null) {
			return names;
		}
		OperationRequiredRole requiredRole = externalCall.getRole_ExternalService();

		for (var connector : system.getConnectors__ComposedStructure()) {
			if (!(connector instanceof AssemblyConnector assemblyConnector)) {
				continue;
			}
			if (!requiredRole.equals(assemblyConnector.getRequiredRole_AssemblyConnector())) {
				continue;
			}
			if (!callerContext.equals(assemblyConnector.getRequiringAssemblyContext_AssemblyConnector())) {
				continue;
			}
			AssemblyContext providing = assemblyConnector.getProvidingAssemblyContext_AssemblyConnector();
			if (providing == null || providing.getEntityName() == null || providing.getEntityName().isBlank()) {
				continue;
			}
			if (!names.contains(providing.getEntityName())) {
				names.add(providing.getEntityName());
			}
		}
		return names;
	}

	/**
	 * Returns a map from node id to node for all nodes in the DFD.
	 */
	public static Map<String, Node> indexNodesById(DataFlowDiagram dfd) {
		Map<String, Node> nodesById = new HashMap<>();
		for (Node node : dfd.getNodes()) {
			nodesById.put(node.getId(), node);
		}
		return nodesById;
	}

	public static LabelType getOrCreateLabelType(DataDictionary dataDictionary, String name,
			String characteristicTypeId) {
		return dataDictionary.getLabelTypes().stream().filter(
				labelType -> labelType.getEntityName().equals(name) || characteristicTypeId.equals(labelType.getId()))
				.findFirst().orElseGet(() -> createLabelType(dataDictionary, name, characteristicTypeId));
	}

	public static Label getOrCreateLabel(LabelType labelType, String labelName, String literalId) {
		return labelType.getLabel().stream()
				.filter(label -> label.getEntityName().equals(labelName) || literalId.equals(label.getId())).findFirst()
				.orElseGet(() -> createLabel(labelType, labelName, literalId));
	}

	public static Label getOrCreateLabel(DataDictionary dataDictionary, CharacteristicType characteristicType,
			Literal literal) {
		LabelType labelType = getOrCreateLabelType(dataDictionary, characteristicType.getName(),
				characteristicType.getId());
		return getOrCreateLabel(labelType, literal.getName(), literal.getId());
	}

	/**
	 * Resolves the repository component id targeted by a usage-model entry-level
	 * system call.
	 */
	public static String resolveRepositoryComponentIdForEntryCall(System system, EntryLevelSystemCall entryCall) {
		OperationProvidedRole providedRole = entryCall.getProvidedRole_EntryLevelSystemCall();
		if (providedRole == null) {
			return null;
		}

		for (ProvidedDelegationConnector connector : system.getConnectors__ComposedStructure().stream()
				.filter(ProvidedDelegationConnector.class::isInstance).map(ProvidedDelegationConnector.class::cast)
				.toList()) {
			if (!providedRole.equals(connector.getOuterProvidedRole_ProvidedDelegationConnector())) {
				continue;
			}

			AssemblyContext assemblyContext = connector.getAssemblyContext_ProvidedDelegationConnector();
			if (assemblyContext != null
					&& assemblyContext.getEncapsulatedComponent__AssemblyContext() instanceof BasicComponent target) {
				return target.getId();
			}
		}

		return null;
	}

	/**
	 * Returns the path of a PCM file with the specified extension in the given
	 * directory.
	 */
	public static String findPcmModelFile(Path pcmDirectory, String extension) throws IOException {
		for (Path file : Files.list(pcmDirectory).toList()) {
			if (Files.isRegularFile(file) && file.getFileName().toString().endsWith(extension)) {
				return file.toString();
			}
		}
		throw new IOException("No PCM file with extension " + extension + " found in " + pcmDirectory);
	}

	private static LabelType createLabelType(DataDictionary dataDictionary, String name, String characteristicTypeId) {
		LabelType labelType = datadictionaryFactory.eINSTANCE.createLabelType();
		labelType.setEntityName(name);
		labelType.setId(characteristicTypeId);
		dataDictionary.getLabelTypes().add(labelType);
		return labelType;
	}

	private static Label createLabel(LabelType labelType, String name, String literalId) {
		Label label = datadictionaryFactory.eINSTANCE.createLabel();
		label.setEntityName(name);
		label.setId(literalId);
		labelType.getLabel().add(label);
		return label;
	}

	static void nameAssignment(Assignment assignment, String componentId) {
		Pin outputPin = assignment.getOutputPin();
		if (outputPin == null || assignment.getOutputLabels().isEmpty()) {
			return;
		}

		Label label = assignment.getOutputLabels().get(0);
		org.dataflowanalysis.dfd.datadictionary.Term term = assignment.getTerm();
		if (term instanceof LabelReference labelReference) {
			labelReference.setId(componentId + "-label-ref-" + label.getId());
			labelReference.setEntityName(label.getEntityName());
			String sourceName = assignment.getInputPins().isEmpty() ? "unknown"
					: assignment.getInputPins().get(0).getEntityName();
			assignment.setId(
					componentId + "-fwd-" + sourceName + "-" + outputPin.getEntityName() + "-" + label.getEntityName());
			assignment.setEntityName(
					"forward " + label.getEntityName() + " " + sourceName + " -> " + outputPin.getEntityName());
		} else if (term instanceof NOT) {
			assignment.setId(componentId + "-negate-" + outputPin.getEntityName() + "-" + label.getEntityName());
			assignment.setEntityName("negate " + label.getEntityName() + " on " + outputPin.getEntityName());
		} else {
			assignment.setId(componentId + "-set-" + outputPin.getEntityName() + "-" + label.getEntityName());
			assignment.setEntityName("set " + label.getEntityName() + " on " + outputPin.getEntityName());
		}
	}

	static void nameForwardingAssignment(ForwardingAssignment assignment, String componentId) {
		Pin outputPin = assignment.getOutputPin();
		Pin sourcePin = assignment.getInputPins().isEmpty() ? null : assignment.getInputPins().get(0);
		String outputName = outputPin != null ? outputPin.getEntityName() : "unknown";
		String sourceName = sourcePin != null ? sourcePin.getEntityName() : "unknown";
		assignment.setId(componentId + "-fwd-all-" + sourceName + "-" + outputName);
		assignment.setEntityName("forward all " + sourceName + " -> " + outputName);
	}

	// Prefer named pin, then empty-key fallback, then any single remaining pin.
	static Pin resolveInputPin(Map<String, Pin> inputPinsByName, String name) {
		List<Pin> pins = resolveInputPins(inputPinsByName, name);
		return pins.isEmpty() ? null : pins.get(0);
	}

	// Every pin matching the variable name. A miss returns every in-pin.
	static List<Pin> resolveInputPins(Map<String, Pin> inputPinsByName, String name) {
		LinkedHashSet<Pin> matches = new LinkedHashSet<>();
		if (name != null) {
			Pin pin = inputPinsByName.get(name);
			if (pin != null) {
				matches.add(pin);
			}
			pin = inputPinsByName.get(IN_PREFIX + name);
			if (pin != null) {
				matches.add(pin);
			}
		}
		if (matches.isEmpty()) {
			matches.addAll(inputPinsByName.values());
		}
		return new ArrayList<>(matches);
	}
}