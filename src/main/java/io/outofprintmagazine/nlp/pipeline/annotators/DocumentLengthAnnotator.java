package io.outofprintmagazine.nlp.pipeline.annotators;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.ParagraphIndexAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.util.ArraySet;

public class DocumentLengthAnnotator extends AbstractAnnotator implements Annotator, OOPAnnotator {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(DocumentLengthAnnotator.class);

	
	public DocumentLengthAnnotator() {
		super();
	}
	
	public DocumentLengthAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPDocumentLengthAnnotation.class;
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
		Map<String, BigDecimal> scoreMap = new HashMap<String, BigDecimal>();
		int characterCount = 0;
		int wordCount = 0;
		int tokenCount = 0;
		int sentenceCount = 0;
		int paragraphCount = 0;
		
		for (CoreSentence sentence : document.sentences()) {
			sentenceCount++;
			paragraphCount = sentence.coreMap().get(ParagraphIndexAnnotation.class);
			for (CoreLabel token : sentence.tokens()) {
				characterCount+=token.originalText().length();
				tokenCount++;
				if (!isPunctuationMark(token)) {
					wordCount++;
				}
			}
		}
        scoreMap.put("characterCount", new BigDecimal(characterCount));
        scoreMap.put("tokenCount", new BigDecimal(tokenCount));
        scoreMap.put("wordCount", new BigDecimal(wordCount));
        scoreMap.put("sentenceCount", new BigDecimal(sentenceCount));
        scoreMap.put("paragraphCount", new BigDecimal(paragraphCount+1));
        document.annotation().set(getAnnotationClass(), scoreMap);
		score(document);
	}

	@Override
	public void score(CoreDocument document) {
		//pass
	}

	@Override
	public String getDescription() {
		return "characterCount, tokenCount, wordCount, sentenceCount, paragraphCount at document level.";
	}

}
