package io.outofprintmagazine.nlp.pipeline.annotators;

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
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public class CommonWordsAnnotator extends AbstractAggregatePosAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(CommonWordsAnnotator.class);
	
	public CommonWordsAnnotator() {
		super();
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass(), this.getAggregateClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass(), this.getAggregateClass()));
		this.appendTagsFromFile("io/outofprintmagazine/nlp/models/COCA/Dolch.txt");
	}
	
	public CommonWordsAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}
	
	@Override
	public String getDescription() {
		return "io/outofprintmagazine/nlp/models/COCA/Dolch.txt";
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPCommonWordsAnnotation.class;
	}
	
	@Override
	public Class getAggregateClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPCommonWordsAnnotationAggregate.class;
	}
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (isDictionaryWord(token)) {
					if ((getTags().contains(token.lemma())) || (getTags().contains(token.lemma().toLowerCase()))) {
						Map<String, BigDecimal> scoreMap = new HashMap<String, BigDecimal>();
						scoreMap.put(token.lemma(), new BigDecimal(1));
						token.set(getAnnotationClass(), scoreMap);
					}
				}
			}
		}
		score(document);
	}
}
