package io.outofprintmagazine.nlp.pipeline.serializers;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;

public class MapListSerializer implements Serializer {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(MapListSerializer.class);

	@SuppressWarnings("rawtypes")
	private Class annotationClass;

	public MapListSerializer() {
		super();
	}

	@SuppressWarnings("rawtypes")
	public MapListSerializer(Class annotationClass) {
		super();
		this.setAnnotationClass(annotationClass);
	}

	@SuppressWarnings("rawtypes")
	public void setAnnotationClass(Class annotationClass) {
		this.annotationClass = annotationClass;
	}

	@SuppressWarnings("rawtypes")
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
		ObjectMapper mapper = new ObjectMapper();

		if (coreNlpDocument.annotation().containsKey(getAnnotationClass())) {
			ObjectNode score = mapper.createObjectNode();
			if (jsonDocument.has(getAnnotationClass().getName())) {
				score = (ObjectNode) jsonDocument.get(getAnnotationClass().getName());
			}
			Map<String, List<String>> rawScores = (Map<String, List<String>>) coreNlpDocument.annotation().get(getAnnotationClass());
			for (String key : rawScores.keySet()) {
				ArrayNode scoreList = score.putArray(key);
				for (String scoreValue : rawScores.get(key)) {
					scoreList.add(scoreValue);
				}
			}
			jsonDocument.set(getAnnotationClass().getName(), score);
		}

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
				Map<String, List<String>> rawScores = (Map<String, List<String>>) sentence.coreMap().get(getAnnotationClass());
				for (String key : rawScores.keySet()) {
					ArrayNode scoreList = score.putArray(key);
					for (String scoreValue : rawScores.get(key)) {
						scoreList.add(scoreValue);
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
					Map<String, List<String>> rawScores = (Map<String, List<String>>) token.get(getAnnotationClass());
					for (String key : rawScores.keySet()) {
						ArrayNode scoreList = score.putArray(key);
						for (String scoreValue : rawScores.get(key)) {
							scoreList.add(scoreValue);
						}
					}
					tokenNode.set(getAnnotationClass().getName(), score);
				}
			}
		}

	}

}
