package io.outofprintmagazine.nlp.pipeline.annotators;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

public class WikipediaCategoriesAnnotator extends AbstractPosAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(WikipediaCategoriesAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}
	
	private MapSum scoreAggregator = new MapSum();
	
	public WikipediaCategoriesAnnotator() {
		super();
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass()));	
	}
	
	public WikipediaCategoriesAnnotator(Properties properties) {
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
		List<Map<String,BigDecimal>> scoreMap = new ArrayList<Map<String,BigDecimal>>();

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
		
		Map<String,BigDecimal> topics = (Map<String, BigDecimal>) document.annotation().get(getTopicsAnnotationClass());
		for (String topic : topics.keySet()) {
			try {
				scoreMap.add(WikipediaUtils.getInstance().getWikipediaCategoriesForTopic(topic));
			} 
			catch (IOException e) {
				logger.error(e);
			}
		}
		
		document.annotation().set(getAnnotationClass(), scoreAggregator.aggregateScores(scoreMap));
		score(document);
	}

	@Override
	public String getDescription() {
		return "Wikipedia page categories (https://en.wikipedia.org/w/api.php?format=json&action=query&prop=categories&utf8=1&titles=Shakespeare) for Topics (NNP,NNPS).";
	}

}
