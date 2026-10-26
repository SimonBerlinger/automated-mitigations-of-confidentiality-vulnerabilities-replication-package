package de.uulm.masterthesis.util;

/**
 * Parsed Kieker {@code $4} record ({@code EntryLevelAfterOperationEvent}).
 */
public class MethodExitRecord implements TraceRecord {

	public long loggingTimestamp;
	public long timestamp;
	public long traceId;
	public int orderIndex;
	public String operationSignature;
	public String classSignature;
	public String returnValue;
	// Integer becaues the value is computed and thus 'null' at start
	public Integer nestingDepth;

	public MethodExitRecord(long loggingTimestamp, long timestamp, long traceId, int orderIndex, String operationSignature,
			String classSignature, String returnValue) {
		this.loggingTimestamp = loggingTimestamp;
		this.timestamp = timestamp;
		this.traceId = traceId;
		this.orderIndex = orderIndex;
		this.operationSignature = operationSignature;
		this.classSignature = classSignature;
		this.returnValue = returnValue;
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
		return "MethodExitRecord{" + "traceId=" + traceId + ", orderIndex=" + orderIndex + ", nestingDepth="
				+ nestingDepth + ", classSignature='" + classSignature + '\'' + ", operationSignature='"
				+ operationSignature + '\'' + ", returnValue='" + returnValue + '\'' + '}';
	}
}