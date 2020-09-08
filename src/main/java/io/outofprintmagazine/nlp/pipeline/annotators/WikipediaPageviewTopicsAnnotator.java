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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.IScorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.ISerializer;
import io.outofprintmagazine.nlp.utils.WikipediaUtils;

public class WikipediaPageviewTopicsAnnotator extends AbstractPosAnnotator implements Annotator, IOOPAnnotator{
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(WikipediaPageviewTopicsAnnotator.class);
	
	@SuppressWarnings("unused")
	private Logger getLogger() {
		return logger;
	}
		
	public WikipediaPageviewTopicsAnnotator() {
		super();
		this.setScorer((IScorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((ISerializer)new MapSerializer(this.getAnnotationClass()));			
	}
	
	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(
			new ArraySet<>(
				Arrays.asList(
					CoreAnnotations.TextAnnotation.class, 
					CoreAnnotations.TokensAnnotation.class,
					CoreAnnotations.SentencesAnnotation.class, 
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPTopicsAnnotation.class
				)
			)
		);
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPWikipediaPageviewTopicsAnnotation.class;
	}

	protected Class getTopicsAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPTopicsAnnotation.class;
	}
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		if (!annotation.containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPTopicsAnnotation.class)) {
			logger.error("requires() not satisfied");
		}
		Map<String,BigDecimal> topics = (Map<String, BigDecimal>) document.annotation().get(getTopicsAnnotationClass());
		Map<String,BigDecimal> scoreMap = new HashMap<String,BigDecimal>();
		for (String topic : topics.keySet()) {
			try {
				addToScoreMap(scoreMap, topic, WikipediaUtils.getInstance(getParameterStore()).getWikipediaPageviewsForTopic(topic).multiply(topics.get(topic)));
			} 
			catch (IOException | URISyntaxException e) {
				logger.error(e);
			}
		} 
		document.annotation().set(getAnnotationClass(), scoreMap);
	}

	@Override
	public String getDescription() {
		return "Wikipedia pageviews for the past month "
				+ "(https://wikimedia.org/api/rest_v1/metrics/pageviews/per-article/en.wikipedia.org/all-access/all-agents/Shakespeare) "
				+ "for Topics (NNP,NNPS).";
	}

}
