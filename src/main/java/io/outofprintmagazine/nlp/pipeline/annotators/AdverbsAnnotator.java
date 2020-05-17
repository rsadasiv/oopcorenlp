package io.outofprintmagazine.nlp.pipeline.annotators;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public class AdverbsAnnotator extends AbstractPosAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(AdverbsAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}
	
	private List<String> posTags = Arrays.asList("RB", "RBR", "RBS");
	
	public AdverbsAnnotator() {
		super();
		this.appendTagsFromFile("io/outofprintmagazine/nlp/models/Adverbs.txt");
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass()));		
	}
	
	public AdverbsAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdverbsAnnotation.class;
	}
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (posTags.contains(token.tag())) {
					Map<String, BigDecimal> scoreMap = new HashMap<String, BigDecimal>();
					if (!getTags().contains(token.lemma().toLowerCase())) {
						addToScoreMap(scoreMap, token.lemma(), new BigDecimal(1));
					}
					if (scoreMap.size() > 0) {
						token.set(getAnnotationClass(), scoreMap);
					}
				}
			}
		}
		score(document);
	}

	@Override
	public String getDescription() {
		return "RB, RBR, RBS not in io/outofprintmagazine/nlp/models/Adverbs.txt";
	}
}
