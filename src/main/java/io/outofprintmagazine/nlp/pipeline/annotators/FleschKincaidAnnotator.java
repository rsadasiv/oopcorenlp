package io.outofprintmagazine.nlp.pipeline.annotators;

import java.math.BigDecimal;
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

public class FleschKincaidAnnotator extends AbstractAggregatePosAnnotator implements Annotator, OOPAnnotator {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(FleschKincaidAnnotator.class);
	
	public FleschKincaidAnnotator() {
		super();
		this.setTags(Arrays.asList("NNP", "NNPS", "FW"));
		this.setScorer((Scorer)new BigDecimalAvg(this.getAnnotationClass(), this.getAggregateClass()));
		this.setSerializer((Serializer)new BigDecimalSerializer(this.getAnnotationClass(), this.getAggregateClass()));
	}
	
	public FleschKincaidAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPFleschKincaidAnnotation.class;
	}
	
	@Override
	public Class getAggregateClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPFleschKincaidAnnotationAggregate.class;
	}
		
	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(
			new ArraySet<>(
				Arrays.asList(
					CoreAnnotations.TextAnnotation.class, 
					CoreAnnotations.TokensAnnotation.class,
					CoreAnnotations.SentencesAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPSyllablesAnnotation.class
				)
			)
		);
	}

	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		int wordCount = 0;
		int syllableCount = 0;
		int sentenceCount = 0;
		for (CoreSentence sentence : document.sentences()) {
			sentenceCount++;
			int sentenceWordCount = 0;
			int sentenceSyllableCount = 0;
			List<CoreLabel> tokens = sentence.tokens();

			for (int i = 0; i < tokens.size(); i++) {
				CoreLabel token = tokens.get(i);
				if (!isDictionaryWord(token)) {
					wordCount++;
					sentenceWordCount++;
					if (token.containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPSyllablesAnnotation.class)) {
						BigDecimal rawScore = (BigDecimal) token.get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPSyllablesAnnotation.class);
						syllableCount += rawScore.intValue();
						sentenceSyllableCount += rawScore.intValue();
					}
					else {
						syllableCount++;
					}
				}
			}
			double fk = (206.835 - (1.015*sentenceWordCount) - (84.6*sentenceSyllableCount/sentenceWordCount))/100;
			try {
				sentence.coreMap().set(getAnnotationClass(), new BigDecimal(fk));
			}
			catch (NumberFormatException nfe) {
				sentence.coreMap().set(getAnnotationClass(), new BigDecimal(0));
			}
		}

		double fk = (206.835 - (1.015*wordCount/sentenceCount) - (84.6*syllableCount/wordCount))/100;
        document.annotation().set(getAnnotationClass(), new BigDecimal(fk));
		score(document);
	}

	@Override
	public String getDescription() {
		return "(206.835 - (1.015*wordCount/sentenceCount) - (84.6*syllableCount/wordCount))/100";
	}
	
	/*
	 * 
	 * 100.00–90.00	5th grade	Very easy to read. Easily understood by an average 11-year-old student.
	 * 90.0–80.0	6th grade	Easy to read. Conversational English for consumers.
	 * 80.0–70.0	7th grade	Fairly easy to read.
	 * 70.0–60.0	8th & 9th grade	Plain English. Easily understood by 13- to 15-year-old students.
	 * 60.0–50.0	10th to 12th grade	Fairly difficult to read.
	 * 50.0–30.0	College	Difficult to read.
	 * 30.0–0.0		College graduate	Very difficult to read. Best understood by university graduates.
	 */
}
