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
package io.outofprintmagazine.nlp.pipeline.annotators;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.pipeline.PhraseAnnotation;
import io.outofprintmagazine.nlp.pipeline.scorers.PhraseScorer;
import io.outofprintmagazine.nlp.pipeline.scorers.IScorer;
import io.outofprintmagazine.nlp.pipeline.serializers.PhraseSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.ISerializer;
import io.outofprintmagazine.nlp.utils.WikipediaUtils;

public class WikipediaCategoriesAnnotator extends AbstractPosAnnotator implements Annotator, IOOPAnnotator {
	
	private static final Logger logger = LogManager.getLogger(WikipediaCategoriesAnnotator.class);
	
	@SuppressWarnings("unused")
	private Logger getLogger() {
		return logger;
	}
	
	public WikipediaCategoriesAnnotator() {
		super();
		this.setScorer((IScorer) new PhraseScorer(this.getAnnotationClass()));
		this.setSerializer((ISerializer) new PhraseSerializer(this.getAnnotationClass()));	
	}
	
	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(
			new ArraySet<>(
				Arrays.asList(
					CoreAnnotations.TextAnnotation.class, 
					CoreAnnotations.TokensAnnotation.class,
					CoreAnnotations.SentencesAnnotation.class, 
					//io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounsAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPTopicsAnnotation.class
				)
			)
		);
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPWikipediaCategoriesAnnotation.class;
	}

	public Class getTopicsAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPTopicsAnnotation.class;
	}
	
	public Class getNounsAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounsAnnotation.class;
	}
	
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		List<PhraseAnnotation> scoreList = new ArrayList<PhraseAnnotation>();

		/*
		Map<String,BigDecimal> nouns = (Map<String, BigDecimal>) document.annotation().get(getNounsAnnotationClass());
		for (String noun : nouns.keySet()) {
			try {
				scoreMap.add(WikipediaUtils.getInstance().getWikipediaCategoriesForTopic(noun));
			} 
			catch (IOException e) {
				logger.error(e);
			}
		}
		*/
		if (!annotation.containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPTopicsAnnotation.class)) {
			logger.error("requires() not satisfied");
		}
		Map<String,BigDecimal> topics = (Map<String, BigDecimal>) document.annotation().get(getTopicsAnnotationClass());
		for (String topic : topics.keySet()) {
			try {
				Map<String, BigDecimal> categories = WikipediaUtils.getInstance(getParameterStore()).getWikipediaCategoriesForTopic(topic);
				for (Entry<String,BigDecimal> categoryScore : categories.entrySet()) {
					addToScoreList(scoreList, new PhraseAnnotation(categoryScore.getKey(), categoryScore.getValue()));
				}
			} 
			catch (IOException e) {
				logger.debug(e);
			} 
			catch (URISyntaxException e) {
				logger.debug(e);
			}
		}
		
		document.annotation().set(getAnnotationClass(), scoreList);
	}

	@Override
	public String getDescription() {
		return "Wikipedia page categories (https://en.wikipedia.org/w/api.php?format=json&action=query&prop=categories&utf8=1&titles=Shakespeare) for Topics (NNP,NNPS).";
	}

}
