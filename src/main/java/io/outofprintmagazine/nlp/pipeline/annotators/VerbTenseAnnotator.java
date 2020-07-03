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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public class VerbTenseAnnotator extends AbstractPosAnnotator implements Annotator, OOPAnnotator{
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(VerbTenseAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}

	private List<String> past = Arrays.asList("VBD","VBN");
	private List<String> present = Arrays.asList("VB","VBP","VBZ");
	private List<String> future = Arrays.asList("MD");
	private List<String> progressive = Arrays.asList("VBG");
	private List<String> infinitive = Arrays.asList("TO");
	
	public VerbTenseAnnotator() {
		super();
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass()));
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbTenseAnnotation.class;
	}

	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (int i=0;i<sentence.tokens().size();i++) {
				CoreLabel token = sentence.tokens().get(i);
				Map<String, BigDecimal> scoreMap = new HashMap<String, BigDecimal>();
				if (scoreLemma(token, past) != null) {
					addToScoreMap(scoreMap, "past", new BigDecimal(1));
				}
				else if (scoreLemma(token, present) != null) {
					addToScoreMap(scoreMap, "present", new BigDecimal(1));
				}
				else if (scoreLemma(token, progressive) != null) {
					addToScoreMap(scoreMap, "progressive", new BigDecimal(1));
				}
				else if (scoreLemma(token, future) != null) {
					addToScoreMap(scoreMap, "future", new BigDecimal(1));
				}
				else if (scoreLemma(token, infinitive) != null) {
					if (i < sentence.tokens().size() && sentence.tokens().get(i).tag().equals("VB")) {
						addToScoreMap(scoreMap, "infinitive", new BigDecimal(1));
					}
				}
				if (scoreMap.size() > 0) {
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}

	@Override
	public String getDescription() {
		return "Verb tenses. past: VBD,VBN " + 
				"present: VB,VBP,VBZ " + 
				"future: MD " + 
				"progressive: VBG " + 
				"infinitive: TO";
	}
}
