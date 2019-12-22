package io.outofprintmagazine.nlp.pipeline.annotators;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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

public class WikipediaRelatedNounsAnnotator extends AbstractPosAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(WikipediaRelatedNounsAnnotator.class);

	
	public WikipediaRelatedNounsAnnotator() {
		super();
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass(), this.getAggregateClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass(), this.getAggregateClass()));			
	}
	
	public WikipediaRelatedNounsAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}
	
	@SuppressWarnings("rawtypes")
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
	
	@SuppressWarnings("rawtypes")
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPWikipediaRelatedNounsAnnotation.class;
	}
	
	@SuppressWarnings("rawtypes")
	public Class getAggregateClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPWikipediaRelatedNounsAnnotationAggregate.class;
	}
	
	public Class getTopicsAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPTopicsAnnotation.class;
	}
	
	public Class getNounsAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounsAnnotation.class;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void annotate(Annotation annotation) {
/*		CoreDocument document = new CoreDocument(annotation);
		Map<String,BigDecimal> topics = (Map<String, BigDecimal>) document.annotation().get(getTopicsAnnotationClass());
		List<Map<String,BigDecimal>> allWikipediaTopicScores = new ArrayList<Map<String,BigDecimal>>();
		for (String topic : topics.keySet()) {
			try {
				List<String> wikipediaTopicPages = WikipediaUtils.getInstance().getWikipediaPagesForTopic(topic);
				List<String> wikipediaTopicPageText = new ArrayList<String>();
				for (String wikipediaTopicPage : wikipediaTopicPages) {
					wikipediaTopicPageText.add(WikipediaUtils.getInstance().getWikipediaPageText(wikipediaTopicPage));
				}
				Map<String,BigDecimal> wikipediaTopicScores = WikipediaUtils.getInstance().getNounsForTopicPages(wikipediaTopicPageText);
				wikipediaTopicScores.remove(topic);
				allWikipediaTopicScores.add(wikipediaTopicScores);
			} 
			catch (IOException e) {
				logger.error(e);
			}
		}
		
		//Map<String,BigDecimal> consolidatedWikipediaTopicScores = scoreAggregator.aggregateScores(allWikipediaTopicScores);
		Map<String,BigDecimal> scoreMap = new HashMap<String,BigDecimal>();
		Map<String,BigDecimal> nouns = (Map<String, BigDecimal>) document.annotation().get(getNounsAnnotationClass());
		for (String noun : nouns.keySet()) {
			BigDecimal nounScore = nouns.get(noun);
			for (Map<String,BigDecimal> pageScore : allWikipediaTopicScores) {
				if (pageScore.get(noun) != null) {
					nounScore = nounScore.add(new BigDecimal(1));
				}
			}
			scoreMap.put(noun, nounScore);
		}
		
		document.annotation().set(getAnnotationClass(), scoreMap);
		score(document);*/
	}

	@Override
	public String getDescription() {
		return "Nouns from the main document amplified by nouns in the wikipedia pages of topics from the main document. NOT USED.";
	}

}
