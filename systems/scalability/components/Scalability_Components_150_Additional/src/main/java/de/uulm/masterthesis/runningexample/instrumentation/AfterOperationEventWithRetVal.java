package de.uulm.masterthesis.runningexample.instrumentation;

import java.nio.BufferOverflowException;

import kieker.common.exception.RecordInstantiationException;
import kieker.common.record.flow.trace.operation.AfterOperationEvent;
import kieker.common.record.io.IValueDeserializer;
import kieker.common.record.io.IValueSerializer;


public class AfterOperationEventWithRetVal extends AfterOperationEvent implements IReturnValueCharacterization {
    /** Descriptive definition of the serialization size of the record. */
    public static final int SIZE = TYPE_SIZE_LONG // IEventRecord.timestamp
             + TYPE_SIZE_LONG // ITraceRecord.traceId
             + TYPE_SIZE_INT // ITraceRecord.orderIndex
             + TYPE_SIZE_STRING // IOperationSignature.operationSignature
             + TYPE_SIZE_STRING // IClassSignature.classSignature
             + TYPE_SIZE_STRING; // IReturnValueCharacterization.returnValue

    public static final Class<?>[] TYPES = {
        long.class, // IEventRecord.timestamp
        long.class, // ITraceRecord.traceId
        int.class, // ITraceRecord.orderIndex
        String.class, // IOperationSignature.operationSignature
        String.class, // IClassSignature.classSignature
        String.class, // IReturnValueCharacterization.returnValue
    };

    private static final long serialVersionUID = -3583783831259543535L;

    /** property name array. */
    public static final String[] VALUE_NAMES = {
        "timestamp",
        "traceId",
        "orderIndex",
        "operationSignature",
        "classSignature",
        "returnValue",
    };

    /** property declarations. */
    private final String returnValue;

    /**
     * Creates a new instance of this class using the given parameters.
     */
    public AfterOperationEventWithRetVal(final long timestamp, final long traceId, final int orderIndex,
            final String operationSignature, final String classSignature, final String returnValue) {
        super(timestamp, traceId, orderIndex, operationSignature, classSignature);
        this.returnValue = returnValue;
    }

    /**
     * @param deserializer
     *            The deserializer to use
     * @throws RecordInstantiationException
     *            when the record could not be deserialized
     */
    public AfterOperationEventWithRetVal(final IValueDeserializer deserializer) throws RecordInstantiationException {
        super(deserializer);
        this.returnValue = deserializer.getString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final IValueSerializer serializer) throws BufferOverflowException {
        serializer.putLong(this.getTimestamp());
        serializer.putLong(this.getTraceId());
        serializer.putInt(this.getOrderIndex());
        serializer.putString(this.getOperationSignature());
        serializer.putString(this.getClassSignature());
        serializer.putString(this.getReturnValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?>[] getValueTypes() {
        return TYPES; // NOPMD
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getValueNames() {
        return VALUE_NAMES; // NOPMD
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSize() {
        return SIZE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }

        final AfterOperationEventWithRetVal castedRecord = (AfterOperationEventWithRetVal) obj;
        if (this.getLoggingTimestamp() != castedRecord.getLoggingTimestamp()) {
            return false;
        }
        if (this.getTimestamp() != castedRecord.getTimestamp()) {
            return false;
        }
        if (this.getTraceId() != castedRecord.getTraceId()) {
            return false;
        }
        if (this.getOrderIndex() != castedRecord.getOrderIndex()) {
            return false;
        }
        if (!this.getOperationSignature().equals(castedRecord.getOperationSignature())) {
            return false;
        }
        if (!this.getClassSignature().equals(castedRecord.getClassSignature())) {
            return false;
        }
        if (!this.getReturnValue().equals(castedRecord.getReturnValue())) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int code = 0;
        code += ((int) this.getTimestamp());
        code += ((int) this.getTraceId());
        code += ((int) this.getOrderIndex());
        code += this.getOperationSignature().hashCode();
        code += this.getClassSignature().hashCode();
        code += this.getReturnValue().hashCode();

        return code;
    }

    public final String getReturnValue() {
        return this.returnValue;
    }
}
