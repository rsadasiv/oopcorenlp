package io.outofprintmagazine.nlp.pipeline.annotators;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import io.outofprintmagazine.nlp.ResourceUtils;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public class AngliciseAnnotator extends AbstractAggregatePosAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(AngliciseAnnotator.class);
	
	public AngliciseAnnotator() {
		super();
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass(), this.getAggregateClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass(), this.getAggregateClass()));
	}
	
	public AngliciseAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}
	
	@Override
	public String getDescription() {
		return "io/outofprintmagazine/nlp/models/EN_GB/american_spellings.json";
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAngliciseAnnotation.class;
	}
	
	@Override
	public Class getAggregateClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAngliciseAnnotationAggregate.class;
	}
	
	@Override
	public void annotate(Annotation annotation) {
		try {
			Map<String,String> dictionary = ResourceUtils.getInstance().getDictionary("io/outofprintmagazine/nlp/models/EN_GB/american_spellings.json");
			CoreDocument document = new CoreDocument(annotation);
			for (CoreSentence sentence : document.sentences()) {
				for (CoreLabel token : sentence.tokens()) {
					String score = dictionary.get(token.lemma());
					if (score != null) {
						Map<String, BigDecimal> scoreMap = new HashMap<String, BigDecimal>();
						scoreMap.put(score, new BigDecimal(1));
						token.set(getAnnotationClass(), scoreMap);
					}
				}
			}
			score(document);
		}
		catch (IOException e) {
			logger.error(e);
		}
	}
}
