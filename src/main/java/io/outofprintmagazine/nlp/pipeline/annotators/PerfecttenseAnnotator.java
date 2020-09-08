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
package io.outofprintmagazine.nlp.pipeline.annotators;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import io.outofprintmagazine.nlp.pipeline.scorers.IScorer;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.serializers.ISerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.utils.PerfecttenseUtils;

public class PerfecttenseAnnotator extends AbstractPosAnnotator implements Annotator, IOOPAnnotator{
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(PerfecttenseAnnotator.class);
	
	@SuppressWarnings("unused")
	private Logger getLogger() {
		return logger;
	}
	
	public PerfecttenseAnnotator() {
		super();
		this.setScorer((IScorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((ISerializer)new MapSerializer(this.getAnnotationClass()));
	}
	
	@Override
	public String getDescription() {
		return "https://www.perfecttense.com/docs/";
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.PerfecttenseAnnotation.class;
	}
	
	public Class getGlossClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.PerfecttenseGlossAnnotation.class;
	}
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		try {
			//String response = PerfecttenseUtils.getInstance(getParameterStore()).correct(document.text(), new ArrayList<String>());
			//ObjectMapper mapper = new ObjectMapper();
	        //JsonNode corrected = mapper.readTree(response);
			JsonNode corrected = PerfecttenseUtils.getInstance(getParameterStore()).correctJson(document.text(), new ArrayList<String>());
	        ArrayNode offset = (ArrayNode) corrected.get("offset");
	        Iterator<CoreSentence> sentenceIter = document.sentences().iterator();
	        Iterator<JsonNode> correctedSentenceIter = offset.iterator();
	        while (sentenceIter.hasNext() && correctedSentenceIter.hasNext()) {
	        	CoreSentence sentence = sentenceIter.next();
				JsonNode correctedSentence = correctedSentenceIter.next();
				//what happens if the sentencing gets out of sync?
				if (sentence.text().trim().equals(correctedSentence.get("originalSentence").asText().trim())) {
					ArrayNode corrections = (ArrayNode)correctedSentence.get("corrections");
					if (corrections.size() > 0) {
						for (JsonNode correction : corrections) {
							int correctionOffset = correction.get("offsetInSentence").asInt();
							for (CoreLabel token : sentence.tokens()) {
								//what happens if these get out of sync?
								if (token.beginPosition() >= correctionOffset) {
									String glosses = null;
									Map<String, BigDecimal> scoreMap = new HashMap<String, BigDecimal>();
									for (JsonNode option : correction.get("options")) {
										for (JsonNode appliedRule : option.get("appliedRules")) {
											if (glosses == null) {
												glosses = appliedRule.get("message").asText();
											}
											else {
												glosses = glosses + "/n" + appliedRule.get("message").asText();
											}
											BigDecimal score = new BigDecimal(1);
											if (option.get("isSuggestion").asBoolean(true)) {
												score = new BigDecimal(.5);
											}
											scoreMap.put(appliedRule.get("ruleId").asText().replace('.', '_'), score);
										}
									}
									token.set(getGlossClass(), glosses);
									token.set(getAnnotationClass(), scoreMap);
									break;
								}
							}
						}
					}
				}
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
	}
	
	@Override
	public void serialize(CoreDocument document, ObjectNode json) {
		getSerializer().serialize(document, json);
		serializeTokensGloss(document, json);
	}
	
	@SuppressWarnings("unchecked")
	protected void serializeTokensGloss(CoreDocument coreNlpDocument, ObjectNode jsonDocument) {
		Iterator<CoreSentence> coreNlpSentencesIter = coreNlpDocument.sentences().iterator();
		Iterator<JsonNode> jsonSentencesIter = ((ArrayNode) jsonDocument.get("sentences")).iterator();
		while (coreNlpSentencesIter.hasNext() && jsonSentencesIter.hasNext()) {
			Iterator<CoreLabel> coreNlpTokensIter = coreNlpSentencesIter.next().tokens().iterator();
			Iterator<JsonNode> jsonTokensIter = ((ArrayNode) jsonSentencesIter.next().get("tokens")).iterator();
			while (coreNlpTokensIter.hasNext() && jsonTokensIter.hasNext()) {
				CoreLabel token = coreNlpTokensIter.next();
				ObjectNode tokenNode = (ObjectNode) jsonTokensIter.next();
				if (token.containsKey(getGlossClass())) {
					tokenNode.put(getGlossClass().getSimpleName(), (String)token.get(getGlossClass()));
				}
			}
		}
	}
}
