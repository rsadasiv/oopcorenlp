package io.outofprintmagazine.nlp.pipeline.annotators;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
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
import io.outofprintmagazine.nlp.WordnetUtils;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public class NounGroupsAnnotator extends AbstractPosAnnotator implements Annotator, OOPAnnotator{
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(NounGroupsAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}
	
	public NounGroupsAnnotator() {
		super();
		this.setTags(Arrays.asList("NN","NNS"));
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass()));
	}
	
	public NounGroupsAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounGroupsAnnotation.class;
	}
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (int i=0;i<document.sentences().size();i++) {
			CoreSentence sentence = document.sentences().get(i);			
			for (CoreLabel token : sentence.tokens()) {
				if (getTags().contains(token.tag())) {
					try {
						String score = WordnetUtils.getInstance().getLexicalFileName(token, getContextWords(document, token));
						if (score != null) {
							if (score.lastIndexOf('.') > -1) {
								score = score.substring(score.lastIndexOf('.')+1);
							}
							Map<String,BigDecimal> scoreMap = new HashMap<String,BigDecimal>();
							addToScoreMap(scoreMap, score, new BigDecimal(1));
							token.set(getAnnotationClass(), scoreMap);
						}
					} 
					catch (IOException e) {
						logger.error(e);
					}
				}
			}
		}
		score(document);
	}

	@Override
	public String getDescription() {
		return "Wordnet lexical file name. https://wordnet.princeton.edu/documentation/lexnames5wn.";
	}
}
