package io.outofprintmagazine.nlp.pipeline.annotators;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.WikipediaUtils;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public class WikipediaPageviewTopicsAnnotator extends AbstractAggregatePosAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(WikipediaPageviewTopicsAnnotator.class);
		
	public WikipediaPageviewTopicsAnnotator() {
		super();
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass(), this.getAggregateClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass(), this.getAggregateClass()));			
	}
	
	public WikipediaPageviewTopicsAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
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
	
	@Override
	public Class getAggregateClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPWikipediaPageviewTopicsAnnotationAggregate.class;
	}
	
	protected Class getTopicsAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPTopicsAnnotation.class;
	}
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		Map<String,BigDecimal> topics = (Map<String, BigDecimal>) document.annotation().get(getTopicsAnnotationClass());
		Map<String,BigDecimal> scoreMap = new HashMap<String,BigDecimal>();
		for (String topic : topics.keySet()) {
			try {
				scoreMap.put(topic, WikipediaUtils.getInstance().getWikipediaPageviewsForTopic(topic).multiply(topics.get(topic)));
			} 
			catch (IOException e) {
				logger.error(e);
			}
		} 
		document.annotation().set(getAnnotationClass(), scoreMap);
		score(document);
	}

	@Override
	public String getDescription() {
		return "Wikipedia pageviews for the past month "
				+ "(https://wikimedia.org/api/rest_v1/metrics/pageviews/per-article/en.wikipedia.org/all-access/all-agents/Shakespeare) "
				+ "for Topics (NNP,NNPS).";
	}

}
