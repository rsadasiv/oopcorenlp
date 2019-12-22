package io.outofprintmagazine.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CorpusDocumentAggregateScore  {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(CorpusDocumentAggregateScore.class);
	
	/*
	 * rollup of Annotation level score (DocumentAggregateScore.score) across all documents of a corpus
	 */
	public CorpusDocumentAggregateScore() {
		super();
	}
	
	public CorpusDocumentAggregateScore(String name) {
		this();
		setName(name);
	}
	
	public CorpusDocumentAggregateScore(String name, Map<String, DocumentAggregateScore> rawScores) {
		this(name);
		loadDocumentAggregateScores(rawScores);
	}
	
	private String name = "";
	private List<AggregatedScore> aggregatedScores = new ArrayList<AggregatedScore>();
	private ScoreStats scoreStats =  new ScoreStats();
	private List<CorpusDocumentMapAggregateScore> detailScores = new ArrayList<CorpusDocumentMapAggregateScore>();
	
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
	

	public List<CorpusDocumentMapAggregateScore> getDetailScores() {
		return detailScores;
	}

	public void setDetailScores(List<CorpusDocumentMapAggregateScore> detailScores) {
		this.detailScores = detailScores;
	}

	// string is document id
	//DocumentAggregateScore is Annotator score
	public void loadDocumentAggregateScores(Map<String, DocumentAggregateScore> rawScores) {
		double[] primitiveNormalizedScores = new double[rawScores.size()];
		Iterator<Entry<String,DocumentAggregateScore>> rawScoresIter = rawScores.entrySet().iterator();
		for (int i=0; i<rawScores.size() && rawScoresIter.hasNext(); i++) {
			Entry<String,DocumentAggregateScore> rawScore = rawScoresIter.next();
			if(rawScore.getValue() == null) {
				Score missingScore = new Score();
				primitiveNormalizedScores[i] = new BigDecimal(0).doubleValue();
				AggregatedScore aggregatedScore = new AggregatedScore(rawScore.getKey());
				aggregatedScore.setScore(missingScore);
				logger.debug("missing");
				getAggregatedScores().add(aggregatedScore);
			}
			else {
				getScoreStats().getScore().setRaw(getScoreStats().getScore().getRaw().add(rawScore.getValue().getScoreStats().getScore().getRaw()));
				getScoreStats().getScore().setNormalized(getScoreStats().getScore().getNormalized().add(rawScore.getValue().getScoreStats().getScore().getNormalized()));
				getScoreStats().getScore().setCount(getScoreStats().getScore().getCount().add(rawScore.getValue().getScoreStats().getScore().getCount()));
				primitiveNormalizedScores[i] = rawScore.getValue().getScoreStats().getScore().getNormalized().doubleValue();
				AggregatedScore aggregatedScore = new AggregatedScore(rawScore.getKey());
				aggregatedScore.setScore((Score)SerializationUtils.clone(rawScore.getValue().getScoreStats().getScore()));
				getAggregatedScores().add(aggregatedScore);
			}
		}
		getScoreStats().getStats().setStddev(new BigDecimal(new StandardDeviation().evaluate(primitiveNormalizedScores)));
		getScoreStats().getStats().setMean(getScoreStats().getScore().getNormalized().divide(new BigDecimal(rawScores.size()), 10, RoundingMode.HALF_DOWN));
		
		Collections.sort(getAggregatedScores());
		Iterator<AggregatedScore> aggregatedScoresIter = getAggregatedScores().iterator();
		for (int i=0; i<getAggregatedScores().size() && aggregatedScoresIter.hasNext(); i++) {
			AggregatedScore aggregatedScore = aggregatedScoresIter.next();
			if (aggregatedScore.getScore().getNormalized().compareTo(new BigDecimal(0)) > 0) {
				aggregatedScore.getAggregateScore().setPercentage(aggregatedScore.getScore().getNormalized().divide(getScoreStats().getScore().getNormalized(), 10, RoundingMode.HALF_DOWN));
				aggregatedScore.getAggregateScore().setZ(aggregatedScore.getScore().getNormalized().subtract(getScoreStats().getStats().getMean()).divide(getScoreStats().getStats().getStddev(), 10, RoundingMode.HALF_DOWN));
				aggregatedScore.getAggregateScore().setRank(new BigDecimal(i));
				aggregatedScore.getAggregateScore().setPercentile(new BigDecimal(1).subtract(new BigDecimal(i).divide(new BigDecimal(getAggregatedScores().size()), 10, RoundingMode.HALF_DOWN)));
			}
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

		Map<String, CorpusDocumentMapAggregateScores> allSubAnnotationScores = new HashMap<String, CorpusDocumentMapAggregateScores>();
		rawScoresIter = rawScores.entrySet().iterator();
		while (rawScoresIter.hasNext()) {
			//for each document
			Entry<String,DocumentAggregateScore> rawScore = rawScoresIter.next();

			if (rawScore.getValue() != null && rawScore.getValue().getAggregatedScores() != null) {
				Iterator<AggregatedScore> aggregatedScoreIter = rawScore.getValue().getAggregatedScores().iterator();
				while (aggregatedScoreIter.hasNext()) {
					//for each subAnnotator score
					AggregatedScore aggregatedScore = aggregatedScoreIter.next();
					CorpusDocumentMapAggregateScores subAnnotationScore = allSubAnnotationScores.get(aggregatedScore.getName());
					if (subAnnotationScore == null) {
						subAnnotationScore = new CorpusDocumentMapAggregateScores();
					}
					subAnnotationScore.getRaw().put(rawScore.getKey(), aggregatedScore.getScore().getRaw());
					subAnnotationScore.getNormalized().put(rawScore.getKey(), aggregatedScore.getScore().getNormalized());
					subAnnotationScore.getCount().put(rawScore.getKey(), aggregatedScore.getScore().getCount());
					subAnnotationScore.getRank().put(rawScore.getKey(), aggregatedScore.getAggregateScore().getRank());
					subAnnotationScore.getPercentage().put(rawScore.getKey(), aggregatedScore.getAggregateScore().getPercentage());
					subAnnotationScore.getPercentile().put(rawScore.getKey(), aggregatedScore.getAggregateScore().getPercentile());
					subAnnotationScore.getZ().put(rawScore.getKey(), aggregatedScore.getAggregateScore().getZ());
					allSubAnnotationScores.put(aggregatedScore.getName(), subAnnotationScore);
				}
			}
			else {
				logger.debug("rawScore missing/empty: " + rawScore.getKey());
			}
		}
		
		Iterator<Entry<String,CorpusDocumentMapAggregateScores>> allSubAnnotationScoresIter = allSubAnnotationScores.entrySet().iterator();
		while (allSubAnnotationScoresIter.hasNext()) {
			Entry<String,CorpusDocumentMapAggregateScores> subAnnotationTotalScore = allSubAnnotationScoresIter.next();
			CorpusDocumentMapAggregateScore subAnnotationDetailScore = new CorpusDocumentMapAggregateScore(subAnnotationTotalScore.getKey());
			subAnnotationDetailScore.setRaw(new DocumentAggregateScore(
					subAnnotationTotalScore.getKey(), 
					subAnnotationTotalScore.getValue().getRaw(),
					new BigDecimal(1)
				)
			);
			//too detailed for in-memory data model
			//subAnnotationDetailScore.getRaw().getAggregatedScores().clear();
			
			subAnnotationDetailScore.setNormalized(new DocumentAggregateScore(
					subAnnotationTotalScore.getKey(), 
					subAnnotationTotalScore.getValue().getNormalized(),
					new BigDecimal(1)
				)
			);
			//too detailed for in-memory data model
			//subAnnotationDetailScore.getNormalized().getAggregatedScores().clear();
			
			subAnnotationDetailScore.setCount(new DocumentAggregateScore(
					subAnnotationTotalScore.getKey(), 
					subAnnotationTotalScore.getValue().getCount(),
					new BigDecimal(1)
				)
			);
			//too detailed for in-memory data model
			//subAnnotationDetailScore.getCount().getAggregatedScores().clear();
			
			subAnnotationDetailScore.setRank(new DocumentAggregateScore(
					subAnnotationTotalScore.getKey(), 
					subAnnotationTotalScore.getValue().getRank(),
					new BigDecimal(1)
				)
			);
			//too detailed for in-memory data model
			//subAnnotationDetailScore.getRank().getAggregatedScores().clear();
			
			subAnnotationDetailScore.setPercentage(new DocumentAggregateScore(
					subAnnotationTotalScore.getKey(), 
					subAnnotationTotalScore.getValue().getPercentage(),
					new BigDecimal(1)
				)
			);
			//too detailed for in-memory data model
			//subAnnotationDetailScore.getPercentage().getAggregatedScores().clear();
			
			subAnnotationDetailScore.setPercentile(new DocumentAggregateScore(
					subAnnotationTotalScore.getKey(), 
					subAnnotationTotalScore.getValue().getPercentile(),
					new BigDecimal(1)
				)
			);
			//too detailed for in-memory data model
			//subAnnotationDetailScore.getPercentile().getAggregatedScores().clear();
			
			subAnnotationDetailScore.setZ(new DocumentAggregateScore(
					subAnnotationTotalScore.getKey(), 
					subAnnotationTotalScore.getValue().getZ(),
					new BigDecimal(1)
				)
			);
			//too detailed for in-memory data model
			//subAnnotationDetailScore.getZ().getAggregatedScores().clear();
			
			detailScores.add(subAnnotationDetailScore);

		}
	}
	
	

}