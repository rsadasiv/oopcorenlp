package io.outofprintmagazine.nlp.pipeline.scorers;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.pipeline.CoreDocument;

public class BigDecimalSumDocumentScorer extends BigDecimalSum implements Scorer {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(BigDecimalSumDocumentScorer.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}

	public BigDecimalSumDocumentScorer() {
		super();
	}

	public BigDecimalSumDocumentScorer(Class annotationClass) {
		super(annotationClass);
	}

	@Override
	public Map<String, BigDecimal> getDocumentAggregatableScores(CoreDocument document) {
		Map<String, BigDecimal> rawScores = new HashMap<String, BigDecimal>();
		rawScores.put(new Integer(0).toString(), (BigDecimal) document.annotation().get(getAnnotationClass()));
		return rawScores;
	}
	
}
