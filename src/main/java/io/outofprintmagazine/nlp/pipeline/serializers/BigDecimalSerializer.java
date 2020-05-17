package io.outofprintmagazine.nlp.pipeline.serializers;

import java.math.BigDecimal;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;

public class BigDecimalSerializer implements Serializer {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(BigDecimalSerializer.class);

	private Class annotationClass;

	public BigDecimalSerializer() {
		super();
	}

	public BigDecimalSerializer(Class annotationClass) {
		super();
		this.setAnnotationClass(annotationClass);
	}

	public void setAnnotationClass(Class annotationClass) {
		this.annotationClass = annotationClass;
	}

	public Class getAnnotationClass() {
		return this.annotationClass;
	}
	
	@Override
	public void serialize(CoreDocument document, ObjectNode json) {
		serializeTokens(document, json);
		serializeSentences(document, json);
		serializeDocument(document, json);
	}

	@SuppressWarnings("unchecked")
	protected void serializeDocument(CoreDocument coreNlpDocument, ObjectNode jsonDocument) {
		if (coreNlpDocument.annotation().containsKey(getAnnotationClass())) {
			BigDecimal rawScore = (BigDecimal) coreNlpDocument.annotation().get(getAnnotationClass());
			jsonDocument.put(getAnnotationClass().getSimpleName(), rawScore.toPlainString());
		}
	}

	protected void serializeSentences(CoreDocument coreNlpDocument, ObjectNode jsonDocument) {
	Iterator<CoreSentence> coreNlpSentencesIter = coreNlpDocument.sentences().iterator();
		Iterator<JsonNode> jsonSentencesIter = ((ArrayNode) jsonDocument.get("sentences")).iterator();
		while (coreNlpSentencesIter.hasNext() && jsonSentencesIter.hasNext()) {
			CoreSentence sentence = coreNlpSentencesIter.next();
			ObjectNode sentenceNode = (ObjectNode) jsonSentencesIter.next();
			if (sentence.coreMap().containsKey(getAnnotationClass())) {
				BigDecimal rawScore = (BigDecimal) sentence.coreMap().get(getAnnotationClass());
				sentenceNode.put(getAnnotationClass().getSimpleName(), rawScore.toPlainString());
			}
		}
	}

	protected void serializeTokens(CoreDocument coreNlpDocument, ObjectNode jsonDocument) {
		Iterator<CoreSentence> coreNlpSentencesIter = coreNlpDocument.sentences().iterator();
		Iterator<JsonNode> jsonSentencesIter = ((ArrayNode) jsonDocument.get("sentences")).iterator();
		while (coreNlpSentencesIter.hasNext() && jsonSentencesIter.hasNext()) {
			Iterator<CoreLabel> coreNlpTokensIter = coreNlpSentencesIter.next().tokens().iterator();
			Iterator<JsonNode> jsonTokensIter = ((ArrayNode) jsonSentencesIter.next().get("tokens")).iterator();
			while (coreNlpTokensIter.hasNext() && jsonTokensIter.hasNext()) {
				CoreLabel token = coreNlpTokensIter.next();
				ObjectNode tokenNode = (ObjectNode) jsonTokensIter.next();
				if (token.containsKey(getAnnotationClass())) {
					BigDecimal rawScore = (BigDecimal) token.get(getAnnotationClass());
					tokenNode.put(getAnnotationClass().getSimpleName(), rawScore.toPlainString());
				}
			}
		}
	}

	@Override
	public void serializeAggregate(Object aggregate, ObjectNode json) {
		ObjectMapper mapper = new ObjectMapper();
		json.set(getAnnotationClass().getSimpleName(), mapper.valueToTree(aggregate));
		
	}

}
