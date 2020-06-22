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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.mit.jwi.item.ISenseKey;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;
import io.outofprintmagazine.nlp.utils.WordnetUtils;

public class VerbnetGroupsAnnotator extends AbstractPosAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(VerbnetGroupsAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}
	
	private List<String> posTags = Arrays.asList("VB","VBD","VBG","VBN","VBP","VBZ");
	
	public VerbnetGroupsAnnotator() {
		super();
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass()));
		this.appendTagsFromFile("io/outofprintmagazine/nlp/models/StativeVerbs.txt");
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbnetGroupsAnnotation.class;
	}

	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (int i=0;i<document.sentences().size();i++) {
			CoreSentence sentence = document.sentences().get(i);			
			for (CoreLabel token : sentence.tokens()) {
				if (posTags.contains(token.tag())) {
					if (!getTags().contains(token.lemma().toLowerCase())) {
						if (!token.lemma().startsWith("'")) {
							try {
								ISenseKey senseKey = WordnetUtils.getInstance(getParameterStore()).getTargetWordSenseKey(token, getContextWords(document, token));
								if (senseKey != null) {
									//logger.debug("getting verbnet senses: " + senseKey);
									List<String> senses = WordnetUtils.getInstance(getParameterStore()).getVerbnetSenses(senseKey);
									if (senses != null) {
										//logger.debug("senses length: " + senses.size());
										Map<String,BigDecimal> scoreMap = new HashMap<String,BigDecimal>();
										for (String sense : senses) {
											addToScoreMap(scoreMap, sense.split("-")[0], new BigDecimal(1));
										}
										token.set(getAnnotationClass(), scoreMap);
									}
								}
							} 
							catch (IOException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
								logger.error(e);
							}
						}
					}
				}
			}
		}
		score(document);
	}

	@Override
	public String getDescription() {
		return "Verbnet senses. https://uvi.colorado.edu/";
	}
}
