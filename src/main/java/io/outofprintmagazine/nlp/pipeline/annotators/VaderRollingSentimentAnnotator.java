package io.outofprintmagazine.nlp.pipeline.annotators;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
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
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.pipeline.scorers.BigDecimalAvg;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.BigDecimalSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public class VaderRollingSentimentAnnotator extends AbstractAggregatePosAnnotator implements Annotator, OOPAnnotator {

	private static final Logger logger = LogManager.getLogger(VaderRollingSentimentAnnotator.class);
	private static final int rollingPeriod = 4;
	
	public VaderRollingSentimentAnnotator() {
		super();
		this.setScorer((Scorer)new BigDecimalAvg(this.getAnnotationClass(), this.getAggregateClass()));
		this.setSerializer((Serializer)new BigDecimalSerializer(this.getAnnotationClass(), this.getAggregateClass()));
	}
	
	public VaderRollingSentimentAnnotator(String name, Properties props) {
		this();
		properties = props;
	}
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);

		if (!annotation.containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.VaderSentimentAnnotation.class)) {
			logger.error("requires() not satisfied");
		}
		Deque<BigDecimal> deque = new LinkedList<BigDecimal>();
        for (CoreSentence sentence : document.sentences()) {
        	BigDecimal score = new BigDecimal(.5);
    		if (sentence.coreMap().containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.VaderSentimentAnnotation.class)) {
    			deque.addFirst(sentence.coreMap().get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.VaderSentimentAnnotation.class));
    			while (deque.size() > rollingPeriod) {
    				deque.removeLast();
    			}
    			BigDecimal total = new BigDecimal(0);
    			for (BigDecimal previousScore : deque) {

    				total = total.add(previousScore);
    			}
    			score = total.divide(new BigDecimal(deque.size()), 10, RoundingMode.HALF_DOWN);
    		}
    		sentence.coreMap().set(getAnnotationClass(), score);
        }
		score(document);
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.VaderRollingSentimentAnnotation.class;
	}
	
	@Override
	public Class getAggregateClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.VaderRollingSentimentAnnotationAggregate.class;
	}

	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(
			new ArraySet<>(
				Arrays.asList(
					CoreAnnotations.TextAnnotation.class, 
					CoreAnnotations.TokensAnnotation.class,
					CoreAnnotations.SentencesAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.VaderSentimentAnnotation.class
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
