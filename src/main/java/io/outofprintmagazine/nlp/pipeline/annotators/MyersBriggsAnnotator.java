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
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.IScorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.ISerializer;

public class MyersBriggsAnnotator extends AbstractPosAnnotator implements Annotator, IOOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(MyersBriggsAnnotator.class);
	
	@SuppressWarnings("unused")
	private Logger getLogger() {
		return logger;
	}
	
	//VerbGroups
	private String extrovert = "social";
	private String introvert = "perception";
	private String sensing = "stative";
	private String intuitive = "change";
	private String thinking = "cognition";
	private String feeling = "emotion";
	//Biber features
	private String judging = "PRMD";
	private String perceiving = "NEMD";
	
	public MyersBriggsAnnotator() {
		super();
		this.setScorer((IScorer) new MapSum(this.getAnnotationClass()));
		this.setSerializer((ISerializer)new MapSerializer(this.getAnnotationClass()));
	}
	
	@Override
	public String getDescription() {
		return "Personality types. https://www.myersbriggs.org/my-mbti-personality-type/mbti-basics/";
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPMyersBriggsAnnotation.class;
	}
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			Map<String,BigDecimal> scoreMap = new HashMap<String,BigDecimal>();
			if (sentence.coreMap().containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbGroupsAnnotation.class)) {
				Map<String,BigDecimal> annotationScore = sentence.coreMap().get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbGroupsAnnotation.class);
				if (annotationScore.containsKey(extrovert)) {
					addToScoreMap(scoreMap, "extrovert", annotationScore.get(extrovert));
				}
				if (annotationScore.containsKey(introvert)) {
					addToScoreMap(scoreMap, "introvert", annotationScore.get(introvert));
				}
				if (annotationScore.containsKey(sensing)) {
					addToScoreMap(scoreMap, "sensing", annotationScore.get(sensing));
				}
				if (annotationScore.containsKey(intuitive)) {
					addToScoreMap(scoreMap, "intuitive", annotationScore.get(intuitive));
				}
				if (annotationScore.containsKey(thinking)) {
					addToScoreMap(scoreMap, "thinking", annotationScore.get(thinking));
				}
				if (annotationScore.containsKey(feeling)) {
					addToScoreMap(scoreMap, "feeling", annotationScore.get(feeling));
				}
			}
			if (sentence.coreMap().containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPBiberAnnotation.class)) {
				Map<String, BigDecimal> biberScoreMap = sentence.coreMap().get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPBiberAnnotation.class);
				if (biberScoreMap.containsKey(judging)) {
					addToScoreMap(scoreMap, "judging", biberScoreMap.get(judging));
				}
				if (biberScoreMap.containsKey(perceiving)) {
					addToScoreMap(scoreMap, "perceiving", biberScoreMap.get(perceiving));
				}
			}
			sentence.coreMap().set(getAnnotationClass(), scoreMap);
		}
	}
	
}
