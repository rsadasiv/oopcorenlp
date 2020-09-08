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
package io.outofprintmagazine.nlp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.JSONOutputter;
import io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPThumbnailAnnotation;
import io.outofprintmagazine.nlp.pipeline.annotators.IOOPAnnotator;
import io.outofprintmagazine.nlp.pipeline.serializers.CoreNlpSerializer;
import io.outofprintmagazine.nlp.utils.CoreNlpUtils;
import io.outofprintmagazine.util.IParameterStore;


/**
 * <p>The main library execution entry point for oopcorenlp.</p>
 * <p>analyze runs the coreNLP annotators, runs the custom annotators, serializes the annotation tree, and returns four JsonDocuments:</p> 
 * <ul>
 * 	<li>STANFORD</li>
 * 	<li>OOP</li>
 * 	<li>AGGREGATES</li>
 *  <li>PIPELINE</li>
 * </ul>
 * @see CoreNlpUtils
 * @see IOOPAnnotator
 * @see CoreNlpSerializer
 * @see io.outofprintmagazine.nlp.pipeline.serializers.ISerializer
 * @author Ram Sadasiv
 */
public class Analyzer {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(Analyzer.class);
	
	private Logger getLogger() {
		return logger;
	}
	
	private ArrayList<IOOPAnnotator> customAnnotators = new ArrayList<IOOPAnnotator>();
	private IParameterStore parameterStore;
	
