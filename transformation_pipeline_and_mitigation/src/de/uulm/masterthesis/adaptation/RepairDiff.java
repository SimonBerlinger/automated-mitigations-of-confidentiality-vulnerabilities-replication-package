package de.uulm.masterthesis.adaptation;

import java.util.ArrayList;
import java.util.List;

import org.dataflowanalysis.converter.dfd2web.DataFlowDiagramAndDictionary;

/**
 * Node properties and outgoing labels that the repaired diagram added.
 */
public final class RepairDiff {

	private final List<NodePropertyChange> nodeProperties = new ArrayList<>();
	private final List<OutgoingLabelChange> outgoingLabels = new ArrayList<>();

	public List<NodePropertyChange> nodeProperties() {
		return nodeProperties;
	}

	public List<OutgoingLabelChange> outgoingLabels() {
		return outgoingLabels;
	}

	public boolean isEmpty() {
		return nodeProperties.isEmpty() && outgoingLabels.isEmpty();
	}

	public static RepairDiff between(DataFlowDiagramAndDictionary original, DataFlowDiagramAndDictionary repaired) {
		return Labels.additions(Labels.from(original), Labels.from(repaired));
	}
}
