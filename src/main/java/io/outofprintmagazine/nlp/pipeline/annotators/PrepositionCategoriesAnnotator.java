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
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.IScorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.ISerializer;

public class PrepositionCategoriesAnnotator extends AbstractPosAnnotator implements Annotator, IOOPAnnotator{
	
	private static final Logger logger = LogManager.getLogger(PrepositionCategoriesAnnotator.class);
	
	@SuppressWarnings("unused")
	private Logger getLogger() {
		return logger;
	}
	
	private Map<String,String> scoreLabelMap = new HashMap<String,String>();
	
	public PrepositionCategoriesAnnotator() {
		super();
		this.setTags(Arrays.asList("IN", "CC"));
		initScoreLabelMap();
		this.setScorer((IScorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((ISerializer)new MapSerializer(this.getAnnotationClass()));	
	}
	
	protected void initScoreLabelMap() {
		scoreLabelMap.put("IN", "subordinating");
		scoreLabelMap.put("CC", "coordinating");
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPPrepositionCategoriesAnnotation.class;
	}

	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				Map<String, BigDecimal> scoreMap = new HashMap<String, BigDecimal>();
				String score = scoreTag(token);
				if (score != null) {
					addToScoreMap(scoreMap, scoreLabelMap.get(score), new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	
	@Override
	public String getDescription() {
		return "IN, CC. subordinating, coordinating.";
	}
}
