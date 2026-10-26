package de.uulm.masterthesis.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.dataflowanalysis.dfd.datadictionary.AND;
import org.dataflowanalysis.dfd.datadictionary.AbstractAssignment;
import org.dataflowanalysis.dfd.datadictionary.Assignment;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.ForwardingAssignment;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.LabelReference;
import org.dataflowanalysis.dfd.datadictionary.NOT;
import org.dataflowanalysis.dfd.datadictionary.OR;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.datadictionary.datadictionaryFactory;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.And;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.False;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.Or;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.Term;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.expressions.True;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.dataflowanalysis.pcm.extension.model.confidentiality.expression.LhsEnumCharacteristicReference;
import org.dataflowanalysis.pcm.extension.model.confidentiality.expression.NamedEnumCharacteristicReference;

import de.uka.ipd.sdq.stoex.AbstractNamedReference;

/**
 * Methods adapted from {@code PCM2DFDConverter} of the DFA converter.
 */
public final class DfaBehaviourUtils {

	private DfaBehaviourUtils() {
	}

	// PCM2DFDConverter: processCharacterization
	public static List<AbstractAssignment> processCharacterization(
			ConfidentialityVariableCharacterisation variableCharacterisation, Pin targetPin,
			DataDictionary dataDictionary, Map<String, Pin> inputPinsByName, Map<String, Pin> outputPinsByName,
			String componentId) {
		var leftHandSide = (LhsEnumCharacteristicReference) variableCharacterisation.getLhs();

		EnumCharacteristicType characteristicType = (EnumCharacteristicType) leftHandSide.getCharacteristicType();
		Literal characteristicValue = leftHandSide.getLiteral();
		AbstractNamedReference reference = variableCharacterisation.getVariableUsage_VariableCharacterisation()
				.getNamedReference__VariableUsage();
		Term rightHandSide = variableCharacterisation.getRhs();
		List<Pin> inPins = new ArrayList<>(inputPinsByName.values());

		if (characteristicType == null && characteristicValue == null) {
			return List.of(processFullForwardingAssignment(targetPin, rightHandSide, reference, inputPinsByName,
					outputPinsByName, componentId));
		}
		if (characteristicValue == null) {
			if (characteristicType == null) {
				return List.of();
			}
			return processHalfForwardingAssignment(targetPin, rightHandSide, reference, characteristicType,
					dataDictionary, inputPinsByName, inPins, componentId);
		}
		if (!Objects.nonNull(characteristicType)) {
			throw new IllegalArgumentException(
					"Invalid assignment contains characteristic value, but no characteristic type");
		}
		return List.of(processAssignment(targetPin, rightHandSide, reference, characteristicType, characteristicValue,
				dataDictionary, inputPinsByName, inPins, componentId));
	}

	// PCM2DFDConverter: processFullForwardingAssignment (adapted)
	private static AbstractAssignment processFullForwardingAssignment(Pin targetPin, Term rightHandSide,
			AbstractNamedReference reference, Map<String, Pin> inputPinsByName, Map<String, Pin> outputPinsByName,
			String componentId) {
		if (!(rightHandSide instanceof NamedEnumCharacteristicReference namedEnumCharacteristicReference)) {
			return datadictionaryFactory.eINSTANCE.createForwardingAssignment();
		}

		ForwardingAssignment assignment = datadictionaryFactory.eINSTANCE.createForwardingAssignment();
		assignment.setOutputPin(targetPin);
		String refName = namedEnumCharacteristicReference.getNamedReference().getReferenceName();
		for (Pin inPin : TransformationUtils.resolveInputPins(inputPinsByName, refName)) {
			if (!assignment.getInputPins().contains(inPin)) {
				assignment.getInputPins().add(inPin);
			}
		}
		TransformationUtils.nameForwardingAssignment(assignment, componentId);
		return assignment;
	}

	// PCM2DFDConverter: processHalfForwardingAssignment
	private static List<AbstractAssignment> processHalfForwardingAssignment(Pin targetPin, Term rightHandSide,
			AbstractNamedReference reference, EnumCharacteristicType characteristicType, DataDictionary dataDictionary,
			Map<String, Pin> inputPinsByName, List<Pin> inPins, String componentId) {
		List<AbstractAssignment> assignments = new ArrayList<>();
		for (Literal value : characteristicType.getType().getLiterals()) {
			Assignment assignment = datadictionaryFactory.eINSTANCE.createAssignment();
			Label label = getOrCreateDFDLabel(dataDictionary, value.getName(), characteristicType);
			assignment.getOutputLabels().add(label);
			assignment.setOutputPin(targetPin);
			org.dataflowanalysis.dfd.datadictionary.Term term = parseTerm(rightHandSide, dataDictionary, label);
			setPins(assignment, term, rightHandSide, inputPinsByName, inPins);
			TransformationUtils.nameAssignment(assignment, componentId);
			assignments.add(assignment);
		}
		return assignments;
	}

