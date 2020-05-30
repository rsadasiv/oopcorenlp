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
package io.outofprintmagazine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import io.burt.jmespath.Expression;
import io.burt.jmespath.JmesPath;
import io.burt.jmespath.jackson.JacksonRuntime;
import io.outofprintmagazine.nlp.CoreNlpUtils;
import io.outofprintmagazine.nlp.pipeline.annotators.OOPAnnotator;

public class Analyzer_Test {

	private Analyzer ta = null;
	private String text = null;
	
	protected String getDefaultPath() {
		return "C:\\Users\\rsada\\eclipse-workspace\\oopcorenlp_web\\WebContent\\Corpora\\IntegrationTest";
	}
	
	public String getText() throws IOException {
		if (text == null) {
			text = IOUtils.toString(new FileInputStream("C:\\Users\\rsada\\eclipse-workspace\\oopcorenlp\\src\\test\\resources\\io\\outofprintmagazine\\wapo.txt"), "UTF-8");
		}
		return text;
	}
	
	public Analyzer getAnalyzer() {
		if (ta == null) {
			//ta = new Analyzer();
		}
		return ta;
	}
	
	public Analyzer_Test() throws IOException {
		super();
		

	}
	
	public List<String> generateMapTokenMatchTargets(JsonNode doc, String annotation) {
		List<String> matchTargets = new ArrayList<String>();
		for (JsonNode sentence : doc.get("sentences")) {
			for (JsonNode token : sentence.get("tokens")) {
				if (token.has(annotation)) {
					Iterator<String> fieldsIter = token.get(annotation).fieldNames();
					while (fieldsIter.hasNext()) {
						String score = fieldsIter.next();
						matchTargets.add(
								String.format(
										"sentences[%d].tokens[%d].%s.%s",
										sentence.get("SentenceIndexAnnotation").asInt(),
										token.get("tokenIndex").asInt()-1,
										annotation,
										score
								)
						);			
					}
				}
			}
		}
		return matchTargets;
	}
	
	public List<String> generateMapSentenceMatchTargets(JsonNode doc, String annotation) {
		List<String> matchTargets = new ArrayList<String>();
		for (JsonNode sentence : doc.get("sentences")) {
			if (sentence.has(annotation)) {
				Iterator<String> fieldsIter = sentence.get(annotation).fieldNames();
				while (fieldsIter.hasNext()) {
					String score = fieldsIter.next();
					matchTargets.add(
							String.format(
									"sentences[%d].%s.%s",
									sentence.get("SentenceIndexAnnotation").asInt(),
									annotation,
									score
							)
					);
				}
			}
		}
		return matchTargets;
	}
	
	public List<String> generateMapDocumentMatchTargets(JsonNode doc, String annotation) {
		List<String> matchTargets = new ArrayList<String>();
		if (doc.has(annotation)) {
			Iterator<String> fieldsIter = doc.get(annotation).fieldNames();
			while (fieldsIter.hasNext()) {
				String score = fieldsIter.next();
				matchTargets.add(
						String.format(
								"%s.%s",
								annotation,
								score
						)
				);
			}
		}
		
		return matchTargets;
	}
	
	public List<String> generateListTokenMatchTargets(JsonNode doc, String annotation) {
		List<String> matchTargets = new ArrayList<String>();
		for (JsonNode sentence : doc.get("sentences")) {
			for (JsonNode token : sentence.get("tokens")) {
				if (token.has(annotation)) {
					ArrayNode annotationList = (ArrayNode) token.get(annotation);
					int fieldIdx = 0;
					Iterator<JsonNode> fieldsIter = annotationList.elements();
					while (fieldsIter.hasNext()) {
						JsonNode score = fieldsIter.next();
						matchTargets.add(
								String.format(
										"sentences[%d].tokens[%d].%s[%d].name = %s",
										sentence.get("SentenceIndexAnnotation").asInt(),
										token.get("tokenIndex").asInt()-1,
										annotation,
										fieldIdx++,
										score.get("name").asText()
								)
						);			
					}
				}
			}
		}
		return matchTargets;
	}
	
