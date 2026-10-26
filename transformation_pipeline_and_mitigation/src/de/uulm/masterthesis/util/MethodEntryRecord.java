package de.uulm.masterthesis.util;

import java.util.Arrays;

/**
 * Parsed Kieker $3 record (EntryLevelBeforeOperationEvent).
 */
public class MethodEntryRecord implements TraceRecord {

	public long loggingTimestamp;
	public long timestamp;
	public long traceId;
	public int orderIndex;
	public String operationSignature;
	public String classSignature;
	public String[] parameterNames;
	public String[] parameterValues;
	public int requestType;
	public String componentId;
	//Integer becaues the value is computed and thus 'null' at start
	public Integer nestingDepth;

	public MethodEntryRecord(long loggingTimestamp, long timestamp, long traceId, int orderIndex, String operationSignature,
			String classSignature, String[] parameterNames, String[] parameterValues, int requestType, String componentId) {
		this.loggingTimestamp = loggingTimestamp;
		this.timestamp = timestamp;
		this.traceId = traceId;
		this.orderIndex = orderIndex;
		this.operationSignature = operationSignature;
		this.classSignature = classSignature;
		this.parameterNames = parameterNames;
		this.parameterValues = parameterValues;
		this.requestType = requestType;
		this.componentId = componentId;		
	}

	@Override
	public long traceId() {
		return traceId;
	}

	@Override
	public int orderIndex() {
		return orderIndex;
	}

	@Override
	public String toString() {
		return "MethodEntryRecord{" + "traceId=" + traceId + ", orderIndex=" + orderIndex + ", nestingDepth="
				+ nestingDepth + ", classSignature='" + classSignature + '\'' + ", operationSignature='"
				+ operationSignature + '\'' + ", parameterNames=" + Arrays.toString(parameterNames)
				+ ", parameterValues=" + Arrays.toString(parameterValues) + ", requestType=" + requestType + ", componentId=" + componentId + '}';
	}
}