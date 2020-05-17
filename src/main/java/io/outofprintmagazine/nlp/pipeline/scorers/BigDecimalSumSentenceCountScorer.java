package io.outofprintmagazine.nlp.pipeline.scorers;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreAnnotations.ParagraphIndexAnnotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;

public class BigDecimalSumSentenceCountScorer extends BigDecimalSum implements Scorer {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(BigDecimalSumSentenceCountScorer.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}

	public BigDecimalSumSentenceCountScorer() {
		super();
	}

	public BigDecimalSumSentenceCountScorer(Class annotationClass) {
		super(annotationClass);
	}

	@Override
	public Map<String, BigDecimal> getDocumentAggregatableScores(CoreDocument document) {
		int paragraphIdx = 0;
		int sentenceCount = -1;
		Map<String, BigDecimal> rawScores = new HashMap<String, BigDecimal>();
		for (int i=0;i<document.sentences().size();i++) {
			CoreSentence sentence = document.sentences().get(i);
			if (sentence.coreMap().containsKey(ParagraphIndexAnnotation.class)) {
				if (paragraphIdx < sentence.coreMap().get(ParagraphIndexAnnotation.class).intValue()) {
					rawScores.put(new Integer(paragraphIdx).toString(), new BigDecimal(++sentenceCount));

					sentenceCount = 0;
					paragraphIdx++;
				}
				else {
					++sentenceCount;
				}
			}
		}
		rawScores.put(new Integer(paragraphIdx).toString(), new BigDecimal(++sentenceCount));
		return rawScores;
	}

}
