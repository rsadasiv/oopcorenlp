package io.outofprintmagazine.nlp.pipeline.annotators;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.pipeline.scorers.BigDecimalAvg;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.BigDecimalSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public class CoreNlpSentimentAnnotator extends AbstractAnnotator implements Annotator, OOPAnnotator {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(CoreNlpSentimentAnnotator.class);
	
	public CoreNlpSentimentAnnotator() {
		super();
		this.setScorer((Scorer)new BigDecimalAvg(this.getAnnotationClass(), this.getAggregateClass()));
		this.setSerializer((Serializer)new BigDecimalSerializer(this.getAnnotationClass(), this.getAggregateClass()));
	}
	
	public CoreNlpSentimentAnnotator(String name, Properties props) {
		this();
		properties = props;
	}
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			String sentiment = sentence.sentiment();
			BigDecimal score = new BigDecimal(.5);
			if (sentiment.equals("Very negative")) {
				score = new BigDecimal(0);
			}
			else if (sentiment.equals("Negative")) {
				score = new BigDecimal(.25);
			}
			else if (sentiment.equals("Neutral")) {
				score = new BigDecimal(.5);
			}
			else if (sentiment.equals("Positive")) {
				score = new BigDecimal(.75);
			}
			else if (sentiment.equals("Very positive")) {
				score = new BigDecimal(1);
			}
			sentence.coreMap().set(getAnnotationClass(), score);
			for (CoreLabel token : sentence.tokens()) {
				if (token.containsKey(getAnnotationClass())) {
					token.set(getAnnotationClass(), score);
				}
			}
		}
		score(document);
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.CoreNlpSentimentAnnotation.class;
	}
	

	public Class getAggregateClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.CoreNlpSentimentAnnotationAggregate.class;
	}
	
	/*requirementsSatisfied() on SentimentAnnotator is broken. 
	 * public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
     * 	return Collections.emptySet();
  	 * }
	 * Set this to run after some more expensive annotators and hope for the best
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(
			new ArraySet<>(
				//Arrays.asList(edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentClass.class)
					Arrays.asList(
							CoreAnnotations.TextAnnotation.class, 
							CoreAnnotations.TokensAnnotation.class,
							CoreAnnotations.LemmaAnnotation.class,
							CoreAnnotations.SentencesAnnotation.class,
							CoreAnnotations.NamedEntityTagAnnotation.class,
							CoreAnnotations.NormalizedNamedEntityTagAnnotation.class,
							CoreAnnotations.CanonicalEntityMentionIndexAnnotation.class,
							CorefCoreAnnotations.CorefChainAnnotation.class,
							SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class
					)
			)
		);
	  }

	@Override
	public void init(Map<String, Object> properties) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDescription() {
		return "edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentClass scaled from 0 to 1";
	}

}
