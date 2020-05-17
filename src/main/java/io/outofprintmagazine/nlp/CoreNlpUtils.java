package io.outofprintmagazine.nlp;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.TypedDependency;
import io.outofprintmagazine.nlp.pipeline.annotators.OOPAnnotator;

public class CoreNlpUtils {
	
	private static CoreNlpUtils single_instance = null; 

    public static CoreNlpUtils getInstance() throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException 
    { 
        if (single_instance == null) 
            single_instance = new CoreNlpUtils(); 
  
        return single_instance; 
    } 

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(CoreNlpUtils.class);
	
	private StanfordCoreNLP pipeline;
	private ArrayList<OOPAnnotator> customAnnotators = new ArrayList<OOPAnnotator>();
	
	public static Properties getDefaultProps() {
		// set up pipeline properties
		Properties props = new Properties();

	    props.put("customAnnotatorClass.core_paragraphs",
	    		"io.outofprintmagazine.nlp.pipeline.annotators.CoreNlpParagraphAnnotator");		
	    props.put("customAnnotatorClass.core_gender",
	    		"io.outofprintmagazine.nlp.pipeline.annotators.CoreNlpGenderAnnotator");
	    props.put("customAnnotatorClass.oop_gender",
	            "io.outofprintmagazine.nlp.pipeline.annotators.GenderAnnotator");
	    props.put("customAnnotatorClass.oop_pronouns",
	            "io.outofprintmagazine.nlp.pipeline.annotators.PronounAnnotator");
	    props.put("customAnnotatorClass.oop_charCount",
	            "io.outofprintmagazine.nlp.pipeline.annotators.count.CharCountAnnotator");
	    props.put("customAnnotatorClass.oop_paragraphCount",
	            "io.outofprintmagazine.nlp.pipeline.annotators.count.ParagraphCountAnnotator");
	    props.put("customAnnotatorClass.oop_sentenceCount",
	            "io.outofprintmagazine.nlp.pipeline.annotators.count.SentenceCountAnnotator");
	    props.put("customAnnotatorClass.oop_syllableCount",
	            "io.outofprintmagazine.nlp.pipeline.annotators.count.SyllableCountAnnotator");	    
	    props.put("customAnnotatorClass.oop_tokenCount",
	            "io.outofprintmagazine.nlp.pipeline.annotators.count.TokenCountAnnotator");
	    props.put("customAnnotatorClass.oop_wordCount",
	            "io.outofprintmagazine.nlp.pipeline.annotators.count.WordCountAnnotator");	    

	    props.put("customAnnotatorClass.oop_fleschKincaid",
	            "io.outofprintmagazine.nlp.pipeline.annotators.FleschKincaidAnnotator");
	    props.put("customAnnotatorClass.core_sentiment",
	            "io.outofprintmagazine.nlp.pipeline.annotators.CoreNlpSentimentAnnotator");
	    props.put("customAnnotatorClass.vader_sentiment",
	            "io.outofprintmagazine.nlp.pipeline.annotators.VaderSentimentAnnotator");
//	    props.put("customAnnotatorClass.vader_rollingSentiment",
//	            "io.outofprintmagazine.nlp.pipeline.annotators.VaderRollingSentimentAnnotator");	    
	    props.put("customAnnotatorClass.oop_tense",
	            "io.outofprintmagazine.nlp.pipeline.annotators.VerbTenseAnnotator");	    
	    props.put("customAnnotatorClass.oop_punctuation",
	            "io.outofprintmagazine.nlp.pipeline.annotators.PunctuationMarkAnnotator");
	    props.put("customAnnotatorClass.oop_adjectives",
	            "io.outofprintmagazine.nlp.pipeline.annotators.AdjectivesAnnotator");
	    props.put("customAnnotatorClass.oop_pointlessadjectives",
	            "io.outofprintmagazine.nlp.pipeline.annotators.PointlessAdjectivesAnnotator");
	    props.put("customAnnotatorClass.oop_adjectiveCategories",
	            "io.outofprintmagazine.nlp.pipeline.annotators.AdjectiveCategoriesAnnotator");
	    props.put("customAnnotatorClass.oop_adverbs",
	            "io.outofprintmagazine.nlp.pipeline.annotators.AdverbsAnnotator");
	    props.put("customAnnotatorClass.oop_pointlessadverbs",
	            "io.outofprintmagazine.nlp.pipeline.annotators.PointlessAdverbsAnnotator");	    
	    props.put("customAnnotatorClass.oop_adverbCategories",
	            "io.outofprintmagazine.nlp.pipeline.annotators.AdverbCategoriesAnnotator");	    
	    props.put("customAnnotatorClass.oop_possessives",
	            "io.outofprintmagazine.nlp.pipeline.annotators.PossessivesAnnotator");
	    props.put("customAnnotatorClass.oop_prepositions",
	            "io.outofprintmagazine.nlp.pipeline.annotators.PrepositionsAnnotator");
	    props.put("customAnnotatorClass.oop_prepositionCategories",
	            "io.outofprintmagazine.nlp.pipeline.annotators.PrepositionCategoriesAnnotator");
	    props.put("customAnnotatorClass.oop_verbs",
	            "io.outofprintmagazine.nlp.pipeline.annotators.VerbsAnnotator");
	    props.put("customAnnotatorClass.oop_actionlessverbs",
	            "io.outofprintmagazine.nlp.pipeline.annotators.ActionlessVerbsAnnotator");
	    props.put("customAnnotatorClass.oop_nouns",
	            "io.outofprintmagazine.nlp.pipeline.annotators.NounsAnnotator");
//	    props.put("customAnnotatorClass.oop_untaggedTopics",
//	            "io.outofprintmagazine.nlp.pipeline.annotators.UntaggedTopicsAnnotator");
	    props.put("customAnnotatorClass.oop_topics",
	            "io.outofprintmagazine.nlp.pipeline.annotators.TopicsAnnotator");
	    props.put("customAnnotatorClass.oop_svo",
	            "io.outofprintmagazine.nlp.pipeline.annotators.SVOAnnotator");
	    props.put("customAnnotatorClass.oop_wikipediaGloss",
	            "io.outofprintmagazine.nlp.pipeline.annotators.WikipediaGlossAnnotator");	    
	    props.put("customAnnotatorClass.oop_wikipediaPageviews",
	            "io.outofprintmagazine.nlp.pipeline.annotators.WikipediaPageviewTopicsAnnotator");
//	    props.put("customAnnotatorClass.oop_wikipediaRelated",
//	            "io.outofprintmagazine.nlp.pipeline.annotators.WikipediaRelatedNounsAnnotator");
	    props.put("customAnnotatorClass.oop_wikipediaCategories",
	            "io.outofprintmagazine.nlp.pipeline.annotators.WikipediaCategoriesAnnotator");
	    props.put("customAnnotatorClass.oop_nonAffirmative",
	            "io.outofprintmagazine.nlp.pipeline.annotators.NonAffirmativeAnnotator");	    
//	    props.put("customAnnotatorClass.oop_simile",
//	            "io.outofprintmagazine.nlp.pipeline.annotators.SimileAnnotator");
	    props.put("customAnnotatorClass.oop_like",
	    		"io.outofprintmagazine.nlp.pipeline.annotators.simile.LikeAnnotator");
	    props.put("customAnnotatorClass.oop_as",
	    		"io.outofprintmagazine.nlp.pipeline.annotators.simile.AsAnnotator");	    
	    props.put("customAnnotatorClass.oop_colors",
	            "io.outofprintmagazine.nlp.pipeline.annotators.ColorsAnnotator");
	    props.put("customAnnotatorClass.oop_flavors",
	            "io.outofprintmagazine.nlp.pipeline.annotators.FlavorsAnnotator");
	    props.put("customAnnotatorClass.oop_verblessSentences",
	            "io.outofprintmagazine.nlp.pipeline.annotators.VerblessSentencesAnnotator");
	    props.put("customAnnotatorClass.oop_wordlessWords",
	            "io.outofprintmagazine.nlp.pipeline.annotators.WordlessWordsAnnotator");
	    props.put("customAnnotatorClass.oop_wordnetGloss",
	            "io.outofprintmagazine.nlp.pipeline.annotators.WordnetGlossAnnotator");
	    props.put("customAnnotatorClass.oop_perfecttense",
	            "io.outofprintmagazine.nlp.pipeline.annotators.PerfecttenseAnnotator");
	    props.put("customAnnotatorClass.oop_uncommonWords",
	            "io.outofprintmagazine.nlp.pipeline.annotators.UncommonWordsAnnotator");
	    props.put("customAnnotatorClass.oop_commonWords",
	            "io.outofprintmagazine.nlp.pipeline.annotators.CommonWordsAnnotator");
	    props.put("customAnnotatorClass.oop_functionWords",
	            "io.outofprintmagazine.nlp.pipeline.annotators.FunctionWordsAnnotator");
	    props.put("customAnnotatorClass.oop_americanize",
	            "io.outofprintmagazine.nlp.pipeline.annotators.AmericanizeAnnotator");
	    props.put("customAnnotatorClass.oop_anglicise",
	            "io.outofprintmagazine.nlp.pipeline.annotators.AngliciseAnnotator");		    
	    props.put("customAnnotatorClass.oop_verbHypernyms",
	            "io.outofprintmagazine.nlp.pipeline.annotators.VerbHypernymsAnnotator");
	    props.put("customAnnotatorClass.oop_verbnetGroups",
	            "io.outofprintmagazine.nlp.pipeline.annotators.VerbnetGroupsAnnotator");	    
	    props.put("customAnnotatorClass.oop_verbGroups",
	            "io.outofprintmagazine.nlp.pipeline.annotators.VerbGroupsAnnotator");
	    props.put("customAnnotatorClass.oop_nounGroups",
	            "io.outofprintmagazine.nlp.pipeline.annotators.NounGroupsAnnotator");	    
	    props.put("customAnnotatorClass.oop_nounHypernyms",
	            "io.outofprintmagazine.nlp.pipeline.annotators.NounHypernymsAnnotator");
	    props.put("customAnnotatorClass.oop_temporalNGrams",
	            "io.outofprintmagazine.nlp.pipeline.annotators.TemporalNGramsAnnotator");
//	    props.put("customAnnotatorClass.oop_wha",
//	            "io.outofprintmagazine.nlp.pipeline.annotators.WhaAnnotator");
	    props.put("customAnnotatorClass.oop_who",
	            "io.outofprintmagazine.nlp.pipeline.annotators.interrogative.WhatAnnotator");	    
	    props.put("customAnnotatorClass.oop_what",
	            "io.outofprintmagazine.nlp.pipeline.annotators.interrogative.WhoAnnotator");
	    props.put("customAnnotatorClass.oop_when",
	            "io.outofprintmagazine.nlp.pipeline.annotators.interrogative.WhenAnnotator");	    
	    props.put("customAnnotatorClass.oop_where",
	            "io.outofprintmagazine.nlp.pipeline.annotators.interrogative.WhereAnnotator");
	    props.put("customAnnotatorClass.oop_why",
	            "io.outofprintmagazine.nlp.pipeline.annotators.interrogative.WhyAnnotator");
	    props.put("customAnnotatorClass.oop_how",
	            "io.outofprintmagazine.nlp.pipeline.annotators.interrogative.HowAnnotator");
	    props.put("customAnnotatorClass.oop_locations",
	            "io.outofprintmagazine.nlp.pipeline.annotators.LocationsAnnotator");
	    props.put("customAnnotatorClass.oop_people",
	            "io.outofprintmagazine.nlp.pipeline.annotators.PeopleAnnotator");
	    props.put("customAnnotatorClass.oop_actors",
	            "io.outofprintmagazine.nlp.pipeline.annotators.ActorsAnnotator");
	    props.put("customAnnotatorClass.oop_settings",
	            "io.outofprintmagazine.nlp.pipeline.annotators.SettingsAnnotator");	    
	    props.put("customAnnotatorClass.oop_biber",
	            "io.outofprintmagazine.nlp.pipeline.annotators.BiberAnnotator");
	    props.put("customAnnotatorClass.oop_biberDimensions",
	            "io.outofprintmagazine.nlp.pipeline.annotators.BiberDimensionsAnnotator");
	    props.put("customAnnotatorClass.oop_myersBriggs",
	            "io.outofprintmagazine.nlp.pipeline.annotators.MyersBriggsAnnotator");
	    props.put("customAnnotatorClass.oop_words",
	            "io.outofprintmagazine.nlp.pipeline.annotators.WordsAnnotator");
	    props.put("customAnnotatorClass.oop_dates",
	            "io.outofprintmagazine.nlp.pipeline.annotators.DatesAnnotator");
//	    props.put("customAnnotatorClass.oop_questions",
//	            "io.outofprintmagazine.nlp.pipeline.annotators.QuestionsAnnotator");
	    props.put("customAnnotatorClass.oop_if",
	            "io.outofprintmagazine.nlp.pipeline.annotators.conditional.IfAnnotator");
	    props.put("customAnnotatorClass.oop_because",
	            "io.outofprintmagazine.nlp.pipeline.annotators.conditional.BecauseAnnotator");	    
	    props.put("customAnnotatorClass.oop_quotes",
	            "io.outofprintmagazine.nlp.pipeline.annotators.QuotesAnnotator");
	
	    
	    // configure pipeline
	//    edu.stanford.nlp.paragraphs.ParagraphAnnotator x = new edu.stanford.nlp.paragraphs.ParagraphAnnotator(props, verbose)
		//props.setProperty("annotators","tokenize,ssplit,pos,lemma,ner,parse,depparse,sentiment,oop_paragraphs,oop_gender,coref");
	    //props.setProperty("annotators", "tokenize,ssplit,pos,ner,lemma,gender,oop_paragraphs,oop_gender,parse,depparse,sentiment,coref,quote");
	    props.setProperty("annotators", 
	    		"tokenize,"
	    		+ "ssplit,"
	    		+ "pos,"
	    		+ "lemma,"
	    		+ "ner,"
	    	//	+ "regexner,"
	    		+ "parse,"
	    		+ "sentiment,"
	    		+ "depparse,"
	    		+ "coref,"
	    		+ "quote,"
	    		+ "oop_biber,"
	    		+ "core_paragraphs,"
	    		+ "core_gender,"
	    		+ "oop_gender,"
	    		+ "oop_pronouns,"
	    		+ "oop_charCount,"
	    		+ "oop_paragraphCount,"
	    		+ "oop_sentenceCount,"
	    		+ "oop_syllableCount,"
	    		+ "oop_tokenCount,"
	    		+ "oop_wordCount,"	    		
	    		+ "core_sentiment,"
	    		+ "vader_sentiment,"
	    		+ "oop_tense,"
	    		+ "oop_punctuation,"
	    		+ "oop_adjectives,"
	    		+ "oop_pointlessadjectives,"
	    		+ "oop_adjectiveCategories,"
	    		+ "oop_adverbs,"
	    		+ "oop_pointlessadverbs,"
	    		+ "oop_adverbCategories,"
	    		+ "oop_possessives,"
	    		+ "oop_prepositionCategories,"
	    		+ "oop_prepositions,"
	    		+ "oop_verbs,"
	    		+ "oop_actionlessverbs,"
	    		+ "oop_nouns,"
	    		+ "oop_topics,"
//	    		+ "oop_untaggedTopics,"
	    		+ "oop_svo,"
	    		+ "oop_nonAffirmative,"	    		
//	    		+ "oop_simile,"
	    		+ "oop_like,"
	    		+ "oop_as,"	    		
	    		+ "oop_colors,"
	    		+ "oop_flavors,"
	    		+ "oop_verblessSentences,"
	    		+ "oop_wordlessWords,"
	    		+ "oop_wordnetGloss,"
	    		+ "oop_perfecttense,"
	    		+ "oop_uncommonWords,"
	    		+ "oop_commonWords,"
	    		+ "oop_functionWords,"
	    		+ "oop_anglicise,"
	    		+ "oop_americanize,"
	    		+ "oop_verbGroups,"
	    		+ "oop_verbnetGroups,"
	    		+ "oop_nounGroups,"	    		
	    		+ "oop_temporalNGrams,"
//	    		+ "oop_wha,"
	    		+ "oop_who,"
	    		+ "oop_what,"
	    		+ "oop_when,"
	    		+ "oop_where,"
	    		+ "oop_why,"		    		
	    		+ "oop_how,"
	    		+ "oop_locations,"
	    		+ "oop_people,"
	    		+ "oop_myersBriggs,"	    		
	    		+ "oop_biberDimensions,"	    		
	    		+ "oop_dates,"
//	    		+ "oop_questions,"
	    		+ "oop_if,"
	    		+ "oop_because,"	 
	    		+ "oop_quotes,"
	    		+ "oop_words,"
//	    		+ "vader_rollingSentiment,"
	    		+ "oop_fleschKincaid,"
	    		+ "oop_verbHypernyms,"
	    	    + "oop_nounHypernyms,"
	    		+ "oop_wikipediaGloss,"	    		
	    		+ "oop_wikipediaPageviews,"
//	    		+ "oop_wikipediaRelated,"
	    		+ "oop_wikipediaCategories,"	    		
	    		+ "oop_actors,"
	    		+ "oop_settings"
	    );
	    //props.setProperty("annotators","tokenize,ssplit,pos,lemma,ner,parse,sentiment,oop_paragraphs,oop_gender");
	    //API call w/annotators tokenize,ssplit,pos,lemma,ner,parse,depparse,coref,kbp,sentiment


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
		
		return props;
	}
	
