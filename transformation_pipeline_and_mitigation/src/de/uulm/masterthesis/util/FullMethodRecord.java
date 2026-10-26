package de.uulm.masterthesis.util;

import java.util.Arrays;

/**
 * Combines a MethodEntryRecord and its matching
 * MethodExitRecord.
 */
public class FullMethodRecord {

	public long traceId;
	public int entryOrderIndex;
	public int exitOrderIndex;
	public long entryLoggingTimestamp;
	public long exitLoggingTimestamp;
	public long entryTimestamp;
	public long exitTimestamp;
	public String operationSignature;
	public String classSignature;
	public String[] parameterNames;
	public String[] parameterValues;
	public int requestType;
	public String returnValue;
	public String componentId;
	//Integer becaues the value is computed and thus 'null' at start
	public Integer nestingDepth;

	public FullMethodRecord(MethodEntryRecord entry, MethodExitRecord exit) {
		this.traceId = entry.traceId;
		this.entryOrderIndex = entry.orderIndex;
		this.exitOrderIndex = exit.orderIndex;
		this.entryLoggingTimestamp = entry.loggingTimestamp;
		this.exitLoggingTimestamp = exit.loggingTimestamp;
		this.entryTimestamp = entry.timestamp;
		this.exitTimestamp = exit.timestamp;
		this.operationSignature = entry.operationSignature;
		this.classSignature = entry.classSignature;
		this.parameterNames = entry.parameterNames;
		this.parameterValues = entry.parameterValues;
		this.requestType = entry.requestType;
		this.returnValue = exit.returnValue;
		this.nestingDepth = entry.nestingDepth;
		this.componentId = entry.componentId;
	}

	@Override
	public String toString() {
		return "FullMethodRecord{" + "traceId=" + traceId + ", entryOrderIndex=" + entryOrderIndex + ", exitOrderIndex="
				+ exitOrderIndex + ", nestingDepth=" + nestingDepth + ", classSignature='" + classSignature + '\''
				+ ", operationSignature='" + operationSignature + '\'' + ", parameterNames="
				+ Arrays.toString(parameterNames) + ", parameterValues=" + Arrays.toString(parameterValues)
				+ ", requestType=" + requestType + ", returnValue='" + returnValue + '\'' + '}';
	}
}