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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import io.outofprintmagazine.util.AggregatedScore;
import io.outofprintmagazine.util.DocumentAggregateScore;

public abstract class MapScorer implements IScorer {

	private static final Logger logger = LogManager.getLogger(MapScorer.class);

	private Logger getLogger() {
		return logger;
	}
	
	protected Class annotationClass = null;

	public MapScorer() {
		super();
	}

	public MapScorer(Class annotationClass) {
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
		//TODO - would we ever need to merge existing and token rollup?
		for (CoreSentence sentence : document.sentences()) {
			if (! sentence.coreMap().containsKey(getAnnotationClass())) {
				ArrayList<Map<String, BigDecimal>> rawScores = new ArrayList<Map<String, BigDecimal>>();
				for (CoreLabel token : sentence.tokens()) {
					if (token.containsKey(getAnnotationClass())) {
						rawScores.add((Map<String, BigDecimal>) token.get(getAnnotationClass()));
					}
				}
				if (rawScores.size() > 0) {
					sentence.coreMap().set(getAnnotationClass(), aggregateScores(rawScores));
				}
			}
		}
	}

	public void scoreDocument(CoreDocument document) {
		if (! document.annotation().containsKey(getAnnotationClass())) {
			ArrayList<Map<String, BigDecimal>> rawScores = new ArrayList<Map<String, BigDecimal>>();
			for (CoreSentence sentence : document.sentences()) {
				if (sentence.coreMap().containsKey(getAnnotationClass())) {
					rawScores.add((Map<String, BigDecimal>)sentence.coreMap().get(getAnnotationClass()));
				}
				else {
					for (CoreLabel token : sentence.tokens()) {
						if (token.containsKey(getAnnotationClass())) {
							rawScores.add((Map<String, BigDecimal>) token.get(getAnnotationClass()));
						}
					}
				}

			}
			document.annotation().set(getAnnotationClass(), aggregateScores(rawScores));
		}
	}
	
	@Override
	public Object aggregateDocument(CoreDocument document) {
		Map<String, BigDecimal> rawScores = (Map<String, BigDecimal>) document.annotation().get(getAnnotationClass());
		BigDecimal normalizer = new BigDecimal(document.tokens().size());
		DocumentAggregateScore retval = new DocumentAggregateScore(getAnnotationClass().getSimpleName());
		if (rawScores == null) {
			getLogger().debug(String.format("No document scores: %s", getAnnotationClass()));
			return retval;
		}
		if (rawScores.size() == 0) {
			return retval;
		}
		double[] primitiveNormalizedScores = new double[rawScores.size()];
		BigDecimal rawSum = new BigDecimal(0);
		Iterator<Entry<String,BigDecimal>> rawScoresIter = rawScores.entrySet().iterator();

		for (int i=0; i<rawScores.size() && rawScoresIter.hasNext(); i++) {
			Entry<String,BigDecimal> rawScore = rawScoresIter.next();
			rawSum = rawSum.add(rawScore.getValue());
			primitiveNormalizedScores[i] = rawScore.getValue().divide(normalizer, 10, BigDecimal.ROUND_HALF_UP).doubleValue();
			retval.getAggregatedScores().add(new AggregatedScore(rawScore.getKey(), rawScore.getValue(), normalizer, rawScore.getValue()));
		}
		retval.getScoreStats().getScore().setRaw(rawSum);
		retval.getScoreStats().getScore().setNormalized(rawSum.divide(normalizer, 10, BigDecimal.ROUND_HALF_UP));
		retval.getScoreStats().getScore().setCount(new BigDecimal(rawScores.size()));
		retval.getScoreStats().getStats().setStddev(new BigDecimal(new StandardDeviation().evaluate(primitiveNormalizedScores)));
		retval.getScoreStats().getStats().setMean(retval.getScoreStats().getScore().getNormalized().divide(new BigDecimal(rawScores.size()), 10, BigDecimal.ROUND_HALF_UP));
		
		Collections.sort(retval.getAggregatedScores());
		Iterator<AggregatedScore> aggregatedScoresIter = retval.getAggregatedScores().iterator();
		for (int i=0; i<retval.getAggregatedScores().size() && aggregatedScoresIter.hasNext(); i++) {
			AggregatedScore aggregatedScore = aggregatedScoresIter.next();
			if (aggregatedScore.getScore().getNormalized().compareTo(new BigDecimal(0)) > 0) {
				aggregatedScore.getAggregateScore().setPercentage(aggregatedScore.getScore().getNormalized().divide(retval.getScoreStats().getScore().getNormalized(), 10, BigDecimal.ROUND_HALF_UP));
			}
			else {
				aggregatedScore.getAggregateScore().setPercentage(new BigDecimal(0));
			}
			if (retval.getScoreStats().getStats().getStddev().compareTo(new BigDecimal(0)) != 0) {
				aggregatedScore.getAggregateScore().setZ(aggregatedScore.getScore().getNormalized().subtract(retval.getScoreStats().getStats().getMean()).divide(retval.getScoreStats().getStats().getStddev(), 10, BigDecimal.ROUND_HALF_UP));
			}
			else {
				aggregatedScore.getAggregateScore().setZ(new BigDecimal(0));
			}
			aggregatedScore.getAggregateScore().setRank(new BigDecimal(i));
			aggregatedScore.getAggregateScore().setPercentile(new BigDecimal(1).subtract(new BigDecimal(i).divide(new BigDecimal(retval.getAggregatedScores().size()), 10, BigDecimal.ROUND_HALF_UP)));
			if (retval.getScoreStats().getStats().getMin().equals(new BigDecimal(0)) || retval.getScoreStats().getStats().getMin().compareTo(aggregatedScore.getScore().getNormalized()) > 0) {
				retval.getScoreStats().getStats().setMin(aggregatedScore.getScore().getNormalized());
				
			}
			if (retval.getScoreStats().getStats().getMax().compareTo(aggregatedScore.getScore().getNormalized()) < 0) {
				retval.getScoreStats().getStats().setMax(aggregatedScore.getScore().getNormalized());
			}
			if (retval.getScoreStats().getStats().getMedian().equals(new BigDecimal(0)) && (i > retval.getAggregatedScores().size()/2)) {
				retval.getScoreStats().getStats().setMedian(aggregatedScore.getScore().getNormalized());
			}

		}
		return retval;
	}

	public abstract Map<String, BigDecimal> aggregateScores(List<Map<String, BigDecimal>> allScores);

}
