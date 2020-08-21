/*******************************************************************************
 * Copyright (C) 2020 Ram Sadasiv
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package io.outofprintmagazine.nlp.pipeline.annotators;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
import io.outofprintmagazine.nlp.pipeline.scorers.IScorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.ISerializer;
import io.outofprintmagazine.util.IParameterStore;

/**
 * <p>Base class for all custom annotators.</p>
 * <p>constructor called once, conventionally constructs delegates for IScorer and ISerializer</p>
 * <p>init called once, with IParameterStore</p>
 * <p>once per document:</p>
 * <p>annotate(), conventionally implemented in the subclass</p>
 * <p>score(), delegated to an instance of IScorer</p>
 * <p>serialize(), delegated to instance of ISerializer</p>
 * <p>serializeAggregateDocument(), delegated to instance of ISerializer</p>
*  @see IParameterStore
 * @see IScorer
 * @see ISerializer
 * 
 * @author Ram Sadasiv
 *
 */
public abstract class AbstractAnnotator implements Annotator, IOOPAnnotator {
	

	private static final Logger logger = LogManager.getLogger(AbstractAnnotator.class);
	
	@SuppressWarnings("unused")	
	private Logger getLogger() {
		return logger;
	}
	
	protected IScorer scorer;
	protected ISerializer serializer;
	protected IParameterStore parameterStore;
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
		this.setScorer((IScorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((ISerializer)new MapSerializer(this.getAnnotationClass()));
	}
	
	protected ISerializer getSerializer() {
		return serializer;
	}

	protected void setSerializer(ISerializer serializer) {
		this.serializer = serializer;
	}
	
	protected IScorer getScorer() {
		return scorer;
	}

	protected void setScorer(IScorer scorer) {
		this.scorer = scorer;
	}

	@Override
	public abstract Class getAnnotationClass();
	
	
	@Override
	public void init(IParameterStore parameterStore) {
		this.parameterStore = parameterStore;
	}
	
	protected IParameterStore getParameterStore() {
		return parameterStore;
	}
		
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
