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
package io.outofprintmagazine.nlp.utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.CorefChain.CorefMention;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.TypedDependency;
import io.outofprintmagazine.util.ParameterStore;

public class CoreNlpUtils {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(CoreNlpUtils.class);
	
    private ParameterStore parameterStore;

	private CoreNlpUtils(ParameterStore parameterStore) {
		super();
		this.parameterStore = parameterStore; 
	}
	
	private static Map<ParameterStore, CoreNlpUtils> instances = new HashMap<ParameterStore, CoreNlpUtils>();
	
    public static CoreNlpUtils getInstance(ParameterStore parameterStore) throws IOException { 
        if (instances.get(parameterStore) == null) {
        	CoreNlpUtils instance = new CoreNlpUtils(parameterStore);
            instances.put(parameterStore, instance);
        }
        return instances.get(parameterStore); 
    }

	private StanfordCoreNLP pipeline = null;

	public static List<String> coreNlpAnnotatorAliases = Arrays.asList(
			"tokenize"
		    , "ssplit"
		    , "pos"
		    , "lemma"
		    , "ner"
		    , "parse"
		    , "sentiment"
		    , "depparse"
		    , "coref"
		    , "quote"	
	);

	public Properties getPipelineProps() {
		// set up pipeline properties
		Properties props = new Properties();

		//props.setProperty("dcoref.maxdist", "2");
		props.setProperty("coref.algorithm", "statistical");
		props.setProperty("coref.maxMentionDistance", "15");
		props.setProperty("coref.maxMentionDistanceWithStringMatch", "50");
		props.setProperty("coref.statisical.pairwiseScoreThresholds", ".15");
		//props.setProperty("openie.resolve_coref", "true");

		props.setProperty("pos.maxlen", "70");
		props.setProperty("ner.maxlen", "70");
		props.setProperty("ner.applyFineGrained", "false");
		props.setProperty("ner.useSUTime", "true");
		props.setProperty("ner.applyNumericClassifiers", "true");
		props.setProperty("ner.combinationMode", "NORMAL");
		props.setProperty("ner.additional.regexner.mapping", "io/outofprintmagazine/nlp/models/NER/regexner.txt");
		props.setProperty("quote.maxLength", "70");
		props.setProperty("quote.singleQuotes", "true");
		props.setProperty("quote.asciiQuotes", "true");
		props.setProperty("quote.attributeQuotes", "true");
		props.setProperty("enforceRequirements", "true");
	    props.setProperty("parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz");
		props.setProperty("parse.maxlen", "70");
		//props.setProperty("threads", "3");
		//props.setProperty("tokenize.options", "asciiQuotes=true,americanize=true");
		
		StringBuffer annotators = new StringBuffer();
		for (String coreNlpAnnotatorAlias : coreNlpAnnotatorAliases) {
			annotators.append(coreNlpAnnotatorAlias);
			annotators.append(",");
		}
		props.setProperty(
				"annotators",
				annotators.substring(0, annotators.length()-1)
			);
		
		return props;
	}
		
	public StanfordCoreNLP getPipeline() {
		if (pipeline == null) {
			pipeline = new StanfordCoreNLP(getPipelineProps());
		}
		return pipeline;
	}
	
	public List<CoreEntityMention> getCoreEntityMentionFromToken(CoreDocument document, CoreLabel token) {
		List<CoreEntityMention> retval = new ArrayList<CoreEntityMention>();
		CoreSentence sentence = getSentenceFromToken(document, token);
		List<CoreEntityMention> entityMentions = sentence.entityMentions();
		for (CoreEntityMention entityMention : entityMentions) {
			for (CoreLabel t : getTokensFromCoreEntityMention(document, entityMention)) {
				if (t.equals(token)) {
					retval.add(entityMention);
				}
			}
		}
		return retval;
	}
	
	
	public List<CoreEntityMention> getCoreEntityMentionFromCorefMention(CoreDocument document, CorefMention corefMention) {
		List<CoreEntityMention> retval = new ArrayList<CoreEntityMention>();
		List<CoreLabel> tokens = getTokensFromCorefMention(document, corefMention);
		for (CoreLabel token : tokens) {
			retval.addAll(getCoreEntityMentionFromToken(document, token));
		}
		return retval;
	}
	
