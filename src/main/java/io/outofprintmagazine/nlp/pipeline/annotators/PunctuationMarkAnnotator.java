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

public class PunctuationMarkAnnotator extends AbstractAggregatePosAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(PunctuationMarkAnnotator.class);

	private Map<String,String> scoreLabelMap = new HashMap<String,String>();
	
	public PunctuationMarkAnnotator() {
		super();
		this.initScoreLabelMap();
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass(), this.getAggregateClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass(), this.getAggregateClass()));
	}
	
	public PunctuationMarkAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	protected void initScoreLabelMap() {
		scoreLabelMap.put("?", "Question");
		scoreLabelMap.put("??", "Question");
		scoreLabelMap.put("!", "Exclamation");
		scoreLabelMap.put(":", "Colon");
		scoreLabelMap.put(";", "Semicolon");
		scoreLabelMap.put(",", "Comma");
		scoreLabelMap.put("--", "Hyphen");
		scoreLabelMap.put("-", "Hyphen");
		scoreLabelMap.put(".", "Period");
		scoreLabelMap.put("\'", "Apostrophe");
		scoreLabelMap.put("\"", "Quotation");
	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPPunctuationMarkAnnotation.class;
	}
	
	@Override
	public Class getAggregateClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPPunctuationMarkAnnotationAggregate.class;
	}
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				Map<String, BigDecimal> scoreMap = new HashMap<String, BigDecimal>();
				if (isPunctuationMark(token)) {
					if (scoreLabelMap.get(token.originalText()) != null) {
						scoreMap.put(scoreLabelMap.get(token.originalText()), new BigDecimal(1));
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
		return "Question, Exclamation, Colon, Semicolon, Comma, Hypen, Period, Apostrophe, Quotation";
	}
}
