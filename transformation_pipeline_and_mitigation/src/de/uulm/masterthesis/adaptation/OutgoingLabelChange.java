package de.uulm.masterthesis.adaptation;

/**
 * A label added to an out-pin. Mapped to a constant set on external calls from
 * that assembly to the neighbour.
 */
public record OutgoingLabelChange(String assemblyContextId, String assemblyName, String neighbourName, String typeName,
		String valueName) {
}