	// PCM2DFDConverter: processAssignment
	private static AbstractAssignment processAssignment(Pin targetPin, Term rightHandSide,
			AbstractNamedReference reference, EnumCharacteristicType characteristicType, Literal characteristicValue,
			DataDictionary dataDictionary, Map<String, Pin> inputPinsByName, List<Pin> inPins, String componentId) {
		Assignment assignment = datadictionaryFactory.eINSTANCE.createAssignment();
		Label label = getOrCreateDFDLabel(dataDictionary, characteristicValue.getName(), characteristicType);
		assignment.getOutputLabels().add(label);
		assignment.setOutputPin(targetPin);
		org.dataflowanalysis.dfd.datadictionary.Term term = parseTerm(rightHandSide, dataDictionary);
		setPins(assignment, term, rightHandSide, inputPinsByName, inPins);
		TransformationUtils.nameAssignment(assignment, componentId);
		return assignment;
	}

	// PCM2DFDConverter: setPins (adapted)
	private static void setPins(Assignment assignment, org.dataflowanalysis.dfd.datadictionary.Term term,
			Term rightHandSide, Map<String, Pin> inputPinsByName, List<Pin> inPins) {
		assignment.setTerm(term);
		if (rightHandSide instanceof NamedEnumCharacteristicReference named) {
			String refName = named.getNamedReference().getReferenceName();
			for (Pin inPin : TransformationUtils.resolveInputPins(inputPinsByName, refName)) {
				if (!assignment.getInputPins().contains(inPin)) {
					assignment.getInputPins().add(inPin);
				}
			}
		} else {
			Pin inPin = TransformationUtils.resolveInputPin(inputPinsByName, null);
			if (inPin != null) {
				assignment.getInputPins().add(inPin);
			} else {
				assignment.getInputPins().addAll(inPins);
			}
		}
	}

	// PCM2DFDConverter: parseTerm
	public static org.dataflowanalysis.dfd.datadictionary.Term parseTerm(Term rightHandSide,
			DataDictionary dataDictionary) {
		return parseTerm(rightHandSide, dataDictionary, null);
	}

	// PCM2DFDConverter: parseTerm
	public static org.dataflowanalysis.dfd.datadictionary.Term parseTerm(Term rightHandSide,
			DataDictionary dataDictionary, Label wildcardLabel) {
		if (rightHandSide instanceof True) {
			return datadictionaryFactory.eINSTANCE.createTRUE();
		}
		//
		if (rightHandSide instanceof False) {
			NOT term = datadictionaryFactory.eINSTANCE.createNOT();
			term.setNegatedTerm(datadictionaryFactory.eINSTANCE.createTRUE());
			return term;
		}
		if (rightHandSide instanceof Or or) {
			OR term = datadictionaryFactory.eINSTANCE.createOR();
			term.getTerms().add(parseTerm(or.getLeft(), dataDictionary, wildcardLabel));
			term.getTerms().add(parseTerm(or.getRight(), dataDictionary, wildcardLabel));
			return term;
		}
		if (rightHandSide instanceof And and) {
			AND term = datadictionaryFactory.eINSTANCE.createAND();
			term.getTerms().add(parseTerm(and.getLeft(), dataDictionary, wildcardLabel));
			term.getTerms().add(parseTerm(and.getRight(), dataDictionary, wildcardLabel));
			return term;
		}
		if (rightHandSide instanceof NamedEnumCharacteristicReference characteristicReference) {
			LabelReference term = datadictionaryFactory.eINSTANCE.createLabelReference();
			if (characteristicReference.getCharacteristicType() instanceof EnumCharacteristicType enumType) {
				if (characteristicReference.getLiteral() != null) {
					term.setLabel(getOrCreateDFDLabel(dataDictionary, characteristicReference.getLiteral().getName(),
							enumType));
				} else {
					term.setLabel(wildcardLabel);
				}
			}
			return term;
		}
		throw new IllegalStateException("Unsupported PCM term: " + rightHandSide.getClass().getSimpleName());
	}

	// PCM2DFDConverter: getOrCreateDFDLabel
	private static Label getOrCreateDFDLabel(DataDictionary dataDictionary, String labelName,
			EnumCharacteristicType characteristicType) {
		for (Literal literal : characteristicType.getType().getLiterals()) {
			if (literal.getName().equals(labelName)) {
				return TransformationUtils.getOrCreateLabel(dataDictionary, characteristicType, literal);
			}
		}
		throw new IllegalArgumentException("Unknown label " + labelName + " for type " + characteristicType.getName());
	}
}