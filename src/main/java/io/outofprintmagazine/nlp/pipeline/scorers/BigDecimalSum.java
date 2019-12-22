package io.outofprintmagazine.nlp.pipeline.scorers;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BigDecimalSum extends BigDecimalScorer implements Scorer {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(BigDecimalSum.class);

	public BigDecimalSum() {
		super();
	}

	public BigDecimalSum(@SuppressWarnings("rawtypes") Class annotationClass) {
		super();
		this.setAnnotationClass(annotationClass);
	}
	
	public BigDecimalSum(@SuppressWarnings("rawtypes") Class annotationClass, Class aggregateClass) {
		super(annotationClass, aggregateClass);
	}

	@Override
	public BigDecimal aggregateScores(ArrayList<BigDecimal> allScores) {
		BigDecimal score = new BigDecimal(0);
		for (BigDecimal rawScore : allScores) {
			score = score.add(rawScore);
		}
		return score;
	}

}
