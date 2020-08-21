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
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;

public class BigDecimalSumSentenceScorer extends BigDecimalSum implements IScorer {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(BigDecimalSumSentenceScorer.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}

	public BigDecimalSumSentenceScorer() {
		super();
	}

	public BigDecimalSumSentenceScorer(Class annotationClass) {
		super(annotationClass);
	}

	@Override
	public Map<String, BigDecimal> getDocumentAggregatableScores(CoreDocument document) {
		Map<String, BigDecimal> rawScores = new HashMap<String, BigDecimal>();
		for (int i=0;i<document.sentences().size();i++) {
			CoreSentence sentence = document.sentences().get(i);
			if (sentence.coreMap().containsKey(getAnnotationClass())) {
				rawScores.put(Integer.toString(i), (BigDecimal) sentence.coreMap().get(getAnnotationClass()));
			}
			else {
				rawScores.put(Integer.toString(i), new BigDecimal(0));
			}
		}
		return rawScores;
	}

}
