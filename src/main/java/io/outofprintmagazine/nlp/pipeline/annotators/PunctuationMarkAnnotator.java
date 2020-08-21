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

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.IScorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.ISerializer;

public class PunctuationMarkAnnotator extends AbstractPosAnnotator implements Annotator, IOOPAnnotator{
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(PunctuationMarkAnnotator.class);
	
	@SuppressWarnings("unused")
	private Logger getLogger() {
		return logger;
	}

	private Map<String,String> scoreLabelMap = new HashMap<String,String>();
	
	public PunctuationMarkAnnotator() {
		super();
		this.initScoreLabelMap();
		this.setScorer((IScorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((ISerializer)new MapSerializer(this.getAnnotationClass()));
	}
	
	protected void initScoreLabelMap() {
		scoreLabelMap.put("?", "Question");
		scoreLabelMap.put("??", "Question");
		scoreLabelMap.put("!", "Exclamation");
		scoreLabelMap.put(":", "Colon");
		scoreLabelMap.put(";", "Semicolon");
		scoreLabelMap.put(",", "Comma");
		scoreLabelMap.put("--", "Hyphen");
		scoreLabelMap.put("-", "Hyphen");
		scoreLabelMap.put(".", "Period");
		scoreLabelMap.put("\'", "Apostrophe");
		scoreLabelMap.put("\"", "Quotation");
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPPunctuationMarkAnnotation.class;
	}

	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				Map<String, BigDecimal> scoreMap = new HashMap<String, BigDecimal>();
				if (isPunctuationMark(token)) {
					if (scoreLabelMap.get(token.originalText()) != null) {
						addToScoreMap(scoreMap, scoreLabelMap.get(token.originalText()), new BigDecimal(1));
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
		return "Question, Exclamation, Colon, Semicolon, Comma, Hypen, Period, Apostrophe, Quotation";
	}
}
