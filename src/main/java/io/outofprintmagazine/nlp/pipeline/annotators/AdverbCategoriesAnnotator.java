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

public class AdverbCategoriesAnnotator extends AbstractPosAnnotator implements Annotator, OOPAnnotator{
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(AdverbCategoriesAnnotator.class);
	
	@SuppressWarnings("unused")
	private Logger getLogger() {
		return logger;
	}

	private List<String> descriptive = Arrays.asList("RB");
	private List<String> comparative = Arrays.asList("RBR");
	private List<String> superlative = Arrays.asList("RBS");
	
	public AdverbCategoriesAnnotator() {
		super();
		this.appendTagsFromFile("io/outofprintmagazine/nlp/models/Adverbs.txt");
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass()));			
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdverbCategoriesAnnotation.class;
	}
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (!getTags().contains(token.lemma().toLowerCase())) {
					Map<String, BigDecimal> scoreMap = new HashMap<String, BigDecimal>();
					if (scoreLemma(token, descriptive) != null) {
						addToScoreMap(scoreMap, "descriptive", new BigDecimal(1));
					}
					else if (scoreLemma(token, comparative) != null) {
						addToScoreMap(scoreMap, "comparative", new BigDecimal(1));
					}
					else if (scoreLemma(token, superlative) != null) {
						addToScoreMap(scoreMap, "superlative", new BigDecimal(1));
					}
					if (scoreMap.size() > 0) {
						token.set(getAnnotationClass(), scoreMap);
					}
				}
			}
		}
	}

	@Override
	public String getDescription() {
		return "Fast-Faster-Fasest. RB, RBR, RBS: descriptive, comparative, superlative.";
	}

}
