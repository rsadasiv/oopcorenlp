package io.outofprintmagazine.nlp.pipeline.serializers;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import io.outofprintmagazine.util.DocumentAggregateScore;

public class MapSerializer implements Serializer {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(MapSerializer.class);

	@SuppressWarnings("rawtypes")
	protected Class annotationClass = null;
	protected Class aggregateClass = null;

	public MapSerializer() {
		super();
	}

	@SuppressWarnings("rawtypes")
	public MapSerializer(Class annotationClass) {
		super();
		this.setAnnotationClass(annotationClass);
	}
	
	@SuppressWarnings("rawtypes")
	public MapSerializer(Class annotationClass, Class aggregateClass) {
		super();
		this.setAnnotationClass(annotationClass);
		this.setAggregateClass(aggregateClass);
	}

	@SuppressWarnings("rawtypes")
	public void setAnnotationClass(Class annotationClass) {
		this.annotationClass = annotationClass;
	}

	@SuppressWarnings("rawtypes")
	public Class getAnnotationClass() {
		return this.annotationClass;
	}
	
	public Class getAggregateClass() {
		return aggregateClass;
	}

	public void setAggregateClass(Class aggregateClass) {
		this.aggregateClass = aggregateClass;
	}

	@Override
	public void serialize(CoreDocument document, ObjectNode json) {
		serializeTokens(document, json);
		serializeSentences(document, json);
		serializeDocument(document, json);
		if (getAggregateClass() != null) {
			serializeDocumentAggregate(document, json);
		}
	}

	@SuppressWarnings("unchecked")
	protected void serializeDocument(CoreDocument coreNlpDocument, ObjectNode jsonDocument) {
		ObjectMapper mapper = new ObjectMapper();

		if (coreNlpDocument.annotation().containsKey(getAnnotationClass())) {
			ObjectNode score = mapper.createObjectNode();
			if (jsonDocument.has(getAnnotationClass().getName())) {
				score = (ObjectNode) jsonDocument.get(getAnnotationClass().getName());
			}
			Map<String, BigDecimal> rawScores = (Map<String, BigDecimal>) coreNlpDocument.annotation().get(getAnnotationClass());
			for (String key : rawScores.keySet()) {
				score.put(key, rawScores.get(key).toPlainString());
			}
			jsonDocument.set(getAnnotationClass().getName(), score);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void serializeDocumentAggregate(CoreDocument coreNlpDocument, ObjectNode jsonDocument) {
		ObjectMapper mapper = new ObjectMapper();
		//trim scores with value 1 ?
		jsonDocument.set(getAggregateClass().getName(), mapper.valueToTree(coreNlpDocument.annotation().get(getAggregateClass())));
	}

	@SuppressWarnings("unchecked")
	protected void serializeSentences(CoreDocument coreNlpDocument, ObjectNode jsonDocument) {
		ObjectMapper mapper = new ObjectMapper();

		Iterator<CoreSentence> coreNlpSentencesIter = coreNlpDocument.sentences().iterator();
		Iterator<JsonNode> jsonSentencesIter = ((ArrayNode) jsonDocument.get("sentences")).iterator();
		while (coreNlpSentencesIter.hasNext() && jsonSentencesIter.hasNext()) {
			CoreSentence sentence = coreNlpSentencesIter.next();
			ObjectNode sentenceNode = (ObjectNode) jsonSentencesIter.next();
			if (sentence.coreMap().containsKey(getAnnotationClass())) {
				ObjectNode score = mapper.createObjectNode();
				if (sentenceNode.has(getAnnotationClass().getName())) {
					score = (ObjectNode) sentenceNode.get(getAnnotationClass().getName());
				}
				Map<String, BigDecimal> rawScores = (Map<String, BigDecimal>) sentence.coreMap().get(getAnnotationClass());
				for (String key : rawScores.keySet()) {
					try {
						score.put(key, rawScores.get(key).toPlainString());
					}
					catch (Throwable t) {
						logger.error(getAnnotationClass());
						logger.error(key, t);
					}
				}
				sentenceNode.set(getAnnotationClass().getName(), score);
			}
		}

	}

	@SuppressWarnings("unchecked")
	protected void serializeTokens(CoreDocument coreNlpDocument, ObjectNode jsonDocument) {
		ObjectMapper mapper = new ObjectMapper();

		Iterator<CoreSentence> coreNlpSentencesIter = coreNlpDocument.sentences().iterator();
		Iterator<JsonNode> jsonSentencesIter = ((ArrayNode) jsonDocument.get("sentences")).iterator();
		while (coreNlpSentencesIter.hasNext() && jsonSentencesIter.hasNext()) {
			Iterator<CoreLabel> coreNlpTokensIter = coreNlpSentencesIter.next().tokens().iterator();
			Iterator<JsonNode> jsonTokensIter = ((ArrayNode) jsonSentencesIter.next().get("tokens")).iterator();
			while (coreNlpTokensIter.hasNext() && jsonTokensIter.hasNext()) {
				CoreLabel token = coreNlpTokensIter.next();
				ObjectNode tokenNode = (ObjectNode) jsonTokensIter.next();
				if (token.containsKey(getAnnotationClass())) {
					ObjectNode score = mapper.createObjectNode();
					if (tokenNode.has(getAnnotationClass().getName())) {
						score = (ObjectNode) tokenNode.get(getAnnotationClass().getName());
					}
					Map<String, BigDecimal> rawScores = (Map<String, BigDecimal>) token.get(getAnnotationClass());
					for (String key : rawScores.keySet()) {
						//someone has MapSerializer where they should have ListSerializer
						//Exception in thread "main" java.lang.ClassCastException: java.util.Arrays$ArrayList cannot be cast to java.math.BigDecimal
						try {
							score.put(key, rawScores.get(key).toPlainString());
						}
						catch (Throwable t) {
							logger.error(getAnnotationClass());
							logger.error(key, t);
						}
					}
					tokenNode.set(getAnnotationClass().getName(), score);
				}
			}
		}

	}

}
