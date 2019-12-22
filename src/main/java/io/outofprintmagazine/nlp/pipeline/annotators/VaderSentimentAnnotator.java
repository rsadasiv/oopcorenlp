package io.outofprintmagazine.nlp.pipeline.annotators;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vader.sentiment.analyzer.SentimentAnalyzer;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.pipeline.scorers.BigDecimalAvg;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.BigDecimalSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public class VaderSentimentAnnotator extends AbstractAggregatePosAnnotator implements Annotator, OOPAnnotator {

	private static final Logger logger = LogManager.getLogger(VaderSentimentAnnotator.class);
	
	public VaderSentimentAnnotator() {
		super();
		this.setScorer((Scorer)new BigDecimalAvg(this.getAnnotationClass(), this.getAggregateClass()));
		this.setSerializer((Serializer)new BigDecimalSerializer(this.getAnnotationClass(), this.getAggregateClass()));
	}
	
	public VaderSentimentAnnotator(String name, Properties props) {
		this();
		properties = props;
	}
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
        for (int i = 0, sz = document.sentences().size(); i < sz; i ++) {
        	CoreSentence sentence = document.sentences().get(i);
        	BigDecimal score = new BigDecimal(.5);
        	try {
	        	String text = sentence.text();
	        	SentimentAnalyzer sentimentAnalyzer = new SentimentAnalyzer(text);
				sentimentAnalyzer.analyze();
				score = new BigDecimal((sentimentAnalyzer.getPolarity().get("compound")+1)/2);
				sentence.coreMap().set(getAnnotationClass(), score);
				for (CoreLabel token : sentence.tokens()) {
					if (token.containsKey(getAnnotationClass())) {
						token.set(getAnnotationClass(), score);
					}
				}
			}
			catch (Exception e) {
				logger.error("bad vader", e);
				sentence.coreMap().set(getAnnotationClass(), score );
			}
        }
		score(document);
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.VaderSentimentAnnotation.class;
	}
	
	@Override
	public Class getAggregateClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.VaderSentimentAnnotationAggregate.class;
	}

	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(
			new ArraySet<>(
				Arrays.asList(
					CoreAnnotations.TextAnnotation.class, 
					CoreAnnotations.TokensAnnotation.class,
					CoreAnnotations.SentencesAnnotation.class
				)
			)
		);
	}

	@Override
	public void init(Map<String, Object> properties) {
	}

	@Override
	public String getDescription() {
		return "com.vader.sentiment.analyzer.SentimentAnalyzer";
	}

}