	public StanfordCoreNLP getPipeline() {
		if (pipeline == null) {
			Properties p = CoreNlpUtils.getDefaultProps();
			//logger.info(StanfordCoreNLP.ensurePrerequisiteAnnotators(p.getProperty("annotators").split(","), p));
			pipeline = new StanfordCoreNLP(p);
		}
		return pipeline;
	}
	

	public void initAnnotators() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		Iterator<Object> keyIter = CoreNlpUtils.getDefaultProps().keySet().iterator();
		while (keyIter.hasNext()) {
			String key = (String) keyIter.next();
			if (key.startsWith("customAnnotatorClass")) {
				String annotatorClassName = CoreNlpUtils.getDefaultProps().getProperty(key);
				Object annotator = Class.forName(annotatorClassName).newInstance();
				if (annotator instanceof OOPAnnotator) {
					OOPAnnotator oopAnnotator = (OOPAnnotator) annotator;
					oopAnnotator.init(null);
					customAnnotators.add(oopAnnotator);
				}

			}
		}		
	}

	private CoreNlpUtils() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		this(CoreNlpUtils.getDefaultProps());
	}
	
	private CoreNlpUtils(Properties props) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		super();
	}
		
	public void serialize(CoreDocument document, ObjectNode json) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (customAnnotators.size() == 0) {
			initAnnotators();
		}
		for (OOPAnnotator annotator : customAnnotators) {
			annotator.serialize(document, json);
		}	
	}
	
	public void serializeAggregates(CoreDocument document, ObjectNode json) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (customAnnotators.size() == 0) {
			initAnnotators();
		}
		for (OOPAnnotator annotator : customAnnotators) {
			annotator.serializeAggregateDocument(document, json);
		}	
	}
	
	public void describeAnnotations(ObjectNode json) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		ArrayNode annotatorList = json.putArray("annotations");
		if (customAnnotators.size() == 0) {
			initAnnotators();
		}
		for (OOPAnnotator annotator : customAnnotators) {
			annotatorList.addObject().put(annotator.getAnnotationClass().getSimpleName(), annotator.getDescription());
		}	
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
	
	public static void main(String[] args) throws Exception {
		// set up pipeline properties
		Properties props = new Properties();
		// set the list of annotators to run
		props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,coref");
		props.setProperty("ner.maxlen", "70");
		props.setProperty("ner.applyFineGrained", "false");
		props.setProperty("ner.useSUTime", "true");
		props.setProperty("ner.applyNumericClassifiers", "true");
		props.setProperty("ner.combinationMode", "NORMAL");
		props.setProperty("ner.additional.regexner.mapping", "io/outofprintmagazine/nlp/models/NER/regexner.txt");
		props.setProperty("coref.algorithm", "statistical");
		// build pipeline
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		// create a document object
		//CoreDocument document = new CoreDocument(
		//		IOUtils.slurpFile("c:/users/rsada/eclipse-workspace/NLP/src/main/resources/Story.txt"));
		// annnotate the document
		CoreDocument document = new CoreDocument(
//				"Venkat's wife Uma was another simpleton. She was from his village and was related to Venkat in a manner that made her the natural choice to be his wife. When the two were still kids there was a tacit agreement between their two families that eventually the two would end up as husband and wife. In villages like this, such agreements were sacrosanct. Uma was five years Venkat's junior and now nine months pregnant with his child. John Smith walked his dog. John felt it was his duty to accomodate the poor mutt, who just wanted to chase rabbits. Jojo was a man."
"They stopped in front of the couple, so close the latter could smell the sweat that rose off their bodies."
				);

		pipeline.annotate(document);
		//CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			try {
//				for (int i=0;i<sentence.tokens().size();i++) {
					SemanticGraph dependencyParse = sentence.dependencyParse();
					Collection<TypedDependency> vdeps = dependencyParse.typedDependencies();
					for (TypedDependency dependency : vdeps) {
						GrammaticalRelation rn = dependency.reln();
						//logger.debug("reln: " + rn.getShortName());
						if (rn.getShortName().equals("root")) {
							Map<String,BigDecimal> vscoreMap = new HashMap<String,BigDecimal>();
							logger.debug("verb: " + dependency.dep().backingLabel());
							//dependency.dep().backingLabel().set(getAnnotationClass(), vscoreMap);
						}
						if (rn.getShortName().equals("acl:relcl")) {
							Map<String,BigDecimal> vscoreMap = new HashMap<String,BigDecimal>();
							logger.debug("verb: " + dependency.dep().backingLabel());
							//dependency.dep().backingLabel().set(getAnnotationClass(), vscoreMap);
						}
						if (rn.getShortName().equals("parataxis")) {
							if (Arrays.asList("VB","VBD","VBG","VBN","VBP","VBZ").contains(dependency.dep().tag())) {
								Map<String,BigDecimal> vscoreMap = new HashMap<String,BigDecimal>();
								logger.debug("verb: " + dependency.dep().backingLabel());
								//dependency.dep().backingLabel().set(getAnnotationClass(), vscoreMap);
							}
						}
						
	//				}
	//				CoreLabel token = sentence.tokens().get(i);
//					List<TypedDependency> deps = CoreNlpUtils.getInstance().getTypedDependencyDepFromToken(document, token);
//					for (TypedDependency dependency : deps) {
//						GrammaticalRelation rn = dependency.reln();
						if (rn.getShortName().equals("nsubj") || rn.getShortName().equals("nsubjpass") || rn.getShortName().equals("csubj") || rn.getShortName().equals("csubjpass")) {
							if (Arrays.asList("VB","VBD","VBG","VBN","VBP","VBZ").contains(dependency.gov().tag())) {
								//Subject of transitive verb. John hit the ball to the fence -> John
								Map<String,BigDecimal> sscoreMap = new HashMap<String,BigDecimal>();
								logger.debug("subject: " + dependency.dep().backingLabel());
								//token.set(getAnnotationClass(), sscoreMap);
							}
						}
						else if (rn.getShortName().equals("dobj")) {
							if (Arrays.asList("VB","VBD","VBG","VBN","VBP","VBZ").contains(dependency.gov().tag())) {
								//Object of transitive verb. John hit the ball to the fence -> ball
								Map<String,BigDecimal> oscoreMap = new HashMap<String,BigDecimal>();
								logger.debug("object: " + dependency.dep().backingLabel());
								//token.set(getAnnotationClass(), oscoreMap);
							}
						}
						else if (rn.getShortName().equals("iobj")) {
							if (Arrays.asList("VB","VBD","VBG","VBN","VBP","VBZ").contains(dependency.gov().tag())) {
								//Object of transitive verb. John hit the ball to the fence -> fence
								Map<String,BigDecimal> oscoreMap = new HashMap<String,BigDecimal>();
								logger.debug("indirectObject: " + dependency.dep().backingLabel());
								//token.set(getAnnotationClass(), oscoreMap);
							}
						}
					}
			//	}
			}
			catch (Exception e) {
				logger.error(e);
			}
		}

	}
}

