package io.outofprintmagazine.nlp.pipeline.serializers;

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

public class StringSerializer implements Serializer {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(StringSerializer.class);

	private Class annotationClass;

	public StringSerializer() {
		super();
	}

	public StringSerializer(Class annotationClass) {
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
			String rawScore = (String) coreNlpDocument.annotation().get(getAnnotationClass());
			jsonDocument.put(getAnnotationClass().getSimpleName(), rawScore);
		}
	}

	protected void serializeSentences(CoreDocument coreNlpDocument, ObjectNode jsonDocument) {
	Iterator<CoreSentence> coreNlpSentencesIter = coreNlpDocument.sentences().iterator();
		Iterator<JsonNode> jsonSentencesIter = ((ArrayNode) jsonDocument.get("sentences")).iterator();
		while (coreNlpSentencesIter.hasNext() && jsonSentencesIter.hasNext()) {
			CoreSentence sentence = coreNlpSentencesIter.next();
			ObjectNode sentenceNode = (ObjectNode) jsonSentencesIter.next();
			if (sentence.coreMap().containsKey(getAnnotationClass())) {
				String rawScore = (String) sentence.coreMap().get(getAnnotationClass());
				sentenceNode.put(getAnnotationClass().getSimpleName(), rawScore);
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
					String rawScore = (String) token.get(getAnnotationClass());
					tokenNode.put(getAnnotationClass().getSimpleName(), rawScore);
				}
			}
		}
	}

	@Override
	public void serializeAggregate(Object aggregate, ObjectNode json) {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode value = mapper.valueToTree(aggregate);
		if (value != null && !value.isNull()) {
			json.set(getAnnotationClass().getSimpleName(), value);
		}
	}

}