	public Analyzer(IParameterStore parameterStore, List<String> annotatorClassNames) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		super();
		this.parameterStore = parameterStore;
		//order is important
		for (String customAnnotatorClassName : customAnnotatorClassNames) {
			for (String annotatorClassName : annotatorClassNames) {
				if (annotatorClassName.equals(customAnnotatorClassName)) {
					logger.debug("Adding CustomAnnotator: " + annotatorClassName);
					Object annotator = Class.forName(annotatorClassName).newInstance();
					if (annotator instanceof IOOPAnnotator) {
						IOOPAnnotator oopAnnotator = (IOOPAnnotator) annotator;
						oopAnnotator.init(parameterStore);
						customAnnotators.add(oopAnnotator);
					}
				}
				continue;
			}
		}
	}
	
	public ArrayList<IOOPAnnotator> getCustomAnnotators() {
		return customAnnotators;
	}
	
	/**
	 * 
	 */
	public static List<String> customAnnotatorClassNames = Arrays.asList(
			"io.outofprintmagazine.nlp.pipeline.annotators.BiberAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.CoreNlpParagraphAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.CoreNlpGenderAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.GenderAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.PronounAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.count.CharCountAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.count.ParagraphCountAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.count.SentenceCountAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.count.SyllableCountAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.count.TokenCountAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.count.WordCountAnnotator"	    		
    		, "io.outofprintmagazine.nlp.pipeline.annotators.CoreNlpSentimentAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.VaderSentimentAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.VerbTenseAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.PunctuationMarkAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.AdjectivesAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.PointlessAdjectivesAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.AdjectiveCategoriesAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.AdverbsAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.PointlessAdverbsAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.AdverbCategoriesAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.PossessivesAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.PrepositionCategoriesAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.PrepositionsAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.VerbsAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.ActionlessVerbsAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.NounsAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.TopicsAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.SVOAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.NonAffirmativeAnnotator"	    		
    		, "io.outofprintmagazine.nlp.pipeline.annotators.simile.LikeAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.simile.AsAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.ColorsAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.FlavorsAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.VerblessSentencesAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.WordlessWordsAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.WordnetGlossAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.PerfecttenseAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.UncommonWordsAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.CommonWordsAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.FunctionWordsAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.AngliciseAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.AmericanizeAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.VerbGroupsAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.VerbnetGroupsAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.NounGroupsAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.TemporalNGramsAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.interrogative.WhoAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.interrogative.WhatAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.interrogative.WhenAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.interrogative.WhereAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.interrogative.WhyAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.interrogative.HowAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.LocationsAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.PeopleAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.MyersBriggsAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.DatesAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.conditional.IfAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.conditional.BecauseAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.QuotesAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.WordsAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.FleschKincaidAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.VerbHypernymsAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.NounHypernymsAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.WikipediaGlossAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.WikipediaPageviewTopicsAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.WikipediaCategoriesAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.BiberDimensionsAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.ActorsAnnotator"
    		, "io.outofprintmagazine.nlp.pipeline.annotators.SettingsAnnotator"
	);
		
	private CoreDocument prepareDocument(Properties metadata, String text) {
		CoreDocument document = new CoreDocument(text);
		document.annotation().set(
				CoreAnnotations.DocIDAnnotation.class, 
				metadata.getProperty(
						CoreAnnotations.DocIDAnnotation.class.getSimpleName(), 
						"LittleNaomi"
						)
				);
		document.annotation().set(
				CoreAnnotations.DocTitleAnnotation.class, 
				metadata.getProperty(
						CoreAnnotations.DocTitleAnnotation.class.getSimpleName(), 
						"story title"
						)
				);
		document.annotation().set(
				CoreAnnotations.DocTypeAnnotation.class, 
				metadata.getProperty(
						CoreAnnotations.DocTypeAnnotation.class.getSimpleName(), 
						"Submissions"
						)
				);
		document.annotation().set(
				CoreAnnotations.DocSourceTypeAnnotation.class, 
				metadata.getProperty(
						CoreAnnotations.DocSourceTypeAnnotation.class.getSimpleName(), 
						"outofprintmagazine@gmail.com"
						)
				);		
		document.annotation().set(
				CoreAnnotations.AuthorAnnotation.class, 
				metadata.getProperty(
						CoreAnnotations.AuthorAnnotation.class.getSimpleName(), 
						"Author"
						)
				);
		document.annotation().set(
				CoreAnnotations.DocDateAnnotation.class, 
				metadata.getProperty(
						CoreAnnotations.DocDateAnnotation.class.getSimpleName(), 
						"9 January 2014 21:05"
						)
				);
		document.annotation().set(
				OOPThumbnailAnnotation.class, 
				metadata.getProperty(
						OOPThumbnailAnnotation.class.getSimpleName(), 
						"blank.png"
						)
				);
		return document;
	}
		
	private void runCoreNLP(CoreDocument document) throws IOException  {
		CoreNlpUtils.getInstance(parameterStore).getPipeline().annotate(document);
	}
	
	private void annotate(CoreDocument document) {
		for (IOOPAnnotator annotator : customAnnotators) {
			long startTime = System.currentTimeMillis();
			annotator.annotate(document.annotation());
			annotator.score(document);
			getLogger().info(String.format("%s %d ms", annotator.getClass().getSimpleName(), System.currentTimeMillis()-startTime));
		}
	}
	
	private void serialize(CoreDocument document, ObjectNode json)  {
		for (IOOPAnnotator annotator : customAnnotators) {
			annotator.serialize(document, json);
		}	
	}
	
	private void serializeAggregates(CoreDocument document, ObjectNode json) {
		for (IOOPAnnotator annotator : customAnnotators) {
			annotator.serializeAggregateDocument(document, json);
		}	
	}
		
	private void serializePipeline(ObjectNode json, long startTime, Properties properties) throws IOException {
		ArrayNode annotatorList = json.putArray("annotations");
		for (IOOPAnnotator annotator : customAnnotators) {
			annotatorList.addObject().put(annotator.getAnnotationClass().getSimpleName(), annotator.getDescription());
		}	

		ArrayNode coreNlpProperties = json.putArray("coreNlpProperties");
		Properties defaultProps = CoreNlpUtils.getInstance(parameterStore).getPipelineProps();
		for (String propertyName : defaultProps.stringPropertyNames()) {
			ObjectNode property = coreNlpProperties.addObject();
			property.put("name", propertyName);
			property.put("value", defaultProps.getProperty(propertyName));
		}
		
		ArrayNode customProperties = json.putArray("customProperties");
		for (String propertyName : properties.stringPropertyNames()) {
			ObjectNode property = customProperties.addObject();
			property.put("name", propertyName);
			property.put("value", properties.getProperty(propertyName));
		}

		ArrayNode analysisList = json.putArray("analysis");
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
		analysisList.addObject().put("startTime", fmt.format(new java.util.Date(startTime)));
		long endTime = System.currentTimeMillis();
		analysisList.addObject().put("endTime", fmt.format(new java.util.Date(endTime)));
		analysisList.addObject().put("elapsedMS", Long.toString(endTime-startTime));
		String ipAddr = "localhost.localdomain/127.0.0.1";
		try {
			ipAddr = InetAddress.getLocalHost().toString();
		}
		catch (UnknownHostException e) {
		}
		analysisList.addObject().put("host", ipAddr);
	}
	
	public Map<String, ObjectNode> analyze(Properties metadata, String text) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {

		ObjectMapper mapper = new ObjectMapper();
		long startTime = System.currentTimeMillis();
		CoreDocument document = prepareDocument(metadata, text);
		Map<String,ObjectNode> retval = new HashMap<String, ObjectNode>();
		
		try {
			logger.debug("analyzing " + metadata.getProperty(CoreAnnotations.DocIDAnnotation.class.getSimpleName()));
			
			runCoreNLP(document);
			retval.put("STANFORD", (ObjectNode) mapper.readTree(JSONOutputter.jsonPrint(document.annotation())));
			
			annotate(document);

			CoreNlpSerializer documentSerializer = new CoreNlpSerializer();
			
			ObjectNode json = mapper.createObjectNode();
			documentSerializer.serialize(document, json);
			serialize(document, json);
			retval.put("OOP", json);
			
			ObjectNode aggregates = mapper.createObjectNode();
			documentSerializer.serializeAggregate(document, aggregates);
			serializeAggregates(document, aggregates);
			retval.put("AGGREGATES", aggregates);

			ObjectNode pipeline = mapper.createObjectNode();
			serializePipeline(pipeline, startTime, metadata);
			retval.put("PIPELINE", pipeline);
			
		}
		catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
		return retval;
	}
	


}
