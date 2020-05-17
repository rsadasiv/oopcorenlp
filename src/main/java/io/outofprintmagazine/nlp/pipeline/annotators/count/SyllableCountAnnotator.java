package io.outofprintmagazine.nlp.pipeline.annotators.count;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.util.ArraySet;
import eu.crydee.syllablecounter.SyllableCounter;
import io.outofprintmagazine.nlp.pipeline.annotators.AbstractAnnotator;
import io.outofprintmagazine.nlp.pipeline.annotators.OOPAnnotator;
import io.outofprintmagazine.nlp.pipeline.scorers.BigDecimalSumTokenScorer;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.BigDecimalSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public class SyllableCountAnnotator extends AbstractAnnotator implements Annotator, OOPAnnotator {
	
	private static final Logger logger = LogManager.getLogger(SyllableCountAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}

	private SyllableCounter syllableCounter = new SyllableCounter();
	
	public SyllableCountAnnotator() {
		super();
		this.setScorer((Scorer)new BigDecimalSumTokenScorer(this.getAnnotationClass()));
		this.setSerializer((Serializer)new BigDecimalSerializer(this.getAnnotationClass()));
	}
	
	public SyllableCounter getSyllableCounter() {
		return syllableCounter;
	}

	public void setSyllableCounter(SyllableCounter syllableCounter) {
		this.syllableCounter = syllableCounter;
	}
	
	public SyllableCountAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}

	@Override
	public void init(Map<String, Object> properties) {
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPSyllableCountAnnotation.class;
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
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				int syllables = getSyllableCounter().count(token.originalText());
				if (syllables < 1) {
					syllables = 1;
				}
				token.set(getAnnotationClass(), new BigDecimal(syllables));
			}
		}
		score(document);
	}

	@Override
	public String getDescription() {
		return "eu.crydee.syllablecounter.SyllableCounter";
	}

}
