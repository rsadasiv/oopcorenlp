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
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import io.outofprintmagazine.util.AggregatedScore;
import io.outofprintmagazine.util.DocumentAggregateScore;

public class BigDecimalAvg extends BigDecimalScorer implements IScorer {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(BigDecimalAvg.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}

	public BigDecimalAvg() {
		super();
	}

	public BigDecimalAvg(@SuppressWarnings("rawtypes") Class annotationClass) {
		super();
		this.setAnnotationClass(annotationClass);
	}

	@Override
	public BigDecimal aggregateScores(List<BigDecimal> allScores) {
		BigDecimal score = new BigDecimal(0);
		for (BigDecimal rawScore : allScores) {
			score = score.add(rawScore);
		}
		if (allScores.size() > 0) {
			return score.divide(new BigDecimal(allScores.size()), 10, RoundingMode.HALF_DOWN);
		}
		else {
			return score;
		}
	}
	
	@Override
	public Object aggregateDocument(CoreDocument document) {
		return aggregateDocument(document, getDocumentAggregatableScores(document));
	}
	
	public Object aggregateDocument(CoreDocument document, Map<String, BigDecimal> rawScores) {
		DocumentAggregateScore retval = null;
		if (document.annotation().containsKey(getAnnotationClass())) {
			retval = new DocumentAggregateScore();
			retval.setName(getAnnotationClass().getSimpleName());
			BigDecimal normalizer = new BigDecimal(document.tokens().size());
			if (rawScores.size() == 0) {
				return retval;
			}
			double[] primitiveNormalizedScores = new double[rawScores.size()];
			BigDecimal rawSum = new BigDecimal(0);
			Iterator<Entry<String,BigDecimal>> rawScoresIter = rawScores.entrySet().iterator();

			for (int i=0; i<rawScores.size() && rawScoresIter.hasNext(); i++) {
				Entry<String,BigDecimal> rawScore = rawScoresIter.next();
				rawSum = rawSum.add(rawScore.getValue());
				primitiveNormalizedScores[i] = rawScore.getValue().doubleValue();
				retval.getAggregatedScores().add(new AggregatedScore(rawScore.getKey(), rawScore.getValue(), normalizer, rawScore.getValue()));
			}
			retval.getScoreStats().getScore().setRaw(rawSum);
			retval.getScoreStats().getScore().setNormalized(rawSum.divide(normalizer, 10, BigDecimal.ROUND_HALF_UP));
			retval.getScoreStats().getScore().setCount(new BigDecimal(rawScores.size()));
			retval.getScoreStats().getStats().setStddev(new BigDecimal(new StandardDeviation().evaluate(primitiveNormalizedScores)));
			retval.getScoreStats().getStats().setMean(retval.getScoreStats().getScore().getRaw().divide(new BigDecimal(rawScores.size()), 10, BigDecimal.ROUND_HALF_UP));
			
			Collections.sort(retval.getAggregatedScores());
			Iterator<AggregatedScore> aggregatedScoresIter = retval.getAggregatedScores().iterator();
			for (int i=0; i<retval.getAggregatedScores().size() && aggregatedScoresIter.hasNext(); i++) {
				AggregatedScore aggregatedScore = aggregatedScoresIter.next();
				if (aggregatedScore.getScore().getRaw().compareTo(new BigDecimal(0)) > 0) {
					aggregatedScore.getAggregateScore().setPercentage(aggregatedScore.getScore().getRaw().divide(retval.getScoreStats().getScore().getRaw(), 10, BigDecimal.ROUND_HALF_UP));
				}
				else {
					aggregatedScore.getAggregateScore().setPercentage(new BigDecimal(0));
				}
				if (retval.getScoreStats().getStats().getStddev().compareTo(new BigDecimal(0)) != 0) {
					aggregatedScore.getAggregateScore().setZ(aggregatedScore.getScore().getRaw().subtract(retval.getScoreStats().getStats().getMean()).divide(retval.getScoreStats().getStats().getStddev(), 10, BigDecimal.ROUND_HALF_UP));
				}
				else {
					aggregatedScore.getAggregateScore().setZ(new BigDecimal(0));
				}
				aggregatedScore.getAggregateScore().setRank(new BigDecimal(i));
				aggregatedScore.getAggregateScore().setPercentile(new BigDecimal(1).subtract(new BigDecimal(i).divide(new BigDecimal(retval.getAggregatedScores().size()), 10, BigDecimal.ROUND_HALF_UP)));
				if (retval.getScoreStats().getStats().getMin().equals(new BigDecimal(0)) || retval.getScoreStats().getStats().getMin().compareTo(aggregatedScore.getScore().getRaw()) > 0) {
					retval.getScoreStats().getStats().setMin(aggregatedScore.getScore().getRaw());
					
				}
				if (retval.getScoreStats().getStats().getMax().compareTo(aggregatedScore.getScore().getRaw()) < 0) {
					retval.getScoreStats().getStats().setMax(aggregatedScore.getScore().getRaw());
				}
				if (retval.getScoreStats().getStats().getMedian().equals(new BigDecimal(0)) && (i > retval.getAggregatedScores().size()/2)) {
					retval.getScoreStats().getStats().setMedian(aggregatedScore.getScore().getRaw());
				}
			}
		}
		return retval;
	}

	@Override
	public Map<String, BigDecimal> getDocumentAggregatableScores(CoreDocument document) {
		Map<String, BigDecimal> rawScores = new HashMap<String, BigDecimal>();
		for (int i=0;i<document.sentences().size();i++) {
			CoreSentence sentence = document.sentences().get(i);
			if (sentence.coreMap().containsKey(getAnnotationClass())) {
				rawScores.put(Integer.toString(i), (BigDecimal) sentence.coreMap().get(getAnnotationClass()));
			}
		}
		return rawScores;
	}


}