	public boolean isCoreEntityMentionInAnyCorefChain(CoreDocument document, CoreEntityMention entityMention) {
		if (getCorefChainFromCoreEntityMention(document, entityMention) == null) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public CorefChain getCorefChainFromCoreEntityMention(CoreDocument document, CoreEntityMention entityMention) {
	    for (CorefChain corefChain : document.annotation().get(CorefCoreAnnotations.CorefChainAnnotation.class).values()) {
		    for (CorefMention corefMention : corefChain.getMentionsInTextualOrder() ) {
		    	//indexes hosed up?
		    	try {
		    		boolean afterStart = false;
		    		if (corefMention.sentNum-1 < document.sentences().size()) {
		    			if (corefMention.startIndex < document.sentences().get(corefMention.sentNum-1).tokens().size()) {
		    				afterStart = (entityMention.charOffsets().first.intValue() >= document.sentences().get(corefMention.sentNum-1).tokens().get(corefMention.startIndex).beginPosition());
		    			}
		    		}
		    		boolean beforeEnd = false;
		    		if (corefMention.sentNum-1 < document.sentences().size()) {
		    			if (corefMention.endIndex < document.sentences().get(corefMention.sentNum-1).tokens().size()) {
		    				beforeEnd = (entityMention.charOffsets().second.intValue() <= document.sentences().get(corefMention.sentNum-1).tokens().get(corefMention.endIndex).endPosition());
		    			}
		    		}
		    				
		    		if (afterStart && beforeEnd) {
		    			return corefChain;
		    		}
		    	}
		    	catch (Throwable t) {
		    		logger.debug("coref sentNum: " + corefMention.sentNum);
		    		logger.debug("coref start: " + corefMention.startIndex);
		    		logger.debug("mention start: " + entityMention.charOffsets().first.intValue());
		    		logger.debug("mention end: " + entityMention.charOffsets().second.intValue());
		    		logger.debug("sentence: " + document.sentences().get(corefMention.sentNum-1));
		    		logger.error("wtf", t);
		    		throw (t);
		    	}
	    	}
	    }
	    return null;
	}
			
	public CoreLabel getTokenFromIndexedWord(CoreDocument document, IndexedWord word) {
		return word.backingLabel();
	}
	
	public List<TypedDependency> getTypedDependencyDepFromToken(CoreDocument document, CoreLabel token) {
		List<TypedDependency> retval = new ArrayList<TypedDependency>();
		CoreSentence sentence = getSentenceFromToken(document, token);
		SemanticGraph dependencyParse = sentence.dependencyParse();
		Collection<TypedDependency> deps = dependencyParse.typedDependencies();
		for (TypedDependency dependency : deps) {
			if (getTokenFromIndexedWord(document, dependency.dep()).equals(token)) {
				retval.add(dependency);
			}
		}
		return retval;
	}
	
	public List<TypedDependency> getTypedDependencyGovFromToken(CoreDocument document, CoreLabel token) {
		List<TypedDependency> retval = new ArrayList<TypedDependency>();
		CoreSentence sentence = getSentenceFromToken(document, token);
		SemanticGraph dependencyParse = sentence.dependencyParse();
		Collection<TypedDependency> deps = dependencyParse.typedDependencies();
		for (TypedDependency dependency : deps) {
			if (getTokenFromIndexedWord(document, dependency.gov()).equals(token)) {
				retval.add(dependency);
			}
		}
		return retval;
	}
	
	public CorefChain getCorefChainFromCorefMention(CoreDocument document, CorefMention corefMention) {
		return document.corefChains().get(corefMention.corefClusterID);
	}
	
	
	public boolean isCorefChainForCanonicalEntityName(CoreDocument document, CorefChain corefChain, String canonicalEntityName)  {
		return canonicalEntityName.equals(getCanonicalEntityNameFromCorefChain(document, corefChain));
	}
	
	public boolean isCoreEntityMentionForCanonicalEntityName(CoreDocument document, CoreEntityMention entityMention, String canonicalEntityName)  {
		String canonicalName = "";
		if (entityMention.canonicalEntityMention().isPresent()) {
			//fer fucks sake
			canonicalName = entityMention.canonicalEntityMention().get().toString();
			if (canonicalName.equalsIgnoreCase("he") || canonicalName.equalsIgnoreCase("she")) {
				canonicalName = entityMention.toString();
			}
		}
		else {
			canonicalName = entityMention.text();
		}
		return canonicalEntityName.equals(canonicalName);
	}
	
	public List<CoreLabel> getTokensFromCorefChain(CoreDocument document, CorefChain corefChain) {
		List<CoreLabel> retval = new ArrayList<CoreLabel>();
	    for (CorefMention corefMention : corefChain.getMentionsInTextualOrder() ) {
	    	retval.addAll(getTokensFromCorefMention(document, corefMention));
	    }
	    return retval;
	}
	
	public List<CoreLabel> getTokensFromCorefMention(CoreDocument document, CorefMention corefMention) {
		List<CoreLabel> retval = new ArrayList<CoreLabel>();
    	CoreSentence sentence = getSentenceFromCorefMention(document, corefMention);
    	for (int i=corefMention.startIndex-1;i<corefMention.endIndex-1&&i<sentence.tokens().size();i++) {
    		retval.add(sentence.tokens().get(i));
    	}
    	return retval;
	}
	
	public int getSentenceIdFromSentence(CoreDocument document, CoreSentence sentence) {
		for (int i=0;i<document.sentences().size();i++) {
			if (document.sentences().get(i).equals(sentence)) {
				return i;
			}
		}
		return -1;
	}
	
	
	public CoreSentence getSentenceFromToken(CoreDocument document, CoreLabel token) {
		return document.sentences().get(token.sentIndex());
	}
	
	public CoreSentence getSentenceFromCoreEntityMention(CoreDocument document, CoreEntityMention entityMention) {
		return entityMention.sentence();
	}
	
	public List<CoreLabel> getTokensFromCoreEntityMention(CoreDocument document, CoreEntityMention entityMention) {
		return entityMention.tokens();
	}
	
	public CoreSentence getSentenceFromCorefMention(CoreDocument document,  CorefMention corefMention) {
		return document.sentences().get(corefMention.sentNum-1);
	}
	
	
	//There can be multiple (different) canonical entities in the same coref mention
	//should differentiate on number of matches?
	//corefchain: CHAIN7-["Venkat 's wife Uma" in sentence 1, "She" in sentence 2, "her" in sentence 2, "Uma" in sentence 5]
	//should return Uma, not Venkat
	//corefchain: CHAIN29-["Venkat 's" in sentence 1, "his" in sentence 2, "his" in sentence 2, "Venkat 's" in sentence 5, "his" in sentence 5]
	//should return Venkat
	public String getCanonicalEntityNameFromCorefChain(CoreDocument document, CorefChain corefChain) {
		String canonicalName = "";
		for (CorefMention corefMention : corefChain.getMentionsInTextualOrder() ) {
    		List<CoreEntityMention> entityMentions = getCoreEntityMentionFromCorefMention(document, corefMention);
    		if (entityMentions.size() > 0) {
	    		for (CoreEntityMention entityMention : entityMentions) {
					if (entityMention.canonicalEntityMention().isPresent()) {
						//fer fucks sake
						String personName = entityMention.canonicalEntityMention().get().toString();
						if (personName.equalsIgnoreCase("he") || personName.equalsIgnoreCase("she")) {
							personName = entityMention.toString();
						}
						if (personName.length() > canonicalName.length()) {
							canonicalName = personName;
						}

					}
					else if (entityMention.text().length() > canonicalName.length()) {
						canonicalName = entityMention.text();
					}
	    		}
    		}
    		else {
    			 List<CoreLabel> corefMentionTokens = getTokensFromCorefMention(document, corefMention);
				 StringBuffer buf = new StringBuffer();
    			 for (CoreLabel token : corefMentionTokens) {
    				 buf.append(token.originalText());
    				 buf.append(token.after());
    			 }
    			 if (buf.toString().trim().length() > canonicalName.length()) {
    				 canonicalName = buf.toString();
    			 }
    		}
    	}
		return canonicalName;
	}
	
	public String getGenderNameFromCorefChain(CoreDocument document, CorefChain corefChain) {
		Map<String,BigDecimal> scoreMap = new HashMap<String,BigDecimal>();
		for (CorefMention corefMention : corefChain.getMentionsInTextualOrder() ) {
    		BigDecimal existingScore = scoreMap.get(corefMention.gender.toString());
    		if (existingScore == null) {
    			existingScore = new BigDecimal(0);
    		}
    		scoreMap.put(corefMention.gender.toString(), existingScore.add(new BigDecimal(1)));
		}
    	String maxEntryKey = null;
    	BigDecimal maxEntryCount = new BigDecimal(0);
    	for (String key : scoreMap.keySet()) {
    		if (scoreMap.get(key).compareTo(maxEntryCount) > 0) {
    			maxEntryKey = key;
    			maxEntryCount = scoreMap.get(key);
    		}
    	}
    	return maxEntryKey;
	}
	
	public String getCoreNlpGenderNameFromCoreEntityMention(CoreDocument document, CoreEntityMention entityMention) {
		Map<String,BigDecimal> scoreMap = new HashMap<String,BigDecimal>();
		for (CoreLabel token : entityMention.tokens()) {
			Map<String,BigDecimal> annotatorScore = token.get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.CoreNlpGenderAnnotation.class);
			if (annotatorScore != null) {
				for (String annotatorKey : annotatorScore.keySet()) {
					BigDecimal existingScore = scoreMap.get(annotatorKey);
					if (existingScore == null) {
						existingScore = new BigDecimal(0);
					}
					scoreMap.put(annotatorKey, existingScore.add(new BigDecimal(1)));
				}
			}
		}
    	String maxEntryKey = null;
    	BigDecimal maxEntryCount = new BigDecimal(0);
    	for (String key : scoreMap.keySet()) {
    		if (scoreMap.get(key).compareTo(maxEntryCount) > 0) {
    			maxEntryKey = key;
    			maxEntryCount = scoreMap.get(key);
    		}
    	}
    	return maxEntryKey;
	}
	
	public String getOOPGenderNameFromCoreEntityMention(CoreDocument document, CoreEntityMention entityMention) {
		Map<String,BigDecimal> scoreMap = new HashMap<String,BigDecimal>();
		for (CoreLabel token : entityMention.tokens()) {
			Map<String,BigDecimal> annotatorScore = token.get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPGenderAnnotation.class);
			if (annotatorScore != null) {
				for (String annotatorKey : annotatorScore.keySet()) {
					BigDecimal existingScore = scoreMap.get(annotatorKey);
					if (existingScore == null) {
						existingScore = new BigDecimal(0);
					}
					scoreMap.put(annotatorKey, existingScore.add(new BigDecimal(1)));
				}
			}
		}
    	String maxEntryKey = null;
    	BigDecimal maxEntryCount = new BigDecimal(0);
    	for (String key : scoreMap.keySet()) {
    		if (scoreMap.get(key).compareTo(maxEntryCount) > 0) {
    			maxEntryKey = key;
    			maxEntryCount = scoreMap.get(key);
    		}
    	}
    	return maxEntryKey;
	}
}

