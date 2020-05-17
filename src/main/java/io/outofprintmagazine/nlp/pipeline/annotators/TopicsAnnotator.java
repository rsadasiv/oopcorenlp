package io.outofprintmagazine.nlp.pipeline.annotators;

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
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public class TopicsAnnotator extends AbstractPosAnnotator implements Annotator, OOPAnnotator{
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(TopicsAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}
	
	private List<String> nouns = Arrays.asList("NN","NNS");
	private List<String> properNouns = Arrays.asList("NNP", "NNPS");
	
	public TopicsAnnotator() {
		super();
		List<String> allTags = new ArrayList<String>();
		allTags.addAll(nouns);
		allTags.addAll(properNouns);
		this.setTags(allTags);
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass()));		
	}
	
	public TopicsAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPTopicsAnnotation.class;
	}

	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(
			new ArraySet<>(
				Arrays.asList(
					CoreAnnotations.TokensAnnotation.class,
					CoreAnnotations.SentencesAnnotation.class, 
					CoreAnnotations.PartOfSpeechAnnotation.class,
					CoreAnnotations.LemmaAnnotation.class
				)
			)
		);
	}	
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				Map<String,BigDecimal> scoreMap = new HashMap<String,BigDecimal>();
//				if (nouns.contains(token.tag())) {
//					scoreMap.put(token.lemma(), new BigDecimal(1));
//				}
				if (properNouns.contains(token.tag())) {
					addToScoreMap(scoreMap, token.lemma(), new BigDecimal(1));
				}
				if (scoreMap.size() > 0) {	
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
		score(document);
	}
	
	@Override
	public String getDescription() {
		return "NNP, NNPS";
	}

}
