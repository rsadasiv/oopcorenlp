package io.outofprintmagazine.nlp.pipeline.scorers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BigDecimalAvg extends BigDecimalScorer implements Scorer {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(BigDecimalAvg.class);

	public BigDecimalAvg() {
		super();
	}

	public BigDecimalAvg(@SuppressWarnings("rawtypes") Class annotationClass) {
		super();
		this.setAnnotationClass(annotationClass);
	}
	
	public BigDecimalAvg(@SuppressWarnings("rawtypes") Class annotationClass, Class aggregateClass) {
		super(annotationClass, aggregateClass);
	}

	@Override
	public BigDecimal aggregateScores(ArrayList<BigDecimal> allScores) {
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

}
