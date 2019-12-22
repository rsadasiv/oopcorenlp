package io.outofprintmagazine.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DocumentAggregateScore {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(DocumentAggregateScore.class);
	
	public DocumentAggregateScore() {
		super();
	}
	
	public DocumentAggregateScore(String name) {
		this();
		this.setName(name);
	}
	
	private String name = "";
	private List<AggregatedScore> aggregatedScores = new ArrayList<AggregatedScore>();
	private ScoreStats scoreStats =  new ScoreStats();

	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<AggregatedScore> getAggregatedScores() {
		return aggregatedScores;
	}
	
	public void setAggregatedScores(List<AggregatedScore> aggregatedScores) {
		this.aggregatedScores = aggregatedScores;
	}
	
	public ScoreStats getScoreStats() {
		return scoreStats;
	}

	public void setScoreStats(ScoreStats scoreStats) {
		this.scoreStats = scoreStats;
	}

	public DocumentAggregateScore(String name, Map<String, BigDecimal> rawScores, BigDecimal normalizer) {
		this(name);
		if (rawScores.size() == 0) {
			return;
		}
		double[] primitiveNormalizedScores = new double[rawScores.size()];
		BigDecimal rawSum = new BigDecimal(0);
		Iterator<Entry<String,BigDecimal>> rawScoresIter = rawScores.entrySet().iterator();

		for (int i=0; i<rawScores.size() && rawScoresIter.hasNext(); i++) {
			Entry<String,BigDecimal> rawScore = rawScoresIter.next();
			rawSum = rawSum.add(rawScore.getValue());
			primitiveNormalizedScores[i] = rawScore.getValue().divide(normalizer, 10, BigDecimal.ROUND_HALF_UP).doubleValue();
			getAggregatedScores().add(new AggregatedScore(rawScore.getKey(), rawScore.getValue(), normalizer));
		}
		getScoreStats().getScore().setRaw(rawSum);
		getScoreStats().getScore().setNormalized(rawSum.divide(normalizer, 10, BigDecimal.ROUND_HALF_UP));
		getScoreStats().getScore().setCount(new BigDecimal(rawScores.size()));
		getScoreStats().getStats().setStddev(new BigDecimal(new StandardDeviation().evaluate(primitiveNormalizedScores)));
		getScoreStats().getStats().setMean(getScoreStats().getScore().getNormalized().divide(new BigDecimal(rawScores.size()), 10, BigDecimal.ROUND_HALF_UP));
		
		Collections.sort(getAggregatedScores());
		Iterator<AggregatedScore> aggregatedScoresIter = getAggregatedScores().iterator();
		for (int i=0; i<getAggregatedScores().size() && aggregatedScoresIter.hasNext(); i++) {
			AggregatedScore aggregatedScore = aggregatedScoresIter.next();
			if (aggregatedScore.getScore().getNormalized().compareTo(new BigDecimal(0)) > 0) {
				aggregatedScore.getAggregateScore().setPercentage(aggregatedScore.getScore().getNormalized().divide(getScoreStats().getScore().getNormalized(), 10, BigDecimal.ROUND_HALF_UP));
			}
			else {
				aggregatedScore.getAggregateScore().setPercentage(new BigDecimal(0));
			}
			if (getScoreStats().getStats().getStddev().compareTo(new BigDecimal(0)) != 0) {
				aggregatedScore.getAggregateScore().setZ(aggregatedScore.getScore().getNormalized().subtract(getScoreStats().getStats().getMean()).divide(getScoreStats().getStats().getStddev(), 10, BigDecimal.ROUND_HALF_UP));
			}
			else {
				aggregatedScore.getAggregateScore().setZ(new BigDecimal(0));
			}
			aggregatedScore.getAggregateScore().setRank(new BigDecimal(i));
			aggregatedScore.getAggregateScore().setPercentile(new BigDecimal(1).subtract(new BigDecimal(i).divide(new BigDecimal(getAggregatedScores().size()), 10, BigDecimal.ROUND_HALF_UP)));
			if (getScoreStats().getStats().getMin().equals(new BigDecimal(0)) || getScoreStats().getStats().getMin().compareTo(aggregatedScore.getScore().getNormalized()) > 0) {
				getScoreStats().getStats().setMin(aggregatedScore.getScore().getNormalized());
				
			}
			if (getScoreStats().getStats().getMax().compareTo(aggregatedScore.getScore().getNormalized()) < 0) {
				getScoreStats().getStats().setMax(aggregatedScore.getScore().getNormalized());
			}
			if (getScoreStats().getStats().getMedian().equals(new BigDecimal(0)) && (i > getAggregatedScores().size()/2)) {
				getScoreStats().getStats().setMedian(aggregatedScore.getScore().getNormalized());
			}

		}
	}
	
	public String toJsonString() {
		return String.format(
				"{%n\t\"Name\": %s%n\t\"ScoreStats\": %s%n\t\"AggregatedScores\": %s%n}",
				getName(),
				getScoreStats().toJsonString(), 
				getAggregatedScores().size()
		);
	}
	


}
