package de.uulm.masterthesis;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.dataflowanalysis.dfd.dataflowdiagram.dataflowdiagramFactory;

import de.uulm.masterthesis.util.FullMethodRecord;
import de.uulm.masterthesis.util.MethodEntryRecord;
import de.uulm.masterthesis.util.MethodExitRecord;
import de.uulm.masterthesis.util.TraceRecord;
import de.uulm.masterthesis.util.TransformationUtils;

public class DataFlowCreator {

	// IMPORTANT: We use $2 and $3 for entry and exit because we excluded Main to
	// get several traces instead of one large trace starting with main. If you
	// include main, you must use $3 and $4 respectively
	private static final String ENTRY_RECORD_TYPE = "$2";
	private static final String EXIT_RECORD_TYPE = "$3";

	/**
	 * Parses Kieker records and returns full method records per trace.
	 */
	public Map<Long, List<FullMethodRecord>> getFullMethodRecordsForTraces(String kiekerDatFolder) throws IOException {

		List<String> kiekerDatFiles = new ArrayList<>();

		File directory = new File(kiekerDatFolder);
		File[] files = directory.listFiles();

		if (files != null) {
			for (File file : files) {
				if (file.isFile() && file.getName().endsWith(".dat")) {
					kiekerDatFiles.add(file.getName());
				}
			}
		}

		System.out.println("Kieker .dat files found: " + kiekerDatFiles);

		Map<Long, List<TraceRecord>> recordsByTraceId = new HashMap<>();
		for (String kiekerDatFile : kiekerDatFiles) {
			for (String line : Files.readString(Path.of(kiekerDatFolder + "/" + kiekerDatFile)).split("\\R")) {
				if (line.isBlank() || line.charAt(0) != '$') {
					continue;
				}

				TraceRecord record = parseLine(line);
				if (record == null) {
					continue;
				}

				// Add the new record to the list for the respective trace
				List<TraceRecord> traceRecords = recordsByTraceId.get(record.traceId());
				if (traceRecords == null) {
					traceRecords = new ArrayList<>();
					recordsByTraceId.put(record.traceId(), traceRecords);
				}
				traceRecords.add(record);
			}
		}

		// for each trace combine the respective entry and exit records to a full method
		// record
		Map<Long, List<FullMethodRecord>> fullRecordsByTraceId = new HashMap<>();
		for (Map.Entry<Long, List<TraceRecord>> traceEntry : recordsByTraceId.entrySet()) {
			List<TraceRecord> traceRecords = traceEntry.getValue();
			traceRecords.sort(Comparator.comparingInt(TraceRecord::orderIndex));
			fullRecordsByTraceId.put(traceEntry.getKey(), buildFullMethodRecords(traceRecords));
		}

		return fullRecordsByTraceId;
	}

	/**
	 * For a list of records belonging to one execution trace, build the full
	 * records with a stack and returns the sorted list (execution order)
	 */
	private List<FullMethodRecord> buildFullMethodRecords(List<TraceRecord> traceRecords) {
		List<FullMethodRecord> fullRecords = new ArrayList<>();
		Deque<MethodEntryRecord> entryStack = new ArrayDeque<>();
		int depth = 0;

		for (TraceRecord traceRecord : traceRecords) {
			if (traceRecord instanceof MethodEntryRecord entry) {
				entry.nestingDepth = depth;
				depth++;
				entryStack.push(entry);
			} else if (traceRecord instanceof MethodExitRecord exit) {
				depth--;
				exit.nestingDepth = depth;
				if (!entryStack.isEmpty()) {
					fullRecords.add(new FullMethodRecord(entryStack.pop(), exit));
				}
			} else {
				throw new IllegalArgumentException("Record " + traceRecord + " fits no Record class");
			}
		}

		fullRecords.sort(Comparator.comparingInt(record -> record.entryOrderIndex));
		return fullRecords;
	}

	/**
	 * Creates data flows in the DFD from full method records and the
	 * class-to-component mapping.
	 */
	public DataFlowDiagram createDataflows(DataFlowDiagram dfd,
			Map<Long, List<FullMethodRecord>> fullRecordsByTraceId) {
		Set<String> existingFlows = collectExistingFlowKeys(dfd);
		for (List<FullMethodRecord> traceRecords : fullRecordsByTraceId.values()) {
			for (int callerIndex = 0; callerIndex < traceRecords.size(); callerIndex++) {
				checkCase1Flows(dfd, existingFlows, traceRecords, callerIndex);
				checkCase2Flows(dfd, existingFlows, traceRecords, callerIndex);				
			}
		}
		return dfd;
	}

