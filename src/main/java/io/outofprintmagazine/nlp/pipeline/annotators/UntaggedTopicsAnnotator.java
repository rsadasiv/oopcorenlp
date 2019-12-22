package io.outofprintmagazine.nlp.pipeline.annotators;

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
import io.outofprintmagazine.nlp.CoreNlpUtils;

public class UntaggedTopicsAnnotator extends AbstractPosAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(UntaggedTopicsAnnotator.class);
	
	public UntaggedTopicsAnnotator() {
		super();
		this.setTags(Arrays.asList("NNP", "NNPS"));
	}
	
	public UntaggedTopicsAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPUntaggedTopicsAnnotation.class;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				Map<String,BigDecimal> scoreMap = new HashMap<String,BigDecimal>();
				String score = scoreToken(token);
				if (score != null) {
					try {
						if (CoreNlpUtils.getInstance().getCoreEntityMentionFromToken(document, token).size() == 0) {
							scoreMap.put(score, new BigDecimal(1));
						}
					} 
					catch (Exception e) {
						logger.error(e);
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
		// TODO Auto-generated method stub
		return "Unused?";
	}

}
