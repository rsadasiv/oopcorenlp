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

public class VerbTenseAnnotator extends AbstractAggregatePosAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(VerbTenseAnnotator.class);

	private List<String> past = Arrays.asList("VBD","VBN");
	private List<String> present = Arrays.asList("VB","VBP","VBZ");
	private List<String> future = Arrays.asList("MD");
	private List<String> progressive = Arrays.asList("VBG");
	private List<String> infinitive = Arrays.asList("TO");
	
	public VerbTenseAnnotator() {
		super();
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass(), this.getAggregateClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass(), this.getAggregateClass()));
	}
	
	public VerbTenseAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbTenseAnnotation.class;
	}
	
	@Override
	public Class getAggregateClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbTenseAnnotationAggregate.class;
	}
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (int i=0;i<sentence.tokens().size();i++) {
				CoreLabel token = sentence.tokens().get(i);
				Map<String, BigDecimal> scoreMap = new HashMap<String, BigDecimal>();
				if (scoreLemma(token, past) != null) {
					scoreMap.put("past", new BigDecimal(1));
				}
				else if (scoreLemma(token, present) != null) {
					scoreMap.put("present", new BigDecimal(1));
				}
				else if (scoreLemma(token, progressive) != null) {
					scoreMap.put("progressive", new BigDecimal(1));
				}
				else if (scoreLemma(token, future) != null) {
					scoreMap.put("future", new BigDecimal(1));
				}
				else if (scoreLemma(token, infinitive) != null) {
					if (i < sentence.tokens().size() && sentence.tokens().get(i).tag().equals("VB")) {
						scoreMap.put("infinitive", new BigDecimal(1));
					}
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
		return "Verb tenses. past: VBD,VBN " + 
				"present: VB,VBP,VBZ " + 
				"future: MD " + 
				"progressive: VBG " + 
				"infinitive: TO";
	}
}
