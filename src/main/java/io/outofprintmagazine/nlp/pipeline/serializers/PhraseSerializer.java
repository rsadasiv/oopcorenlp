/*******************************************************************************
 * Copyright (C) 2020 Ram Sadasiv
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package io.outofprintmagazine.nlp.pipeline.serializers;

import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import io.outofprintmagazine.nlp.pipeline.PhraseAnnotation;

public class PhraseSerializer implements Serializer {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(PhraseSerializer.class);
	
	protected Logger getLogger() {
		return logger;
	}

	@SuppressWarnings("rawtypes")
	private Class annotationClass;

	public PhraseSerializer() {
		super();
	}

	@SuppressWarnings("rawtypes")
	public PhraseSerializer(Class annotationClass) {
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
			List<PhraseAnnotation> rawScores = (List<PhraseAnnotation>) coreNlpDocument.annotation().get(getAnnotationClass());
			jsonDocument.set(getAnnotationClass().getSimpleName(), mapper.valueToTree(rawScores));
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
				List<PhraseAnnotation> rawScores = (List<PhraseAnnotation>) sentence.coreMap().get(getAnnotationClass());
				sentenceNode.set(getAnnotationClass().getSimpleName(), mapper.valueToTree(rawScores));
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
					List<PhraseAnnotation> rawScores = (List<PhraseAnnotation>) token.get(getAnnotationClass());
					tokenNode.set(getAnnotationClass().getSimpleName(), mapper.valueToTree(rawScores));
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
