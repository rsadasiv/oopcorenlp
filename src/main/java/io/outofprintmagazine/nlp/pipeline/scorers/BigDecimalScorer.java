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
package io.outofprintmagazine.nlp.pipeline.scorers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;

public abstract class BigDecimalScorer implements IScorer {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(BigDecimalScorer.class);
	
	protected Logger getLogger() {
		return logger;
	}

	protected Class annotationClass = null;


	public BigDecimalScorer() {
		super();
	}

	public BigDecimalScorer(@SuppressWarnings("rawtypes") Class annotationClass) {
		super();
		this.setAnnotationClass(annotationClass);
	}


	public void setAnnotationClass(Class annotationClass) {
		this.annotationClass = annotationClass;
	}

	public Class getAnnotationClass() {
		return this.annotationClass;
	}
	
	@Override
	public void score(CoreDocument document) {
		scoreTokens(document);
		scoreSentences(document);
		scoreDocument(document);
	}

	public void scoreTokens(CoreDocument document) {
		// pass
	}

	public void scoreSentences(CoreDocument document) {
		for (CoreSentence sentence : document.sentences()) {
			ArrayList<BigDecimal> rawScores = new ArrayList<BigDecimal>();
			for (CoreLabel token : sentence.tokens()) {
				if (token.containsKey(getAnnotationClass())) {
					rawScores.add((BigDecimal) token.get(getAnnotationClass()));
				}
			}
			if (rawScores.size() > 0) {
				sentence.coreMap().set(getAnnotationClass(), aggregateScores(rawScores));
			}

		}
	}

	public void scoreDocument(CoreDocument document) {
		if (! document.annotation().containsKey(getAnnotationClass())) {
			ArrayList<BigDecimal> rawScores = new ArrayList<BigDecimal>();
			for (CoreSentence sentence : document.sentences()) {
				if (sentence.coreMap().containsKey(getAnnotationClass())) {
					rawScores.add((BigDecimal) sentence.coreMap().get(getAnnotationClass()));
				}
				else {
					for (CoreLabel token : sentence.tokens()) {
						if (token.containsKey(getAnnotationClass())) {
							rawScores.add((BigDecimal) token.get(getAnnotationClass()));
						}
					}
				}
			}
			document.annotation().set(getAnnotationClass(), aggregateScores(rawScores));
		}
	}
	
	@Override
	public abstract Object aggregateDocument(CoreDocument document);
	
	public abstract Map<String, BigDecimal> getDocumentAggregatableScores(CoreDocument document);
	
	public abstract BigDecimal aggregateScores(List<BigDecimal> allScores);
}
