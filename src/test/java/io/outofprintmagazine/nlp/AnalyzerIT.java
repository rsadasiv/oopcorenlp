package io.outofprintmagazine.nlp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.burt.jmespath.Expression;
import io.burt.jmespath.JmesPath;
import io.burt.jmespath.jackson.JacksonRuntime;
import io.outofprintmagazine.util.IParameterStore;
import io.outofprintmagazine.util.ParameterStoreLocal;

public class AnalyzerIT {

	public AnalyzerIT() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		super();
		loadGoldSource();
		loadCandidate();
	}
	
	private static JsonNode candidate = null;
	private static JsonNode goldSource = null;
	private static Map<String, ObjectNode> output = null;
	
	/**
	 * 	#http://wordnetcode.princeton.edu/wn3.1.dict.tar.gz
	 *  wordNet_location=dict
	 *  #http://verbs.colorado.edu/verb-index/vn/verbnet-3.3.tar.gz
	 *  verbNet_location=verbnet3.3/
     *
	 *	#https://www.mediawiki.org/wiki/API:Etiquette
	 *	wikipedia_apikey=OOPCoreNlp/0.9.1 httpclient/4.5.6
	*/
	private IParameterStore parameterStore = null;

	public IParameterStore getParameterStore() throws IOException {
		if (parameterStore == null) {
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode p = mapper.createObjectNode();
			p.put("wordNet_location", "../data/dict");
			p.put("verbNet_location", "../data/verbnet3.3");
			p.put("wikipedia_apikey", "OOPCoreNlp/0.9.1 httpclient/4.5.6");
			parameterStore = new ParameterStoreLocal(p);
		}
		return parameterStore;
	}
	
	@SuppressWarnings("unused")
	private void writeFiles(Map<String, ObjectNode> output) throws IOException {
		for (String outputType : output.keySet()) {
			writeFile(".", outputType + "_GettysburgAddress.json", output.get(outputType));
		}
	}
	
	private void writeFile(String path, String fileName, JsonNode content) throws IOException {
		File f = new File(path + System.getProperty("file.separator", "/") + fileName);
		ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
		mapper.configure(com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
		mapper.writeValue(f, content);
	}
	
	private void loadGoldSource() throws IOException {
		if (goldSource == null) {
			ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
			mapper.configure(com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
			JsonNode doc = mapper.readTree(getClass().getClassLoader().getResourceAsStream("io/outofprintmagazine/OOP_GettysburgAddress.json"));
			goldSource = doc;
		}
	}
	
	private void loadCandidate() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (candidate == null || output == null) {
			SimpleDateFormat fmt = new SimpleDateFormat("DD-mm-YYYY");
			Properties props = new Properties();
			props.setProperty("DocTitleAnnotation", "GettysburgAddress");
			props.setProperty("DocSourceTypeAnnotation", "IT");
			props.setProperty("DocTypeAnnotation", "OOP");
			props.setProperty("AuthorAnnotation", "Abraham Lincoln");
			props.setProperty("DocDateAnnotation", fmt.format(new Date(System.currentTimeMillis())));
			props.setProperty("DocIDAnnotation", "1");
			String text = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("io/outofprintmagazine/GettysburgAddress.txt"), "utf-8");
			List<String> annotators = IOUtils.readLines(getClass().getClassLoader().getResourceAsStream("io/outofprintmagazine/util/annotators.txt"), "utf-8");
			Analyzer analyzer = new Analyzer(
					getParameterStore(), 
					annotators
			);
			
			output = analyzer.analyze(
					props, 
					text
			);
			candidate = output.get("OOP");
		}
	}
	
		
	private List<String> generateMapTokenMatchTargets(JsonNode doc, String annotation) {
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
	
	private List<String> generateMapSentenceMatchTargets(JsonNode doc, String annotation) {
		List<String> matchTargets = new ArrayList<String>();
		for (JsonNode sentence : doc.get("sentences")) {
			if (sentence.has(annotation)) {
				Iterator<String> fieldsIter = sentence.get(annotation).fieldNames();
				while (fieldsIter.hasNext()) {
					String score = fieldsIter.next();
					matchTargets.add(
							String.format(
									"sentences[%d].%s.\"%s\"",
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
	
	private List<String> generateMapDocumentMatchTargets(JsonNode doc, String annotation) {
		List<String> matchTargets = new ArrayList<String>();
		if (doc.has(annotation)) {
			Iterator<String> fieldsIter = doc.get(annotation).fieldNames();
			while (fieldsIter.hasNext()) {
				String score = fieldsIter.next();
				matchTargets.add(
						String.format(
								"%s.\"%s\"",
								annotation,
								score
						)
				);
			}
		}
		
		return matchTargets;
	}
	
	
	/*
	 * load gold source gettysburg address json
	 * generate match targets based on gold source
	 * analyze gettysburg address txt
	 * for each annotator
	 * for each generated match target
	 * test output
	 */
	
	@Test
	public void analyzerIT_Test() throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		assertTrue(output.containsKey("OOP"), "OOP output missing");
		assertTrue(output.containsKey("STANFORD"), "STANFORD output missing");
		assertTrue(output.containsKey("PIPELINE"), "PIPELINE output missing");
		assertTrue(output.containsKey("AGGREGATES"), "AGGREGATES output missing");

		writeFiles(output);
	}
	

	@Test
	public void actionlessVerbsAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPActionlessVerbsAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void adjectiveCategoriesAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPAdjectiveCategoriesAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void adjectivesAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPAdjectivesAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void adverbCategoriesAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPAdverbCategoriesAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void adverbsAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPAdverbsAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void americanizeAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPAmericanizeAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void angliciseAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPAngliciseAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void biberAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPBiberAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void biberDimensionsAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPBiberDimensionsAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void colorsAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPColorsAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void commonWordsAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPCommonWordsAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void coreNlpGenderAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "CoreNlpGenderAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void coreNlpParagraphAnnotatorIT_Test() {
		String annotationName = "OOPParagraphCountAnnotation";
		assertEquals(goldSource.get(annotationName).asText(), candidate.get(annotationName).asText());
	}
	
	@Test
	public void coreNlpSentimentAnnotatorIT_Test() {
		String annotationName = "CoreNlpSentimentAnnotation";
		assertEquals(goldSource.get(annotationName).asText(), candidate.get(annotationName).asText());
	}
	
	@Test
	public void datesAnnotatorIT_Test() {
		String annotationName = "OOPDatesAnnotation";
		assertEquals(
				((ArrayNode)goldSource.get(annotationName)).size(), 
				((ArrayNode)candidate.get(annotationName)).size()
		);
	}
	
	@Test
	public void flavorsAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPFlavorsAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void fleschKincaidAnnotatorIT_Test() {
		String annotationName = "OOPFleschKincaidAnnotation";
		assertEquals(goldSource.get(annotationName).asText(), candidate.get(annotationName).asText());
	}
	
	@Test
	public void functionWordsAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPFuntionWordsAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void genderAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPGenderAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void locationsAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPLocationsAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void nonAffirmativeAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPNonAffirmativeAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void nounGroupsAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPNounGroupsAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void nounHypernymsAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPNounHypernymsAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void nounsAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPNounsAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void peopleAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPPeopleAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void pointlessAdjectivesAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPPointlessAdjectivesAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void pointlessAdverbsAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPPointlessAdverbsAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void possessivesAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPPossessivesAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void prepositionCategoriesAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPPrepositionCategoriesAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void prepositionsAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPPrepositionsAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void pronounAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPPronounAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void punctuationMarkAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPPunctuationMarkAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void quotesAnnotatorIT_Test() {
		String annotationName = "OOPQuotesAnnotation";
		assertEquals(
				((ArrayNode)goldSource.get(annotationName)).size(), 
				((ArrayNode)candidate.get(annotationName)).size()
		);
	}
	
	@Test
	public void svoAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPSVOAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void topicsAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPTopicsAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void uncommonWordsAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPUncommonWordsAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void vaderSentimentAnnotatorIT_Test() {
		String annotationName = "VaderSentimentAnnotation";
		assertEquals(goldSource.get(annotationName).asText(), candidate.get(annotationName).asText());
	}
	
	@Test
	public void verbGroupsAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPVerbGroupsAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void verbHypernymsAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPVerbHypernymsAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void verblessSentencesAnnotatorIT_Test() {
		String annotationName = "OOPVerblessSentencesAnnotation";
		assertEquals(goldSource.get(annotationName).asText(), candidate.get(annotationName).asText());
	}
	
	@Test
	public void verbnetGroupsAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPVerbnetGroupsAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void verbsAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPVerbsAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void verbTenseAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPVerbTenseAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void wikipediaCategoriesAnnotatorIT_Test() {
		String annotationName = "OOPWikipediaCategoriesAnnotation";
		assertTrue(
				Math.abs(
						(((ArrayNode)goldSource.get(annotationName)).size()) - (((ArrayNode)candidate.get(annotationName)).size())
				) < 3,
				String.format(
						"%s : Document: %s", 
						((ArrayNode)goldSource.get(annotationName)).size(),
						((ArrayNode)candidate.get(annotationName)).size()
				)
		);
	}
	
	@Test
	public void wikipediaPageviewTopicsAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPWikipediaPageviewTopicsAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	
	@Test
	public void wordlessWordsAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPWordlessWordsAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void wordsAnnotatorIT_Test() {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		String annotationName = "OOPWordsAnnotation";
		for (String expr : generateMapTokenMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Token: %s", annotationName, expr));
		}

		for (String expr : generateMapSentenceMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Sentence: %s", annotationName, expr));
		}

		for (String expr : generateMapDocumentMatchTargets(goldSource, annotationName)) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			assertFalse(expression.search(candidate).isNull(), String.format("%s : Document: %s", annotationName, expr));
		}
	}
	
	@Test
	public void becauseAnnotatorIT_Test() {
		String annotationName = "OOPBecauseAnnotation";
		assertEquals(
				((ArrayNode)goldSource.get(annotationName)).size(), 
				((ArrayNode)candidate.get(annotationName)).size()
		);
	}
	
	@Test
	public void ifAnnotatorIT_Test() {
		String annotationName = "OOPIfAnnotation";
		assertEquals(
				((ArrayNode)goldSource.get(annotationName)).size(), 
				((ArrayNode)candidate.get(annotationName)).size()
		);
	}
	
	@Test
	public void charCountAnnotatorIT_Test() {
		String annotationName = "OOPCharCountAnnotation";
		assertEquals(goldSource.get(annotationName).asText(), candidate.get(annotationName).asText());
	}
	
	@Test
	public void paragraphCountAnnotatorIT_Test() {
		String annotationName = "OOPParagraphCountAnnotation";
		assertEquals(goldSource.get(annotationName).asText(), candidate.get(annotationName).asText());
	}
	
	@Test
	public void sentenceCountAnnotatorIT_Test() {
		String annotationName = "OOPSentenceCountAnnotation";
		assertEquals(goldSource.get(annotationName).asText(), candidate.get(annotationName).asText());
	}
	
	@Test
	public void syllableCountAnnotatorIT_Test() {
		String annotationName = "OOPSyllableCountAnnotation";
		assertEquals(goldSource.get(annotationName).asText(), candidate.get(annotationName).asText());
	}
	
	@Test
	public void tokenCountAnnotatorIT_Test() {
		String annotationName = "OOPTokenCountAnnotation";
		assertEquals(goldSource.get(annotationName).asText(), candidate.get(annotationName).asText());
	}
	
	@Test
	public void wordCountAnnotatorIT_Test() {
		String annotationName = "OOPWordCountAnnotation";
		assertEquals(goldSource.get(annotationName).asText(), candidate.get(annotationName).asText());
	}
	
	@Test
	public void howAnnotatorIT_Test() {
		String annotationName = "OOPHowAnnotation";
		assertEquals(
				((ArrayNode)goldSource.get(annotationName)).size(), 
				((ArrayNode)candidate.get(annotationName)).size()
		);
	}
	
	@Test
	public void whatAnnotatorIT_Test() {
		String annotationName = "OOPWhatAnnotation";
		assertEquals(
				((ArrayNode)goldSource.get(annotationName)).size(), 
				((ArrayNode)candidate.get(annotationName)).size()
		);
	}
	
	@Test
	public void whenAnnotatorIT_Test() {
		String annotationName = "OOPWhenAnnotation";
		assertEquals(
				((ArrayNode)goldSource.get(annotationName)).size(), 
				((ArrayNode)candidate.get(annotationName)).size()
		);
	}
	
	@Test
	public void whereAnnotatorIT_Test() {
		String annotationName = "OOPWhereAnnotation";
		assertEquals(
				((ArrayNode)goldSource.get(annotationName)).size(), 
				((ArrayNode)candidate.get(annotationName)).size()
		);
	}
	
	@Test
	public void whoAnnotatorIT_Test() {
		String annotationName = "OOPWhoAnnotation";
		assertEquals(
				((ArrayNode)goldSource.get(annotationName)).size(), 
				((ArrayNode)candidate.get(annotationName)).size()
		);
	}
	
	@Test
	public void whyAnnotatorIT_Test() {
		String annotationName = "OOPWhyAnnotation";
		assertEquals(
				((ArrayNode)goldSource.get(annotationName)).size(), 
				((ArrayNode)candidate.get(annotationName)).size()
		);
	}
	
	@Test
	public void asAnnotatorIT_Test() {
		String annotationName = "OOPAsAnnotation";
		assertEquals(
				((ArrayNode)goldSource.get(annotationName)).size(), 
				((ArrayNode)candidate.get(annotationName)).size()
		);
	}
	
	@Test
	public void likeAnnotatorIT_Test() {
		String annotationName = "OOPLikeAnnotation";
		assertEquals(
				((ArrayNode)goldSource.get(annotationName)).size(), 
				((ArrayNode)candidate.get(annotationName)).size()
		);
	}
}
