package de.uulm.masterthesis;

import org.dataflowanalysis.dfd.datadictionary.Behavior;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.dataflowanalysis.dfd.dataflowdiagram.Process;
import org.dataflowanalysis.dfd.dataflowdiagram.dataflowdiagramFactory;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.BasicComponent;

public class NodeCreator {

	/**
	 * Adds nodes for all components of the passed pcm model to the passed data flow
	 * diagram and returns it
	 */
	public DataFlowDiagram createNodes(DataFlowDiagram dfd, DataDictionary dataDictionary, AnnotatedPCM annotatedPcm) {
		for (int index = 0; index < annotatedPcm.assemblyContexts.size(); index++) {
			createNodeForComponent(dfd, dataDictionary, annotatedPcm.assemblyContexts.get(index));
		}

		return dfd;
	}

	/**
	 * Creates a single node for one component
	 */
	private Node createNodeForComponent(DataFlowDiagram dfd, DataDictionary dataDictionary,
			AssemblyContext assemblyContext) {		
		Process node = dataflowdiagramFactory.eINSTANCE.createProcess();
		node.setEntityName(assemblyContext.getEntityName());
		node.setId(assemblyContext.getId());

		//set empty behaviour
		Behavior behaviour = datadictionaryFactory.eINSTANCE.createBehavior();
		behaviour.setId(assemblyContext.getId());
		behaviour.setEntityName(assemblyContext.getEntityName());
		node.setBehavior(behaviour);		
		
		dataDictionary.getBehavior().add(behaviour);
		dfd.getNodes().add(node);

		return node;
	}
}