	private void checkCase1Flows(DataFlowDiagram dfd, Set<String> existingFlows, List<FullMethodRecord> traceRecords,
			int callerIndex) {
		FullMethodRecord caller = traceRecords.get(callerIndex);
		for (int calleeIndex = callerIndex + 1; calleeIndex < traceRecords.size(); calleeIndex++) {

			FullMethodRecord callee = traceRecords.get(calleeIndex);

			if (!isDirectCalleeWithParameters(caller, callee)) {
				continue;
			}

			String sourceNodeId = caller.componentId;
			String destinationNodeId = callee.componentId;
			if (sourceNodeId == null || destinationNodeId == null) {
				System.out.println("The component id of the class(es): " + sourceNodeId == null ? caller.classSignature
						: "" + destinationNodeId == null ? callee.classSignature : "" + " is null");
				continue;
			}

			Node sourceNode = null;
			for (Node node : dfd.getNodes()) {
				if (node.getId().equals(sourceNodeId) || node.getEntityName().equals(sourceNodeId)) {
					sourceNode = node;
					break;
				}
			}

			Node destinationNode = null;
			for (Node node : dfd.getNodes()) {
				if (node.getId().equals(destinationNodeId) || node.getEntityName().equals(destinationNodeId)) {
					destinationNode = node;
					break;
				}
			}

			if (sourceNode == null || destinationNode == null) {
				System.out.println("The component for the class(es): " + (sourceNode == null ? caller.classSignature
						: "") + (destinationNode == null ? callee.classSignature : "") + " can not be found");
				continue;
			}

//			System.out.println("Detected Dataflow (CASE 1) from [" + caller.classSignature + "] to ["
//					+ callee.classSignature + "] : " + callee.operationSignature);
			addComponentFlow(dfd, sourceNode, destinationNode, existingFlows);
		}
	}

	private void checkCase2Flows(DataFlowDiagram dfd, Set<String> existingFlows, List<FullMethodRecord> traceRecords,
			int callerIndex) {
		FullMethodRecord caller = traceRecords.get(callerIndex);
		for (int calleeIndex = callerIndex + 1; calleeIndex < traceRecords.size(); calleeIndex++) {
			FullMethodRecord callee = traceRecords.get(calleeIndex);
			if (!isDirectCalleeWithReturnValue(caller, callee)) {
				continue;
			}

			String sourceNodeId = callee.componentId;
			String destinationNodeId = caller.componentId;
			if (sourceNodeId == null || destinationNodeId == null) {
				System.out.println("The component id of the class(es): " + sourceNodeId == null ? caller.classSignature
						: "" + destinationNodeId == null ? callee.classSignature : "" + " is null");
				continue;
			}

			Node sourceNode = null;
			for (Node node : dfd.getNodes()) {
				if (node.getId().equals(sourceNodeId) || node.getEntityName().equals(sourceNodeId)) {
					sourceNode = node;
					break;
				}
			}

			Node destinationNode = null;
			for (Node node : dfd.getNodes()) {
				if (node.getId().equals(destinationNodeId) || node.getEntityName().equals(destinationNodeId)) {
					destinationNode = node;
					break;
				}
			}
			if (sourceNode == null || destinationNode == null) {
				System.out.println("The component for the class(es): " + (sourceNode == null ? caller.classSignature
						: "") + (destinationNode == null ? callee.classSignature : "") + " can not be found");
				continue;
			}

//					System.out.println("Detected Dataflow (CASE 2) from [" + callee.classSignature + "] to ["
//							+ caller.classSignature + "] " + callee.operationSignature);
			addComponentFlow(dfd, sourceNode, destinationNode, existingFlows);
		}
	}

	private FullMethodRecord findCallingOperation(List<FullMethodRecord> records, FullMethodRecord callee) {
		for (int i = records.indexOf(callee) - 1; i >= 0; i--) {
			FullMethodRecord currentElement = records.get(i);
			if (currentElement.nestingDepth == callee.nestingDepth - 1
					&& currentElement.exitOrderIndex > callee.exitOrderIndex) {
				return currentElement;
			}
		}
		return null;
	}

	/**
	 * Checks, if the method caller calls the method callee with parameters
	 */
	private boolean isDirectCalleeWithParameters(FullMethodRecord caller, FullMethodRecord callee) {
		if (caller.nestingDepth == null || callee.nestingDepth == null) {
			return false;
		}

		return caller.entryOrderIndex < callee.entryOrderIndex && callee.nestingDepth == caller.nestingDepth + 1
				&& callee.entryOrderIndex < caller.exitOrderIndex && callee.parameterNames != null
				&& callee.parameterNames.length > 0;
	}

