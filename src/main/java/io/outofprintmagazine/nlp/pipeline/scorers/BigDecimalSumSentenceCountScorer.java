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

import edu.stanford.nlp.ling.CoreAnnotations.ParagraphIndexAnnotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;

public class BigDecimalSumSentenceCountScorer extends BigDecimalSum implements IScorer {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(BigDecimalSumSentenceCountScorer.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}

	public BigDecimalSumSentenceCountScorer() {
		super();
	}

	public BigDecimalSumSentenceCountScorer(Class annotationClass) {
		super(annotationClass);
	}

	@Override
	public Map<String, BigDecimal> getDocumentAggregatableScores(CoreDocument document) {
		int paragraphIdx = 0;
		int sentenceCount = -1;
		Map<String, BigDecimal> rawScores = new HashMap<String, BigDecimal>();
		for (int i=0;i<document.sentences().size();i++) {
			CoreSentence sentence = document.sentences().get(i);
			if (sentence.coreMap().containsKey(ParagraphIndexAnnotation.class)) {
				if (paragraphIdx < sentence.coreMap().get(ParagraphIndexAnnotation.class).intValue()) {
					rawScores.put(Integer.valueOf(paragraphIdx).toString(), new BigDecimal(++sentenceCount));

					sentenceCount = 0;
					paragraphIdx++;
				}
				else {
					++sentenceCount;
				}
			}
		}
		rawScores.put(Integer.valueOf(paragraphIdx).toString(), new BigDecimal(++sentenceCount));
		return rawScores;
	}

}
