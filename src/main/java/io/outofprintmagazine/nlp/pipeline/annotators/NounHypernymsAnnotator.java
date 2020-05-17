package io.outofprintmagazine.nlp.pipeline.annotators;

import java.io.IOException;
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
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.WordnetUtils;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

import java.math.BigDecimal;

public class NounHypernymsAnnotator extends AbstractPosAnnotator implements Annotator, OOPAnnotator{
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(NounHypernymsAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}
	
	public NounHypernymsAnnotator() {
		super();
		this.setTags(Arrays.asList("NN","NNS"));
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass()));		
	}
	
	public NounHypernymsAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounHypernymsAnnotation.class;
	}

	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(
			new ArraySet<>(
				Arrays.asList(
					CoreAnnotations.TextAnnotation.class, 
					CoreAnnotations.TokensAnnotation.class,
					CoreAnnotations.SentencesAnnotation.class, 
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounsAnnotation.class
				)
			)
		);
	}	
	

	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		Map<String,BigDecimal> allNouns = new HashMap<String,BigDecimal>();
		if (annotation.containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounsAnnotation.class)) {
			allNouns = annotation.get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounsAnnotation.class);
		}
		else {
			logger.error("requires() not satisfied");
		}
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				String score = scoreLemma(token);
				if (score != null) {
					try {
						Map<String, BigDecimal> scoreMap = WordnetUtils.getInstance().scoreTokenHypernym(
								token, 
								getContextWords(document, token), 
								allNouns
						);
						if (scoreMap.size() > 0) {
							token.set(getAnnotationClass(), scoreMap);
						}
					} 
					catch (IOException e) {
						logger.error(e);
					}
				}
			}
		}
		score(document);
	}

	@Override
	public String getDescription() {
		return "Wordnet most likely hypernym based on hyponym match against all nouns.";
	}

}