	public List<String> generateListSentenceMatchTargets(JsonNode doc, String annotation) {
		List<String> matchTargets = new ArrayList<String>();
		for (JsonNode sentence : doc.get("sentences")) {
			if (sentence.has(annotation)) {
				ArrayNode annotationList = (ArrayNode) sentence.get(annotation);
				int fieldIdx = 0;
				Iterator<JsonNode> fieldsIter = annotationList.elements();
				while (fieldsIter.hasNext()) {
					JsonNode score = fieldsIter.next();
					matchTargets.add(
							String.format(
									"sentences[%d].%s[%d].name = %s",
									sentence.get("SentenceIndexAnnotation").asInt(),
									annotation,
									fieldIdx++,
									score.get("name").asText()
							)
					);
				}
			}
		}
		return matchTargets;
	}
	
	public List<String> generateListDocumentMatchTargets(JsonNode doc, String annotation) {
		List<String> matchTargets = new ArrayList<String>();
		if (doc.has(annotation)) {
			ArrayNode annotationList = (ArrayNode)doc.get(annotation);
			Iterator<JsonNode> fieldsIter = annotationList.elements();
			int fieldIdx = 0;
			while (fieldsIter.hasNext()) {
				JsonNode score = fieldsIter.next();
				matchTargets.add(
						String.format(
								"%s[%d].name = %s",
								annotation,
								fieldIdx++,
								score.get("name").asText()
						)
				);
			}
		}
		
		return matchTargets;
	}
	
	public List<String> generateScalarTokenMatchTargets(JsonNode doc, String annotation) {
		List<String> matchTargets = new ArrayList<String>();
		for (JsonNode sentence : doc.get("sentences")) {
			for (JsonNode token : sentence.get("tokens")) {
				if (token.has(annotation)) {
					matchTargets.add(
							String.format(
									"sentences[%d].tokens[%d].%s.%s",
									sentence.get("SentenceIndexAnnotation").asInt(),
									token.get("tokenIndex").asInt()-1,
									annotation,
									token.get(annotation).asDouble()
							)
					);			
				}
			}
		}
		return matchTargets;
	}
	
	public List<String> generateScalarSentenceMatchTargets(JsonNode doc, String annotation) {
		List<String> matchTargets = new ArrayList<String>();
		for (JsonNode sentence : doc.get("sentences")) {
			if (sentence.has(annotation)) {
				matchTargets.add(
						String.format(
								"sentences[%d].%s.%s",
								sentence.get("SentenceIndexAnnotation").asInt(),
								annotation,
								sentence.get(annotation).asDouble()
						)
				);
			}
		}
		return matchTargets;
	}
	
	public List<String> generateScalarDocumentMatchTargets(JsonNode doc, String annotation) {
		List<String> matchTargets = new ArrayList<String>();
		if (doc.has(annotation)) {
			matchTargets.add(
					String.format(
							"%s.%s",
							annotation,
							doc.get(annotation).asDouble()
					)
			);
		}
		
		return matchTargets;
	}
	
