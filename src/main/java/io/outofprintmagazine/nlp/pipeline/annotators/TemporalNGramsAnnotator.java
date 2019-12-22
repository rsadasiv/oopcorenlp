package io.outofprintmagazine.nlp.pipeline.annotators;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.NGramUtils;
import io.outofprintmagazine.nlp.NGramUtils.NGramPhraseScore;
import io.outofprintmagazine.nlp.NGramUtils.NGramScore;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public class TemporalNGramsAnnotator extends AbstractAggregatePosAnnotator implements Annotator, OOPAnnotator {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(TemporalNGramsAnnotator.class);

	
	public TemporalNGramsAnnotator() {
		super();
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass(), this.getAggregateClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass(), this.getAggregateClass()));
		this.appendTagsFromFile("io/outofprintmagazine/nlp/models/COCA/Dolch.txt");
	}
	
	public TemporalNGramsAnnotator(String name, Properties props) {
		this();
		properties = props;
	}
	
    public Map<String,BigDecimal> nGramScoreToTemporalAnnotation(NGramScore nGramScore) {
    	Map<String,BigDecimal> retval = new HashMap<String,BigDecimal>();
    	retval.put(nGramScoreToTemporalString(nGramScore), new BigDecimal(1));
    	return retval;
    }
    
    public String nGramScoreToTemporalString(NGramScore nGramScore) {
    	return nGramPhraseScoreToTemporalString(nGramScore.match);
    }
    
    public String nGramPhraseScoreToTemporalString(NGramPhraseScore nGramPhraseScore) {
		if (nGramPhraseScore == null) {
			return "novel";
		}
		else if (nGramPhraseScore.lastYear < 1900) {
			return "archaic";
		}
		else if (nGramPhraseScore.lastYear < 1950) {
			return "dated";
		}
		else if (nGramPhraseScore.firstYear > 1999) {
			return "millenial";
		}
		else if (nGramPhraseScore.firstYear > 1899) {
			return "modern";
		}
		else if (nGramPhraseScore.firstYear > 1799) {
			return "victorian";
		}
		else if (nGramPhraseScore.firstYear > 1699) {
			return "enlightenment";
		}
		else if (nGramPhraseScore.firstYear > 1599) {
			return "elizabethan";
		}
		else {
			return "core";
		}
    }
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		List<String> queries = new ArrayList<String>();
		for (int i=0;i<document.tokens().size();i++) {
			CoreLabel token = document.tokens().get(i);
			if (isDictionaryWord(token)) {
				if (!getTags().contains(token.lemma())) {
					queries.add(token.originalText().toLowerCase());
				}
			}
		}
		try {
			NGramUtils.getInstance().addToWordCache(queries);
			for (int i=0;i<document.tokens().size();i++) {
				CoreLabel token = document.tokens().get(i);
				try {
					if (isDictionaryWord(token)) {
						if (!getTags().contains(token.lemma())) {
							List<NGramPhraseScore> wordCache = NGramUtils.getInstance().getWordCache(token.originalText().toLowerCase());
							if (wordCache != null && wordCache.size() > 0) {
								String key = nGramPhraseScoreToTemporalString(wordCache.get(0));
								Map<String,BigDecimal> scoreMap = new HashMap<String,BigDecimal>();
								scoreMap.put(key, new BigDecimal(1));
								token.set(getAnnotationClass(), scoreMap);
							}
						}
					}
				}
				catch (Throwable t) {
					logger.error("wtf?", t);
				}
			}
			NGramUtils.getInstance().pruneWordCache();
		}
		catch (Exception e) {
			logger.error(e);
		}
		score(document);
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPTemporalNGramsAnnotation.class;
	}
	
	@Override
	public Class getAggregateClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPTemporalNGramsAnnotationAggregate.class;
	}	

	
	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(
			new ArraySet<>(
				Arrays.asList(
					CoreAnnotations.TextAnnotation.class, 
					CoreAnnotations.TokensAnnotation.class,
					CoreAnnotations.SentencesAnnotation.class
				)
			)
		);
	}
	
	@Override
	public void init(Map<String, Object> properties) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getDescription() {
		return "Google nGram first/last recorded date of token. "
				+ "not found: novel, "
				+ "last < 1900: archaic, "
				+ "last < 1950: dated, "
				+ "first > 1999: millenial, "
				+ "first > 1899: modern, "
				+ "first > 1799: victorian, "
				+ "first > 1699: enlightenment, "
				+ "first > 1599: elizabethan, "
				+ "else: core";
	}

}
