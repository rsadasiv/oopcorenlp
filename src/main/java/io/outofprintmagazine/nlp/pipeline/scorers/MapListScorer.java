package io.outofprintmagazine.nlp.pipeline.scorers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;

public class MapListScorer implements Scorer {

	private static final Logger logger = LogManager.getLogger(MapListScorer.class);

	@SuppressWarnings("rawtypes")
	protected Class annotationClass;

	public MapListScorer() {
		super();
	}

	@SuppressWarnings("rawtypes")
	public MapListScorer( Class annotationClass) {
		super();
		this.setAnnotationClass(annotationClass);
	}

	@SuppressWarnings("rawtypes")
	public void setAnnotationClass(Class annotationClass) {
		this.annotationClass = annotationClass;
	}

	@SuppressWarnings("rawtypes")
	public Class getAnnotationClass() {
		return this.annotationClass;
	}

	@Override
	public void score(CoreDocument document) {
		scoreTokens(document);
		scoreSentences(document);
		scoreDocument(document);
	}

	public void scoreTokens(CoreDocument document) {
		// pass
	}

	@SuppressWarnings("unchecked")
	public void scoreSentences(CoreDocument document) {
		for (CoreSentence sentence : document.sentences()) {
			Map<String, List<String>> rawScores = new HashMap<String, List<String>>();
			for (CoreLabel token : sentence.tokens()) {
				if (token.containsKey(getAnnotationClass())) {
					Map<String, List<String>> tokenScores = (Map<String, List<String>>) token.get(getAnnotationClass());
					for (String scoreName : tokenScores.keySet()) {
						List<String> rawNameScore = rawScores.get(scoreName);
						if (rawNameScore == null) {
							rawNameScore = new ArrayList<String>();
						}
						for (String score : tokenScores.get(scoreName)) {
							if (! rawNameScore.contains(score)) {
								rawNameScore.add(score);
							}
						}
						rawScores.put(scoreName, rawNameScore);
					}
				}
			}
			if (rawScores.size() > 0) {
				//TODO - check if sentence scores already exist? Merge?
				sentence.coreMap().set(getAnnotationClass(), rawScores);
			}

		}
	}

	@SuppressWarnings("unchecked")
	public void scoreDocument(CoreDocument document) {
		Map<String, List<String>> rawScores = new HashMap<String, List<String>>();
		for (CoreSentence sentence : document.sentences()) {
			if (sentence.coreMap().containsKey(getAnnotationClass())) {
				Map<String, List<String>> sentenceScores = (Map<String, List<String>>) sentence.coreMap().get(getAnnotationClass());
				for (String scoreName : sentenceScores.keySet()) {
					List<String> rawNameScore = rawScores.get(scoreName);
					if (rawNameScore == null) {
						rawNameScore = new ArrayList<String>();
					}
					for (String score : sentenceScores.get(scoreName)) {
						if (! rawNameScore.contains(score)) {
							rawNameScore.add(score);
						}
					}
					rawScores.put(scoreName, rawNameScore);
				}
			}
		}

		document.annotation().set(getAnnotationClass(), rawScores);
	}
}
