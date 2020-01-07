package io.outofprintmagazine.nlp.pipeline.annotators;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;
import io.outofprintmagazine.util.DocumentAggregateScore;

public class VerblessSentencesAnnotator extends AbstractAggregatePosAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(VerblessSentencesAnnotator.class);
	
	public VerblessSentencesAnnotator() {
		super();
		this.setTags(Arrays.asList("VB","VBD","VBG","VBN","VBP","VBZ","MD"));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass(), this.getAggregateClass()));
	}
	
	public VerblessSentencesAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerblessSentencesAnnotation.class;
	}
	
	@Override
	public Class getAggregateClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerblessSentencesAnnotationAggregate.class;
	}

	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			boolean shouldScore = true;
			for (CoreLabel token : sentence.tokens()) {
				if (scoreTag(token) != null) {
					shouldScore = false;
					break;
				}
			}
			if (shouldScore) {
				Map<String, BigDecimal> scoreMap = new HashMap<String, BigDecimal>();
				scoreMap.put(sentence.text(), new BigDecimal(1));
			}
		}
		score(document);
	}

	@Override
	public String getDescription() {
		return "VB,VBD,VBG,VBN,VBP,VBZ,MD not in sentence.";
	}
	
	@Override
	public void score(CoreDocument document) {
		scoreDocument(document);
		scoreDocumentAggregate(document);
	}


	public void scoreDocument(CoreDocument document) {
		if (! document.annotation().containsKey(getAnnotationClass())) {
			ArrayList<Map<String, BigDecimal>> rawScores = new ArrayList<Map<String, BigDecimal>>();
			for (CoreSentence sentence : document.sentences()) {
				if (sentence.coreMap().containsKey(getAnnotationClass())) {
					rawScores.add((Map<String, BigDecimal>) sentence.coreMap().get(getAnnotationClass()));
				}
			}
			document.annotation().set(getAnnotationClass(), aggregateScores(rawScores));
		}
	}
	
	public void scoreDocumentAggregate(CoreDocument document) {
		if (document.annotation().containsKey(getAnnotationClass())) {
			document.annotation().set(
				getAggregateClass(),
				new DocumentAggregateScore(
					getAnnotationClass().getCanonicalName() + "Aggregate", 
					(Map<String, BigDecimal>) document.annotation().get(getAnnotationClass()),
					new BigDecimal(document.tokens().size())
				)
			);
		}
	}

	public Map<String, BigDecimal> aggregateScores(List<Map<String, BigDecimal>> allScores) {
		Map<String, BigDecimal> scoreMap = new HashMap<String, BigDecimal>();
		for (Map<String, BigDecimal> rawScore : allScores) {
			for (String key : rawScore.keySet()) {
				BigDecimal existingScore = scoreMap.get(key);
				if (existingScore == null) {
					scoreMap.put(key, rawScore.get(key));
				} else {
					scoreMap.put(key, existingScore.add(rawScore.get(key)));
				}
			}
		}
		return scoreMap;
	}

	
}
