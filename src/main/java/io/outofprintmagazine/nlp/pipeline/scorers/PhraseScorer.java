package io.outofprintmagazine.nlp.pipeline.scorers;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import io.outofprintmagazine.nlp.pipeline.PhraseAnnotation;

public class PhraseScorer implements Scorer {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(PhraseScorer.class);
	
	protected Logger getLogger() {
		return logger;
	}

	protected Class annotationClass = null;

	public PhraseScorer() {
		super();
	}

	public PhraseScorer(Class annotationClass) {
		super();
		this.setAnnotationClass(annotationClass);
	}
	

	public void setAnnotationClass(Class annotationClass) {
		this.annotationClass = annotationClass;
	}

	public Class getAnnotationClass() {
		return this.annotationClass;
	}
	
	@Override
	public void score(CoreDocument document) {
		//scoreTokens(document);
		scoreSentences(document);
		scoreDocument(document);
	}
	
	public void scoreSentences(CoreDocument document) {
		for (CoreSentence sentence : document.sentences()) {
			if (! sentence.coreMap().containsKey(getAnnotationClass())) {
				List<PhraseAnnotation> rawScores = new ArrayList<PhraseAnnotation>();
				for (CoreLabel token : sentence.tokens()) {
					if (token.containsKey(getAnnotationClass())) {
						List<PhraseAnnotation> scoreMap = (List<PhraseAnnotation>) token.get(getAnnotationClass());
						for (PhraseAnnotation p : scoreMap) {
							addToScoreList(rawScores, p);
						}
					}
				}
				if (rawScores.size() > 0) {
					sentence.coreMap().set(getAnnotationClass(), rawScores);
				}
			}
		}
	}
	

	public void scoreDocument(CoreDocument document) {
		if (! document.annotation().containsKey(getAnnotationClass())) {
			List<PhraseAnnotation> rawScores = new ArrayList<PhraseAnnotation>();
			for (CoreSentence sentence : document.sentences()) {
				if (sentence.coreMap().containsKey(getAnnotationClass())) {
					List<PhraseAnnotation> scoreMap = (List<PhraseAnnotation>) sentence.coreMap().get(getAnnotationClass());
					for (PhraseAnnotation p : scoreMap) {
						addToScoreList(rawScores, p);
					}
				}
			}
			document.annotation().set(getAnnotationClass(), rawScores);
		}
	}
	
	
	protected void addToScoreList(List<PhraseAnnotation> scoreMap, PhraseAnnotation p) {
		boolean updatedExisting = false;
		for (PhraseAnnotation scorePair : scoreMap) {
			if (scorePair.getName().equals(p.getName())) {
				scorePair.setValue(scorePair.getValue().add(p.getValue()));
				updatedExisting = true;
				break;
			}
		}
		if (!updatedExisting) {
			scoreMap.add(new PhraseAnnotation(p));
		}
	}

	@Override
	public Object aggregateDocument(CoreDocument document) {
		return document.annotation().get(getAnnotationClass());
	}

}
