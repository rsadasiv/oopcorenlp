package io.outofprintmagazine.nlp.pipeline.annotators;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreQuote;
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.pipeline.scorers.MapListScorer;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapListSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public class QuotesAnnotator extends AbstractListAggregatePosAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(QuotesAnnotator.class);

	public QuotesAnnotator() {
		super();
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass(), this.getAggregateClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass(), this.getAggregateClass()));
		this.setListScorer((Scorer)new MapListScorer(this.getListClass()));
		this.setListSerializer((Serializer)new MapListSerializer(this.getListClass()));
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
	public Class getListClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPQuotesAnnotationList.class;
	}

	@Override
	public Class getAggregateClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPQuotesAnnotationAggregate.class;
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
			int quoteStartIdx = quote.quoteCharOffsets().first().intValue();
			for (CoreLabel token : document.tokens()) {
				if (token.beginPosition() == quoteStartIdx) {
					Map<String, List<String>> scoreMap = new HashMap<String, List<String>>();
					String speaker = "anon";
					if (quote.hasCanonicalSpeaker) {
						speaker = quote.canonicalSpeaker().get();
					}
					scoreMap.put(speaker, Arrays.asList(quote.text()));
					token.set(getListClass(), scoreMap);
					Map<String,BigDecimal> score = new HashMap<String,BigDecimal>();
					score.put(speaker, new BigDecimal(1));
					token.set(getAnnotationClass(), score);
					continue;
				}
			}
		}
		score(document);
	}

	@Override
	public String getDescription() {
		return "CoreNLP: QuoteAnnotator plus NER speaker.";
	}
}
