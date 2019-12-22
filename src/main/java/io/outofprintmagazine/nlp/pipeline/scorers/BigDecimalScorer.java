package io.outofprintmagazine.nlp.pipeline.scorers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import io.outofprintmagazine.util.AggregateScore;
import io.outofprintmagazine.util.DocumentAggregateScore;

public abstract class BigDecimalScorer implements Scorer {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(BigDecimalScorer.class);


	protected Class annotationClass = null;
	protected Class aggregateClass = null;

	public BigDecimalScorer() {
		super();
	}

	public BigDecimalScorer(@SuppressWarnings("rawtypes") Class annotationClass) {
		super();
		this.setAnnotationClass(annotationClass);
	}
	
	public BigDecimalScorer(@SuppressWarnings("rawtypes") Class annotationClass, Class aggregateClass) {
		super();
		this.setAnnotationClass(annotationClass);
		this.setAggregateClass(aggregateClass);
	}

	public void setAnnotationClass(Class annotationClass) {
		this.annotationClass = annotationClass;
	}

	public void setAggregateClass(Class aggregateClass) {
		this.aggregateClass = aggregateClass;
	}

	public Class getAnnotationClass() {
		return this.annotationClass;
	}
	
	public Class getAggregateClass() {
		return this.aggregateClass;
	}


	@Override
	public void score(CoreDocument document) {
		scoreTokens(document);
		scoreSentences(document);
		scoreDocument(document);
		if (getAggregateClass() != null) {
			scoreDocumentAggregate(document);
		}
	}

	public void scoreTokens(CoreDocument document) {
		// pass
	}

	public void scoreSentences(CoreDocument document) {
		for (CoreSentence sentence : document.sentences()) {
			ArrayList<BigDecimal> rawScores = new ArrayList<BigDecimal>();
			for (CoreLabel token : sentence.tokens()) {
				if (token.containsKey(getAnnotationClass())) {
					rawScores.add((BigDecimal) token.get(getAnnotationClass()));
				}
			}
			if (rawScores.size() > 0) {
				sentence.coreMap().set(getAnnotationClass(), aggregateScores(rawScores));
			}

		}
	}

	public void scoreDocument(CoreDocument document) {
		if (! document.annotation().containsKey(getAnnotationClass())) {
			ArrayList<BigDecimal> rawScores = new ArrayList<BigDecimal>();
			for (CoreSentence sentence : document.sentences()) {
				if (sentence.coreMap().containsKey(getAnnotationClass())) {
					for (CoreLabel token : sentence.tokens()) {
						if (token.containsKey(getAnnotationClass())) {
							rawScores.add((BigDecimal) token.get(getAnnotationClass()));
						}
					}
				}
			}
			if (rawScores.size() == 0) {
				for (CoreSentence sentence : document.sentences()) {
					if (sentence.coreMap().containsKey(getAnnotationClass())) {
						rawScores.add((BigDecimal) sentence.coreMap().get(getAnnotationClass()));
					}
				}
			}
			document.annotation().set(getAnnotationClass(), aggregateScores(rawScores));
		}
	}
	
	public void scoreDocumentAggregate(CoreDocument document) {
		if (document.annotation().containsKey(getAnnotationClass())) {
			Map<String, BigDecimal> rawScores = new HashMap<String, BigDecimal>();
			for (int i=0;i<document.sentences().size();i++) {
				CoreSentence sentence = document.sentences().get(i);
				if (sentence.coreMap().containsKey(getAnnotationClass())) {
					rawScores.put(Integer.toString(i), (BigDecimal) sentence.coreMap().get(getAnnotationClass()));
				}
			}
			//TODO - aggregate of BigDecimal Scores?
			/*
			document.annotation().set(
				getAggregateClass(),
				new DocumentAggregateScore(
					getAnnotationClass().getCanonicalName() + "Aggregate", 
					(Map<String, BigDecimal>) document.annotation().get(getAnnotationClass()),
					new BigDecimal(1)
				)
			);
			*/
		}
	}	

	public abstract BigDecimal aggregateScores(ArrayList<BigDecimal> allScores);
}
