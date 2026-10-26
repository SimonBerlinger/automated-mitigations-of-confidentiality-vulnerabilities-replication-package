package de.uulm.masterthesis.adaptation;

/**
 * A characteristic added to one assembly node. Mapped to an assembly assignee
 */
public record NodePropertyChange(String assemblyContextId, String assemblyName, String typeName, String valueName) {
}