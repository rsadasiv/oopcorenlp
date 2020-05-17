package io.outofprintmagazine.nlp.pipeline.annotators;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.pipeline.PhraseAnnotation;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public abstract class AbstractAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(AbstractAnnotator.class);
	
	protected Logger getLogger() {
		return logger;
	}
	
	protected Scorer scorer;
	protected Serializer serializer;
	protected Properties properties;
	protected List<String> punctuationMarks = Arrays.asList("``", "''","?", "??", "!", ":", ";", ",", "--", "-", ".", "\"", "`", "'", "‘", "’", "“", "”", ".", "*", "[", "]", "{", "}");
	//protected List<String> nonDictionaryPOS = Arrays.asList("NNP", "NNPS", "CD", "LS", "SYM", "POS", "FW");
	protected List<String> dictionaryPOS = Arrays.asList(
			"CC",
			"DT",
			"EX",
			"IN",
			"JJ",
			"JJR",
			"JJS",
			"MD",
			"NN",
			"NNS",
			"PRP",
			"PRP$",
			"RB",
			"RBR",
			"RBS",
			"RP",
			"TO",
			"UH",
			"VB",
			"VBD",
			"VBG",
			"VBN",
			"VBP",
			"VBZ",
			"WDT",
			"WP",
			"WP$",
			"WRB"
	);
	
	public AbstractAnnotator() {
		super();
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass()));
	}

	public AbstractAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	protected Serializer getSerializer() {
		return serializer;
	}

	protected void setSerializer(Serializer serializer) {
		this.serializer = serializer;
	}
	
	protected Scorer getScorer() {
		return scorer;
	}

	protected void setScorer(Scorer scorer) {
		this.scorer = scorer;
	}

	@Override
	public abstract Class getAnnotationClass();
	
	
	@Override
	public abstract void init(Map<String,Object> properties);
		
	@Override
	public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
		Set<Class<? extends CoreAnnotation>> retval = new ArraySet<Class<? extends CoreAnnotation>>();
		retval.add(getAnnotationClass());
		return retval;
	}

	@Override
	public abstract Set<Class<? extends CoreAnnotation>> requires();
	
	@Override
	public abstract void annotate(Annotation annotation);
	
	@Override
	public void score(CoreDocument document) {
		getScorer().score(document);
	}
	
	@Override
	public void serialize(CoreDocument document, ObjectNode json) {
		getSerializer().serialize(document, json);
	}
	
	@Override
	public void serializeAggregateDocument(CoreDocument document, ObjectNode json) {
		getSerializer().serializeAggregate(getScorer().aggregateDocument(document), json);
	}
	
	public boolean isPunctuationMark(CoreLabel token) {
		return punctuationMarks.contains(token.lemma());
	}
	
	public boolean hasPunctuationMark(CoreLabel token) {
		for (String punctuationMark : punctuationMarks) {
			if (token.lemma().contains(punctuationMark)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isDictionaryWord(CoreLabel token) {
		if (isPunctuationMark(token) || hasPunctuationMark(token)) {
			return false;
		}
		return dictionaryPOS.contains(token.tag());
	}
	
	public String toAlphaNumeric(String s) {
		return s.replaceAll("[^A-Za-z0-9_\\s]", "");
	}
	
	public void addToScoreMap(Map<String,BigDecimal> scoreMap, String key, BigDecimal score) {
		String strippedKey = toAlphaNumeric(key);
		if (strippedKey.length() > 0) {
			BigDecimal existingScore = scoreMap.get(strippedKey);
			if (existingScore != null) {
				scoreMap.put(strippedKey, existingScore.add(score));
			}
			else {
				scoreMap.put(strippedKey, score);
			}
		}
	}
	
	protected void addToScoreList(List<PhraseAnnotation> scoreMap, PhraseAnnotation p) {
		boolean updatedExisting = false;
		for (PhraseAnnotation scorePair : scoreMap) {
			if (scorePair.getName().equals(p.getName())) {
				scorePair.setValue(scorePair.getValue().add(p.getValue()));
				updatedExisting = true;
				break;
			}
		}
		if (!updatedExisting) {
			scoreMap.add(new PhraseAnnotation(p));
		}
	}

	
	public List<CoreLabel> getContextWords(CoreDocument document, CoreLabel token) {
		List<CoreLabel> retval = new ArrayList<CoreLabel>();
		int sentenceIndex = token.sentIndex();
		for (CoreLabel sentenceToken : document.sentences().get(sentenceIndex).tokens()) {
			if (isDictionaryWord(sentenceToken)) {
				retval.add(sentenceToken);
			}
		}
		return retval;
	}
}
