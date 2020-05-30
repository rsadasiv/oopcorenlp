/*******************************************************************************
 * Copyright (C) 2020 Ram Sadasiv
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
