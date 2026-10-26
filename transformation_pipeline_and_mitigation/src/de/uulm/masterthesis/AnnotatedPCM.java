package de.uulm.masterthesis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.AssemblyAssignee;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.ResourceAssignee;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.UsageAssignee;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryComponent;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

public final class AnnotatedPCM {

	final UsageModel usageModel;
	final Repository repository;
	final System system;
	final Allocation allocation;
	final List<AssemblyContext> assemblyContexts;
	final List<BasicComponent> repositoryComponents;
	final Map<String, AssemblyContext> componentsById;
	final List<AssemblyAssignee> assemblyCharacteristics;
	final List<ResourceAssignee> resourceCharacteristics;
	final List<UsageAssignee> usageCharacteristics;

	AnnotatedPCM(UsageModel usageModel, Repository repository, System system, Allocation allocation,
			List<AssemblyContext> assemblyContexts, List<BasicComponent> repositoryComponents,
			List<AssemblyAssignee> assemblyCharacteristics, List<ResourceAssignee> resourceCharacteristics,
			List<UsageAssignee> usageCharacteristics) {

		this.usageModel = usageModel;
		this.repository = repository;
		this.system = system;
		this.allocation = allocation;
		this.assemblyContexts = assemblyContexts;
		this.repositoryComponents = repositoryComponents;
		this.assemblyCharacteristics = assemblyCharacteristics;
		this.resourceCharacteristics = resourceCharacteristics;
		this.usageCharacteristics = usageCharacteristics;

		this.componentsById = new HashMap<>();
		for (int index = 0; index < assemblyContexts.size(); index++) {
			componentsById.put(assemblyContexts.get(index).getId(), assemblyContexts.get(index));
		}
	}
}