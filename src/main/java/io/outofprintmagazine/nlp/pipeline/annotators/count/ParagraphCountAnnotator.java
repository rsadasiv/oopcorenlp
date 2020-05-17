package io.outofprintmagazine.nlp.pipeline.annotators.count;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.ParagraphIndexAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.pipeline.annotators.AbstractAnnotator;
import io.outofprintmagazine.nlp.pipeline.annotators.OOPAnnotator;
import io.outofprintmagazine.nlp.pipeline.scorers.BigDecimalSumDocumentScorer;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.BigDecimalSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public class ParagraphCountAnnotator extends AbstractAnnotator implements Annotator, OOPAnnotator {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(ParagraphCountAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}

	public ParagraphCountAnnotator() {
		super();
		this.setScorer((Scorer) new BigDecimalSumDocumentScorer(this.getAnnotationClass()));
		this.setSerializer((Serializer) new BigDecimalSerializer(this.getAnnotationClass()));
	}
	
	public ParagraphCountAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPParagraphCountAnnotation.class;
	}
		
	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(
			new ArraySet<>(
				Arrays.asList(
					CoreAnnotations.TextAnnotation.class, 
					CoreAnnotations.TokensAnnotation.class,
					CoreAnnotations.SentencesAnnotation.class,
					CoreAnnotations.ParagraphIndexAnnotation.class
				)
			)
		);
	}

	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		annotation.set(
				getAnnotationClass(), 
				new BigDecimal(
						document.sentences().get(document.sentences().size()-1).coreMap().get(ParagraphIndexAnnotation.class)+1
				)
		);
	}

	@Override
	public String getDescription() {
		return "paragraphCount";
	}

}
