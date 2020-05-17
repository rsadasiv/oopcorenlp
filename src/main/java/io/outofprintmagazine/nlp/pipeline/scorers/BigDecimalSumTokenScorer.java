package io.outofprintmagazine.nlp.pipeline.scorers;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;

public class BigDecimalSumTokenScorer extends BigDecimalSum implements Scorer {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(BigDecimalSumTokenScorer.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}

	public BigDecimalSumTokenScorer() {
		super();
	}

	public BigDecimalSumTokenScorer(Class annotationClass) {
		super(annotationClass);
	}

	@Override
	public Map<String, BigDecimal> getDocumentAggregatableScores(CoreDocument document) {
		Map<String, BigDecimal> rawScores = new HashMap<String, BigDecimal>();
		int tokenIdx = 0;
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				rawScores.put(new Integer(tokenIdx++).toString(), (BigDecimal) token.get(getAnnotationClass()));
			}
		}
		return rawScores;
	}

}
