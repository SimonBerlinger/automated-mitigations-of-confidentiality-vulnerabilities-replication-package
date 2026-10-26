/***************************************************************************
 * Copyright (C) 2018 iObserve Project (https://www.iobserve-devops.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/
package de.uulm.masterthesis.runningexample.instrumentation;

import kieker.common.record.flow.trace.TraceMetadata;
import kieker.common.record.flow.trace.operation.AfterOperationFailedEvent;
import kieker.common.record.flow.trace.operation.BeforeOperationEvent;
import kieker.monitoring.core.controller.IMonitoringController;
import kieker.monitoring.core.controller.MonitoringController;
import kieker.monitoring.core.registry.TraceRegistry;
import kieker.monitoring.probe.aspectj.AbstractAspectJProbe;
import kieker.monitoring.timer.ITimeSource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Based on Jan Waller's
 * kieker.monitoring.probe.aspectj.flow.operationExecution.AbstractAspect class.
 *
 * @author Reiner Jung
 *
 * @since 0.0.3
 */
@Aspect
public abstract class AbstractOperationExecutionWithParameterAspect extends AbstractAspectJProbe { // NOPMD

	private static final IMonitoringController CTRLINST = MonitoringController.getInstance();
	private static final ITimeSource TIME = AbstractOperationExecutionWithParameterAspect.CTRLINST.getTimeSource();
	private static final TraceRegistry TRACEREGISTRY = TraceRegistry.INSTANCE;
	private static final java.util.Map<Class<?>, java.util.Optional<java.lang.reflect.Field>> COMPONENT_ID_FIELD_CACHE =
	        new java.util.concurrent.ConcurrentHashMap<>();

	private static String extractPublicStringField(final Object thisObject, final String fieldName,
	        final java.util.Map<Class<?>, java.util.Optional<java.lang.reflect.Field>> cache) {
	    final Class<?> clazz = thisObject.getClass();
	    final java.util.Optional<java.lang.reflect.Field> cached = cache.computeIfAbsent(clazz, c -> {
	        try {
	            return java.util.Optional.of(c.getField(fieldName));
	        } catch (final NoSuchFieldException e) {
	            return java.util.Optional.empty();
	        }
	    });
	    if (cached.isEmpty()) {
	        return null;
	    }
	    try {
	        final Object value = cached.get().get(thisObject);
	        return value == null ? null : value.toString();
	    } catch (final IllegalAccessException e) {
	        return null;
	    }
	}

	private static String valueToString(final Object value, final Class<?> type) {
	    if (type == void.class || type == Void.class) {
	        return "";
	    }
	    if (value == null) {
	        return "null";
	    }
	    if (type.isPrimitive()) {
	        return value.toString();
	    }
	    if (type.isArray()) {
	        final int n = java.lang.reflect.Array.getLength(value);
	        final Class<?> comp = type.getComponentType();
	        final StringBuilder sb = new StringBuilder("[");
	        for (int i = 0; i < n; i++) {
	            if (i > 0) sb.append(',');
	            sb.append(valueToString(java.lang.reflect.Array.get(value, i), comp));
	        }
	        return sb.append(']').toString();
	    }
	    return type.getName() + "#" + System.identityHashCode(value);
	}

	private static String returnValueToString(final Object value, final Class<?> returnType) {
		return valueToString(value, returnType);
	}

	private static String extractComponentId(final Object thisObject) {
		return AbstractOperationExecutionWithParameterAspect.extractPublicStringField(thisObject, "componentId",
				AbstractOperationExecutionWithParameterAspect.COMPONENT_ID_FIELD_CACHE);
	}

	/**
	 * The pointcut for the monitored operations. Inheriting classes should extend
	 * the pointcut in order to find the correct executions of the methods (e.g. all
	 * methods or only methods with specific annotations).
	 */
	@Pointcut
	public abstract void monitoredOperation();

