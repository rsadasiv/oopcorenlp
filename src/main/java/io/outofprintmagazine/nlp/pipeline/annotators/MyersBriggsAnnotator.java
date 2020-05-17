package io.outofprintmagazine.nlp.pipeline.annotators;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public class MyersBriggsAnnotator extends AbstractPosAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(MyersBriggsAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}
	
	//VerbGroups
	private String extrovert = "social";
	private String introvert = "perception";
	private String sensing = "stative";
	private String intuitive = "change";
	private String thinking = "cognition";
	private String feeling = "emotion";
	//Biber features
	private String judging = "PRMD";
	private String perceiving = "NEMD";
	
	public MyersBriggsAnnotator() {
		super();
		this.setScorer((Scorer) new MapSum(this.getAnnotationClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass()));
	}
	
	public MyersBriggsAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}
	
	@Override
	public String getDescription() {
		return "Personality types. https://www.myersbriggs.org/my-mbti-personality-type/mbti-basics/";
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPMyersBriggsAnnotation.class;
	}
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			Map<String,BigDecimal> scoreMap = new HashMap<String,BigDecimal>();
			if (sentence.coreMap().containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbGroupsAnnotation.class)) {
				Map<String,BigDecimal> annotationScore = sentence.coreMap().get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbGroupsAnnotation.class);
				if (annotationScore.containsKey(extrovert)) {
					addToScoreMap(scoreMap, "extrovert", annotationScore.get(extrovert));
				}
				if (annotationScore.containsKey(introvert)) {
					addToScoreMap(scoreMap, "introvert", annotationScore.get(introvert));
				}
				if (annotationScore.containsKey(sensing)) {
					addToScoreMap(scoreMap, "sensing", annotationScore.get(sensing));
				}
				if (annotationScore.containsKey(intuitive)) {
					addToScoreMap(scoreMap, "intuitive", annotationScore.get(intuitive));
				}
				if (annotationScore.containsKey(thinking)) {
					addToScoreMap(scoreMap, "thinking", annotationScore.get(thinking));
				}
				if (annotationScore.containsKey(feeling)) {
					addToScoreMap(scoreMap, "feeling", annotationScore.get(feeling));
				}
			}
			if (sentence.coreMap().containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPBiberAnnotation.class)) {
				Map<String, BigDecimal> biberScoreMap = sentence.coreMap().get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPBiberAnnotation.class);
				if (biberScoreMap.containsKey(judging)) {
					addToScoreMap(scoreMap, "judging", biberScoreMap.get(judging));
				}
				if (biberScoreMap.containsKey(perceiving)) {
					addToScoreMap(scoreMap, "perceiving", biberScoreMap.get(perceiving));
				}
			}
			sentence.coreMap().set(getAnnotationClass(), scoreMap);
		}
		score(document);
	}
	
}
