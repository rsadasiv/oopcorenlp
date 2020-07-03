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

public class PronounAnnotator extends AbstractPosAnnotator implements Annotator, OOPAnnotator{
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(PronounAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}

	public PronounAnnotator() {
		super();
		this.setTags(Arrays.asList("PRP"));
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass()));
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPPronounAnnotation.class;
	}
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (int sentenceIdx = 0; sentenceIdx < document.sentences().size(); sentenceIdx++) {
			CoreSentence sentence = document.sentences().get(sentenceIdx);
			List<CoreLabel> tokens = sentence.tokens();
			for (int i = 0; i < tokens.size(); i++) {
				CoreLabel token = tokens.get(i);
				Map<String, BigDecimal> scoreMap = new HashMap<String, BigDecimal>();
				if (!token.lemma().endsWith("self") && !token.lemma().endsWith("selves") && !token.lemma().equals("'s")) {
					String score = scoreLemma(token);
					if (score != null) {
						addToScoreMap(scoreMap, score, new BigDecimal(1));
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
		return "PRP minus -self, -selves, -'s";
	}

}
