package io.outofprintmagazine.nlp.pipeline.annotators;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import io.outofprintmagazine.nlp.pipeline.scorers.BigDecimalSumSentenceScorer;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.BigDecimalSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public class VerblessSentencesAnnotator extends AbstractPosAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(VerblessSentencesAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}
	
	public VerblessSentencesAnnotator() {
		super();
		this.setTags(Arrays.asList("VB","VBD","VBG","VBN","VBP","VBZ","MD"));
		this.setScorer((Scorer) new BigDecimalSumSentenceScorer(this.getAnnotationClass()));
		this.setSerializer((Serializer) new BigDecimalSerializer(this.getAnnotationClass()));
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
				sentence.coreMap().set(getAnnotationClass(), new BigDecimal(1));
			}
		}
		score(document);
	}

	@Override
	public String getDescription() {
		return "VB,VBD,VBG,VBN,VBP,VBZ,MD not in sentence.";
	}
		
}