	private boolean isDirectCalleeWithReturnValue(FullMethodRecord caller, FullMethodRecord callee) {
		if (caller.nestingDepth == null || callee.nestingDepth == null) {
			return false;
		}
		// System.out.println("RET " + callee.returnValue);
		return caller.entryOrderIndex < callee.entryOrderIndex && callee.nestingDepth == caller.nestingDepth + 1
				&& callee.entryOrderIndex < caller.exitOrderIndex && callee.returnValue != null
				&& callee.returnValue != "";
	}

	private void addComponentFlow(DataFlowDiagram dfd, Node sourceNode, Node destinationNode,
			Set<String> existingFlows) {
		if (sourceNode.getId().equals(destinationNode.getId())) {
			return;
		}

		String flowKey = flowKey(sourceNode.getId(), destinationNode.getId());
		if (existingFlows.contains(flowKey)) {
			return;
		}

		Flow flow = dataflowdiagramFactory.eINSTANCE.createFlow();
		flow.setId(flowKey);
		flow.setEntityName(sourceNode.getEntityName() + " -> " + destinationNode.getEntityName());
		flow.setSourceNode(sourceNode);
		flow.setDestinationNode(destinationNode);
		Pin sourcePin = TransformationUtils.getOrCreateOutPinTo(sourceNode, destinationNode.getEntityName());
		Pin destPin = TransformationUtils.getOrCreateInPinFrom(destinationNode, sourceNode.getEntityName());
		flow.setSourcePin(sourcePin);
		flow.setDestinationPin(destPin);

		dfd.getFlows().add(flow);
		existingFlows.add(flowKey);
	}

	private Set<String> collectExistingFlowKeys(DataFlowDiagram dfd) {
		Set<String> existingFlows = new HashSet<>();
		for (Flow flow : dfd.getFlows()) {
			existingFlows.add(flowKey(flow.getSourceNode().getId(), flow.getDestinationNode().getId()));
		}
		return existingFlows;
	}

	private String flowKey(String sourceNodeId, String destinationNodeId) {
		return sourceNodeId + "-to-" + destinationNodeId;
	}

	/**
	 * Parse a line into either entry or exit record
	 */
	private TraceRecord parseLine(String line) {
		String[] fields = line.split(";", -1);
		if (fields.length < 2) {
			return null;
		}

		return switch (fields[0]) {
		case ENTRY_RECORD_TYPE -> parseEntryRecord(fields);
		case EXIT_RECORD_TYPE -> parseExitRecord(fields);
		default -> null;
		};
	}

	/**
	 * Create an entry record object from an array of fields
	 */
	private MethodEntryRecord parseEntryRecord(String[] fields) {
		if (fields.length < 8) {
			return null;
		}

		int index = 7;
		int parameterCount = Integer.parseInt(fields[index]);
		index++;
		String[] parameterNames = readStringArray(fields, index, parameterCount);
		index += parameterCount;

		int valueCount = Integer.parseInt(fields[index]);
		index++;
		String[] parameterValues = readStringArray(fields, index, valueCount);
		index += valueCount;

		int requestType = Integer.parseInt(fields[index]);
		index++;

		if (index >= fields.length) {
			System.err.println("Ignore incomplete Record: " + fields);
			;
			return null;
		}

		return new MethodEntryRecord(Long.parseLong(fields[1]), Long.parseLong(fields[2]), Long.parseLong(fields[3]),
				Integer.parseInt(fields[4]), fields[5], fields[6], parameterNames, parameterValues, requestType,
				fields[index]);

	}

	/**
	 * Create an exit record object from an array of fields
	 */
	private MethodExitRecord parseExitRecord(String[] fields) {
		if (fields.length < 8) {
			return null;
		}
		return new MethodExitRecord(Long.parseLong(fields[1]), Long.parseLong(fields[2]), Long.parseLong(fields[3]),
				Integer.parseInt(fields[4]), fields[5], fields[6], fields[7]);
	}

	/**
	 * Read a string array of a predefined length for parameters of the entry record
	 */
	private String[] readStringArray(String[] fields, int startIndex, int length) {
		String[] values = new String[length];
		for (int i = 0; i < length; i++) {
			values[i] = fields[startIndex + i];
		}
		return values;
	}
}