	public void testMatchTargets(JsonNode doc, List<String> matchTargets) {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		
		for (String expr : matchTargets) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			JsonNode result = expression.search(doc);
			if (result.isNull()) {
				System.out.println("FAILED: " + expr);
			}
			else {
				System.out.println("PASSED: " + expr);
			}
		}
	}

	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		Analyzer_Test me = new Analyzer_Test();
		JsonNode doc = null;
		ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
		mapper.configure(com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
		
		Properties metadata = new Properties();
		metadata.put(CoreAnnotations.DocIDAnnotation.class.getName(), "Wapo");
		metadata.put(CoreAnnotations.DocTypeAnnotation.class.getName(), "IntegrationTest");
		metadata.put(CoreAnnotations.AuthorAnnotation.class.getName(), "Clarence Williams");
		metadata.put(CoreAnnotations.DocDateAnnotation.class.getName(), "April 25, 2020 at 12:15 p.m. EDT");
		metadata.put(CoreAnnotations.DocTitleAnnotation.class.getName(), "Man faces federal charges for allegedly throwing molotov cocktail at D.C. police car");
		metadata.put(CoreAnnotations.DocSourceTypeAnnotation.class.getName(), "Wapo");
		
		Map<String, ObjectNode> analysis = null; //me.getAnalyzer().analyze(CoreNlpUtils.getDefaultProps(), metadata, me.getText());
		doc = analysis.get("OOP");
		File f = new File(me.getDefaultPath() + "\\" + "OOP_" + "Wapo" + ".json");
        mapper.writeValue(f, doc);
        
		f = new File(me.getDefaultPath() + "\\" + "AGGREGATES_" + "Wapo" + ".json");
        mapper.writeValue(f, analysis.get("AGGREGATES"));
        
		f = new File(me.getDefaultPath() + "\\" + "STANFORD_" + "Wapo" + ".json");
        mapper.writeValue(f, analysis.get("STANFORD"));
		
		//String json = "C:\\Users\\rsada\\eclipse-workspace\\oopcorenlp_web\\WebContent\\Corpora\\IntegrationTest\\OOP_GettysburgAddress.json";
		//doc = mapper.readTree(new File(json));
		
		List<String> mapAnnotators = new ArrayList<String>();
		List<String> scalarAnnotators = new ArrayList<String>();
		List<String> listAnnotators = new ArrayList<String>();
		Iterator<Object> keyIter = null; //CoreNlpUtils.getDefaultProps().keySet().iterator();
		while (keyIter.hasNext()) {
			String key = (String) keyIter.next();
			if (key.startsWith("customAnnotatorClass")) {
				String annotatorClassName = null; //CoreNlpUtils.getDefaultProps().getProperty(key);
				OOPAnnotator annotator = (OOPAnnotator) Class.forName(annotatorClassName).newInstance();
				annotator.init(null);
				CoreAnnotation annotation = (CoreAnnotation) annotator.getAnnotationClass().newInstance();
				if (annotation.getType().getSimpleName().equals("Map")) {
					mapAnnotators.add(annotator.getAnnotationClass().getSimpleName());
				}
				else if (annotation.getType().getSimpleName().equals("BigDecimal")) {
					scalarAnnotators.add(annotator.getAnnotationClass().getSimpleName());
				}
				else if (annotation.getType().getSimpleName().equals("List")) {
					listAnnotators.add(annotator.getAnnotationClass().getSimpleName());
				}
			}
		}
		Collections.sort(scalarAnnotators);
		Collections.sort(mapAnnotators);
		Collections.sort(listAnnotators);
		
		System.out.println("SCALAR");
		for (String annotationName : scalarAnnotators) {
			List<String> tokenMatchTargets = me.generateScalarTokenMatchTargets(doc, annotationName);
			System.out.println(annotationName + ": " + "Token: " + tokenMatchTargets.size());
			//me.testMatchTargets(doc, tokenMatchTargets);
			List<String> sentenceMatchTargets = me.generateScalarSentenceMatchTargets(doc, annotationName);
			System.out.println(annotationName + ": " + "Sentence: " + sentenceMatchTargets.size());
			//me.testMatchTargets(doc, sentenceMatchTargets);
			List<String> documentMatchTargets = me.generateScalarDocumentMatchTargets(doc, annotationName);
			System.out.println(annotationName + ": " + "Document: " + documentMatchTargets.size());
			//me.testMatchTargets(doc, documentMatchTargets);
		}
		
		System.out.println("MAP");
		for (String annotationName : mapAnnotators) {
			List<String> tokenMatchTargets = me.generateMapTokenMatchTargets(doc, annotationName);
			System.out.println(annotationName + ": " + "Token: " + tokenMatchTargets.size());
			//me.testMatchTargets(doc, tokenMatchTargets);
			List<String> sentenceMatchTargets = me.generateMapSentenceMatchTargets(doc, annotationName);
			System.out.println(annotationName + ": " + "Sentence: " + sentenceMatchTargets.size());
			//me.testMatchTargets(doc, sentenceMatchTargets);
			List<String> documentMatchTargets = me.generateMapDocumentMatchTargets(doc, annotationName);
			System.out.println(annotationName + ": " + "Document: " + documentMatchTargets.size());
			//me.testMatchTargets(doc, documentMatchTargets);
		}
		
		System.out.println("LIST");
		for (String annotationName : listAnnotators) {
			List<String> tokenMatchTargets = me.generateListTokenMatchTargets(doc, annotationName);
			System.out.println(annotationName + ": " + "Token: " + tokenMatchTargets.size());
			//me.testMatchTargets(doc, tokenMatchTargets);
			List<String> sentenceMatchTargets = me.generateListSentenceMatchTargets(doc, annotationName);
			System.out.println(annotationName + ": " + "Sentence: " + sentenceMatchTargets.size());
			//me.testMatchTargets(doc, sentenceMatchTargets);
			List<String> documentMatchTargets = me.generateListDocumentMatchTargets(doc, annotationName);
			System.out.println(annotationName + ": " + "Document: " + documentMatchTargets.size());
			//me.testMatchTargets(doc, documentMatchTargets);
		}
	}

}
