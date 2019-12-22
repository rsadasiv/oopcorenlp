package io.outofprintmagazine.nlp.pipeline.scorers;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MapSum extends MapScorer implements Scorer {

	private static final Logger logger = LogManager.getLogger(MapSum.class);

	public MapSum() {
		super();
	}

	public MapSum(@SuppressWarnings("rawtypes") Class annotationClass) {
		super(annotationClass);
	}
	
	public MapSum(@SuppressWarnings("rawtypes") Class annotationClass, Class aggregateClass) {
		super(annotationClass, aggregateClass);
	}

	public Map<String, BigDecimal> aggregateScores(List<Map<String, BigDecimal>> allScores) {
		Map<String, BigDecimal> scoreMap = new HashMap<String, BigDecimal>();
		for (Map<String, BigDecimal> rawScore : allScores) {
			for (String key : rawScore.keySet()) {
				BigDecimal existingScore = scoreMap.get(key);
				if (existingScore == null) {
					scoreMap.put(key, rawScore.get(key));
				} else {
					scoreMap.put(key, existingScore.add(rawScore.get(key)));
				}
			}
		}
		return scoreMap;
	}

}
