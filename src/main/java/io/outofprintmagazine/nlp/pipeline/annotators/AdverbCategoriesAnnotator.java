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

public class AdverbCategoriesAnnotator extends AbstractAggregatePosAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(AdverbCategoriesAnnotator.class);

	private List<String> descriptive = Arrays.asList("RB");
	private List<String> comparative = Arrays.asList("RBR");
	private List<String> superlative = Arrays.asList("RBS");
	
	public AdverbCategoriesAnnotator() {
		super();
		this.appendTagsFromFile("io/outofprintmagazine/nlp/models/Adverbs.txt");
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass(), this.getAggregateClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass(), this.getAggregateClass()));			
	}
	
	public AdverbCategoriesAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdverbCategoriesAnnotation.class;
	}
	
	@Override
	public Class getAggregateClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdverbCategoriesAnnotationAggregate.class;
	}
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (!getTags().contains(token.lemma().toLowerCase())) {
					Map<String, BigDecimal> scoreMap = new HashMap<String, BigDecimal>();
					if (scoreLemma(token, descriptive) != null) {
						scoreMap.put("descriptive", new BigDecimal(1));
					}
					else if (scoreLemma(token, comparative) != null) {
						scoreMap.put("comparative", new BigDecimal(1));
					}
					else if (scoreLemma(token, superlative) != null) {
						scoreMap.put("superlative", new BigDecimal(1));
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
		return "Fast-Faster-Fasest. RB, RBR, RBS: descriptive, comparative, superlative.";
	}

}
