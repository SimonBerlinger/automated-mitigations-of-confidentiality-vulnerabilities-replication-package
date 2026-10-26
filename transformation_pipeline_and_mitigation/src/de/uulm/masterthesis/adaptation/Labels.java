package de.uulm.masterthesis.adaptation;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dataflowanalysis.converter.dfd2web.DataFlowDiagramAndDictionary;
import org.dataflowanalysis.dfd.datadictionary.AbstractAssignment;
import org.dataflowanalysis.dfd.datadictionary.Assignment;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.LabelType;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.datadictionary.SetAssignment;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.eclipse.emf.ecore.EObject;

import de.uulm.masterthesis.util.TransformationUtils;

/**
 * Node names, node properties, and neighbor labels read from one diagram.
 */
record Labels(Map<String, String> nodeNames, Map<String, Set<LabelKey>> nodeProperties,
		Map<PinKey, Set<LabelKey>> outgoingLabels) {

	static Labels from(DataFlowDiagramAndDictionary model) {
		Map<String, String> nodeNames = new LinkedHashMap<>();
		Map<String, Set<LabelKey>> nodeProperties = new LinkedHashMap<>();
		Map<PinKey, Set<LabelKey>> outgoingLabels = new LinkedHashMap<>();
		for (Node node : model.dataFlowDiagram().getNodes()) {
			nodeNames.put(node.getId(), node.getEntityName());
			nodeProperties.put(node.getId(), labelKeys(node.getProperties()));
			readOutgoing(node, outgoingLabels);
		}
		return new Labels(nodeNames, nodeProperties, outgoingLabels);
	}

	/**
	 * Labels present on {@code repaired} and absent from {@code original}.
	 */
	static RepairDiff additions(Labels original, Labels repaired) {
		RepairDiff diff = new RepairDiff();
		for (var entry : repaired.nodeProperties.entrySet()) {
			String nodeId = entry.getKey();
			String nodeName = nodeName(original, repaired, nodeId);
			Set<LabelKey> previous = original.nodeProperties.getOrDefault(nodeId, Set.of());
			for (LabelKey label : entry.getValue()) {
				if (previous.contains(label)) {
					continue;
				}
				diff.nodeProperties()
						.add(new NodePropertyChange(nodeId, nodeName, label.typeName(), label.valueName()));
			}
		}
		for (var entry : repaired.outgoingLabels.entrySet()) {
			PinKey pin = entry.getKey();
			String nodeName = nodeName(original, repaired, pin.nodeId());
			String neighbour = pin.pinName().substring(TransformationUtils.OUT_PREFIX.length());
			Set<LabelKey> previous = original.outgoingLabels.getOrDefault(pin, Set.of());
			for (LabelKey label : entry.getValue()) {
				if (previous.contains(label)) {
					continue;
				}
				diff.outgoingLabels().add(new OutgoingLabelChange(pin.nodeId(), nodeName, neighbour, label.typeName(),
						label.valueName()));
			}
		}
		return diff;
	}

	private static String nodeName(Labels original, Labels repaired, String nodeId) {
		if (repaired.nodeNames.containsKey(nodeId)) {
			return repaired.nodeNames.get(nodeId);
		}
		return original.nodeNames.getOrDefault(nodeId, nodeId);
	}

	private static void readOutgoing(Node node, Map<PinKey, Set<LabelKey>> outgoingLabels) {
		if (node.getBehavior() == null) {
			return;
		}
		for (AbstractAssignment assignment : node.getBehavior().getAssignment()) {
			Pin outputPin = assignment.getOutputPin();
			if (outputPin == null || outputPin.getEntityName() == null
					|| !outputPin.getEntityName().startsWith(TransformationUtils.OUT_PREFIX)) {
				continue;
			}
			PinKey pin = new PinKey(node.getId(), outputPin.getEntityName());
			outgoingLabels.computeIfAbsent(pin, key -> new LinkedHashSet<>())
					.addAll(labelKeys(outputLabels(assignment)));
		}
	}

	private static Set<LabelKey> labelKeys(Iterable<Label> labels) {
		Set<LabelKey> keys = new LinkedHashSet<>();
		for (Label label : labels) {
			LabelKey key = labelKey(label);
			if (key != null) {
				keys.add(key);
			}
		}
		return keys;
	}

	private static LabelKey labelKey(Label label) {
		EObject container = label.eContainer();
		if (!(container instanceof LabelType labelType) || labelType.getEntityName() == null
				|| label.getEntityName() == null) {
			return null;
		}
		return new LabelKey(labelType.getEntityName(), label.getEntityName());
	}

	private static List<Label> outputLabels(AbstractAssignment assignment) {
		if (assignment instanceof Assignment labelled) {
			return labelled.getOutputLabels();
		}
		if (assignment instanceof SetAssignment labelled) {
			return labelled.getOutputLabels();
		}
		return List.of();
	}
}