	@Around("monitoredOperation() && this(thisObject) && notWithinKieker()")
	public Object operation(final Object thisObject, final ProceedingJoinPoint thisJoinPoint) throws Throwable { // NOCS
																													// (Throwable)
		if (!AbstractOperationExecutionWithParameterAspect.CTRLINST.isMonitoringEnabled()) {
			return thisJoinPoint.proceed();
		}
		final String operationSignature = this.signatureToLongString(thisJoinPoint.getSignature());
		if (!AbstractOperationExecutionWithParameterAspect.CTRLINST.isProbeActivated(operationSignature)) {
			return thisJoinPoint.proceed();
		}

		// common fields
		TraceMetadata trace = AbstractOperationExecutionWithParameterAspect.TRACEREGISTRY.getTrace();
		final boolean newTrace = trace == null;
		if (newTrace) {
			trace = AbstractOperationExecutionWithParameterAspect.TRACEREGISTRY.registerTrace();
			AbstractOperationExecutionWithParameterAspect.CTRLINST.newMonitoringRecord(trace);
		}

		final long traceId = trace.getTraceId();
		final String clazz = thisObject.getClass().getName();
		final MethodSignature methodSignature = (MethodSignature) thisJoinPoint.getSignature();
		final String[] names = methodSignature.getParameterNames();
		final Object[] arguments = thisJoinPoint.getArgs();
		final Class<?>[] parameterTypes = methodSignature.getParameterTypes();
		final String[] values = new String[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			values[i] = AbstractOperationExecutionWithParameterAspect.valueToString(arguments[i], parameterTypes[i]);
		}
		
		final String componentId = AbstractOperationExecutionWithParameterAspect.extractComponentId(thisObject);

		AbstractOperationExecutionWithParameterAspect.CTRLINST.newMonitoringRecord(
				new EntryLevelBeforeOperationEvent(AbstractOperationExecutionWithParameterAspect.TIME.getTime(),
						traceId, trace.getNextOrderId(), operationSignature, clazz, names, values, 0, componentId));

		final Object retval;

		try {
			retval = thisJoinPoint.proceed();
		} catch (final Throwable th) { // NOPMD NOCS (catch throw might ok here)
			AbstractOperationExecutionWithParameterAspect.CTRLINST.newMonitoringRecord(
					new AfterOperationFailedEvent(AbstractOperationExecutionWithParameterAspect.TIME.getTime(), traceId,
							trace.getNextOrderId(), operationSignature, clazz, th.toString()));
			throw th;
		} finally {
			if (newTrace) {
				AbstractOperationExecutionWithParameterAspect.TRACEREGISTRY.unregisterTrace();
			}
		}

		AbstractOperationExecutionWithParameterAspect.CTRLINST.newMonitoringRecord(
				new AfterOperationEventWithRetVal(AbstractOperationExecutionWithParameterAspect.TIME.getTime(), traceId,
						trace.getNextOrderId(), operationSignature, clazz, AbstractOperationExecutionWithParameterAspect
								.returnValueToString(retval, methodSignature.getReturnType())));
		return retval;
	}

	@Around("monitoredOperation() && !this(java.lang.Object) && notWithinKieker()")
	public Object staticOperation(final ProceedingJoinPoint thisJoinPoint) throws Throwable { // NOCS
																								// (Throwable)
		if (!AbstractOperationExecutionWithParameterAspect.CTRLINST.isMonitoringEnabled()) {
			return thisJoinPoint.proceed();
		}
		final Signature sig = thisJoinPoint.getSignature();
		final String operationSignature = this.signatureToLongString(sig);
		if (!AbstractOperationExecutionWithParameterAspect.CTRLINST.isProbeActivated(operationSignature)) {
			return thisJoinPoint.proceed();
		}

		TraceMetadata trace = AbstractOperationExecutionWithParameterAspect.TRACEREGISTRY.getTrace();
		final boolean newTrace = trace == null;
		if (newTrace) {
			trace = AbstractOperationExecutionWithParameterAspect.TRACEREGISTRY.registerTrace();
			AbstractOperationExecutionWithParameterAspect.CTRLINST.newMonitoringRecord(trace);
		}

		final long traceId = trace.getTraceId();
		final String clazz = sig.getDeclaringTypeName();
		final MethodSignature methodSignature = (MethodSignature) sig;

		AbstractOperationExecutionWithParameterAspect.CTRLINST.newMonitoringRecord(
				new BeforeOperationEvent(AbstractOperationExecutionWithParameterAspect.TIME.getTime(), traceId,
						trace.getNextOrderId(), operationSignature, clazz));

		final Object retval;

		try {
			retval = thisJoinPoint.proceed();
		} catch (final Throwable th) { // NOPMD NOCS (catch throw might ok here)
			AbstractOperationExecutionWithParameterAspect.CTRLINST.newMonitoringRecord(
					new AfterOperationFailedEvent(AbstractOperationExecutionWithParameterAspect.TIME.getTime(), traceId,
							trace.getNextOrderId(), operationSignature, clazz, th.toString()));
			throw th;
		} finally {
			if (newTrace) {
				AbstractOperationExecutionWithParameterAspect.TRACEREGISTRY.unregisterTrace();
			}
		}

		AbstractOperationExecutionWithParameterAspect.CTRLINST.newMonitoringRecord(
				new AfterOperationEventWithRetVal(AbstractOperationExecutionWithParameterAspect.TIME.getTime(), traceId,
						trace.getNextOrderId(), operationSignature, clazz, AbstractOperationExecutionWithParameterAspect
								.returnValueToString(retval, methodSignature.getReturnType())));
		return retval;
	}
}