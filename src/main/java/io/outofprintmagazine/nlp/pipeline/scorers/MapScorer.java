package io.outofprintmagazine.nlp.pipeline.scorers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import io.outofprintmagazine.util.DocumentAggregateScore;

public abstract class MapScorer implements Scorer {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(MapScorer.class);


	protected Class annotationClass = null;
	protected Class aggregateClass = null;

	public MapScorer() {
		super();
	}

	public MapScorer(Class annotationClass) {
		super();
		this.setAnnotationClass(annotationClass);
	}
	
	public MapScorer(Class annotationClass, Class aggregateClass) {
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
			ArrayList<Map<String, BigDecimal>> rawScores = new ArrayList<Map<String, BigDecimal>>();
			for (CoreLabel token : sentence.tokens()) {
				if (token.containsKey(getAnnotationClass())) {
					rawScores.add((Map<String, BigDecimal>) token.get(getAnnotationClass()));
				}
			}
			if (rawScores.size() > 0) {
				//TODO - check if sentence scores already exist? Merge?
				sentence.coreMap().set(getAnnotationClass(), aggregateScores(rawScores));
			}
		}
	}

	public void scoreDocument(CoreDocument document) {
		if (! document.annotation().containsKey(getAnnotationClass())) {
			ArrayList<Map<String, BigDecimal>> rawScores = new ArrayList<Map<String, BigDecimal>>();
			for (CoreSentence sentence : document.sentences()) {
				for (CoreLabel token : sentence.tokens()) {
					if (token.containsKey(getAnnotationClass())) {
						rawScores.add((Map<String, BigDecimal>) token.get(getAnnotationClass()));
					}
				}
			}
			document.annotation().set(getAnnotationClass(), aggregateScores(rawScores));
		}
	}
	
	public void scoreDocumentAggregate(CoreDocument document) {
		if (document.annotation().containsKey(getAnnotationClass())) {
			document.annotation().set(
				getAggregateClass(),
				new DocumentAggregateScore(
					getAnnotationClass().getCanonicalName() + "Aggregate", 
					(Map<String, BigDecimal>) document.annotation().get(getAnnotationClass()),
					new BigDecimal(document.tokens().size())
				)
			);
		}
	}

	public abstract Map<String, BigDecimal> aggregateScores(List<Map<String, BigDecimal>> allScores);

}
