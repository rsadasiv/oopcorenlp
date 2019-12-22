package io.outofprintmagazine.nlp.pipeline.annotators;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import io.outofprintmagazine.nlp.WiktionaryUtils;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public class WordlessWordsAnnotator extends AbstractAggregatePosAnnotator implements Annotator, OOPAnnotator {
	
	private static final Logger logger = LogManager.getLogger(WordlessWordsAnnotator.class);
	
	public WordlessWordsAnnotator() {
		super();
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass(), this.getAggregateClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass(), this.getAggregateClass()));
		this.appendTagsFromFile("io/outofprintmagazine/nlp/models/COCA/Dolch.txt");
	}
	
	public WordlessWordsAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPWordlessWordsAnnotation.class;
	}
	
	@Override
	public Class getAggregateClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPWordlessWordsAnnotationAggregate.class;
	}
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		List<String> queries = new ArrayList<String>();
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (isDictionaryWord(token)) {
					if (!getTags().contains(token.lemma())) {
						queries.add(token.originalText().toLowerCase());
					}
				}
			}
		}
		try {
			WiktionaryUtils.getInstance().addToWordCache(queries);
			for (int i=0;i<document.tokens().size();i++) {
				CoreLabel token = document.tokens().get(i);
				try {
					if (isDictionaryWord(token)) {
						if (!getTags().contains(token.lemma())) {
							String wordCache = WiktionaryUtils.getInstance().getWordCache(token.originalText().toLowerCase());
							if (wordCache == null) {
								Map<String,BigDecimal> scoreMap = new HashMap<String,BigDecimal>();
								scoreMap.put(token.lemma(), new BigDecimal(1));
								token.set(getAnnotationClass(), scoreMap);
							}
						}
					}
				}
				catch (Throwable t) {
					logger.error("wtf?", t);
				}
			}
			WiktionaryUtils.getInstance().pruneWordCache();
		}
		catch (Exception e) {
			logger.error(e);
		}

		score(document);
	}

	@Override
	public String getDescription() {
		return "Wiktionary lookup is null";
	}
	
}
