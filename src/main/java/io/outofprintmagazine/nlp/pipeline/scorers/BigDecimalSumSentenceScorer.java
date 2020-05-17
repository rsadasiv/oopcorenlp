package io.outofprintmagazine.nlp.pipeline.scorers;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;

public class BigDecimalSumSentenceScorer extends BigDecimalSum implements Scorer {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(BigDecimalSumSentenceScorer.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}

	public BigDecimalSumSentenceScorer() {
		super();
	}

	public BigDecimalSumSentenceScorer(Class annotationClass) {
		super(annotationClass);
	}

	@Override
	public Map<String, BigDecimal> getDocumentAggregatableScores(CoreDocument document) {
		Map<String, BigDecimal> rawScores = new HashMap<String, BigDecimal>();
		for (int i=0;i<document.sentences().size();i++) {
			CoreSentence sentence = document.sentences().get(i);
			if (sentence.coreMap().containsKey(getAnnotationClass())) {
				rawScores.put(Integer.toString(i), (BigDecimal) sentence.coreMap().get(getAnnotationClass()));
			}
			else {
				rawScores.put(Integer.toString(i), new BigDecimal(0));
			}
		}
		return rawScores;
	}

}
