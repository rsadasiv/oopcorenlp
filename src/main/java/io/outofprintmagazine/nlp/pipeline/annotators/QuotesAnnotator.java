package io.outofprintmagazine.nlp.pipeline.annotators;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreQuote;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.pipeline.PhraseAnnotation;
import io.outofprintmagazine.nlp.pipeline.scorers.PhraseScorer;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.PhraseSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public class QuotesAnnotator extends AbstractAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(QuotesAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}

	public QuotesAnnotator() {
		super();
		this.setScorer((Scorer) new PhraseScorer(this.getAnnotationClass()));
		this.setSerializer((Serializer) new PhraseSerializer(this.getAnnotationClass()));
	}
	
	public QuotesAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPQuotesAnnotation.class;
	}
	
	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(
			new ArraySet<>(
				Arrays.asList(
					CoreAnnotations.QuotationsAnnotation.class,
					CoreAnnotations.NamedEntityTagAnnotation.class, 
					CoreAnnotations.NormalizedNamedEntityTagAnnotation.class,
					CoreAnnotations.CanonicalEntityMentionIndexAnnotation.class
				)
			)
		);
	}
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreQuote quote : document.quotes()) {
			CoreSentence sentence = document.sentences().get(quote.coreMap().get(CoreAnnotations.SentenceBeginAnnotation.class));
			List<PhraseAnnotation> scoreMap = new ArrayList<PhraseAnnotation>();
			addToScoreList(scoreMap, new PhraseAnnotation(quote.text().replaceAll("\"", ""), new BigDecimal(1)));
			sentence.coreMap().set(getAnnotationClass(), scoreMap);
		}
		score(document);
	}

	@Override
	public String getDescription() {
		return "CoreNLP: QuoteAnnotator plus NER speaker.";
	}

}
