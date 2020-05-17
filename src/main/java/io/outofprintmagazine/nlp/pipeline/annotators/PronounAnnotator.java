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

public class PronounAnnotator extends AbstractPosAnnotator implements Annotator, OOPAnnotator{
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(PronounAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}

	public PronounAnnotator() {
		super();
		this.setTags(Arrays.asList("PRP"));
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass()));
	}
	
	public PronounAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPPronounAnnotation.class;
	}
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (int sentenceIdx = 0; sentenceIdx < document.sentences().size(); sentenceIdx++) {
			CoreSentence sentence = document.sentences().get(sentenceIdx);
			List<CoreLabel> tokens = sentence.tokens();
			for (int i = 0; i < tokens.size(); i++) {
				CoreLabel token = tokens.get(i);
				Map<String, BigDecimal> scoreMap = new HashMap<String, BigDecimal>();
				if (!token.lemma().endsWith("self") && !token.lemma().endsWith("selves") && !token.lemma().equals("'s")) {
					String score = scoreLemma(token);
					if (score != null) {
						addToScoreMap(scoreMap, score, new BigDecimal(1));
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
		return "PRP minus -self, -selves, -'s";
	}

}
