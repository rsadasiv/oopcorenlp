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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


import io.burt.jmespath.JmesPath;
import io.burt.jmespath.Expression;
import io.burt.jmespath.jackson.JacksonRuntime;

public class ActionlessVerbsAnnotator_Test {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(ActionlessVerbsAnnotator_Test.class);
	
	protected Logger getLogger() {
		return logger;
	}
	
	public ActionlessVerbsAnnotator_Test() {
		// TODO Auto-generated constructor stub
	}
	
	public List<String> generateMatchTargets(JsonNode doc) {
		List<String> matchTargets = new ArrayList<String>();
		for (JsonNode sentence : doc.get("sentences")) {
			for (JsonNode token : sentence.get("tokens")) {
				if (token.has("OOPActionlessVerbsAnnotation")) {
					Iterator<String> fieldsIter = token.get("OOPActionlessVerbsAnnotation").fieldNames();
					while (fieldsIter.hasNext()) {
						String score = fieldsIter.next();
						matchTargets.add(
								String.format(
										"sentences[%d].tokens[%d].OOPActionlessVerbsAnnotation.%s",
										sentence.get("SentenceIndexAnnotation").asInt(),
										token.get("tokenIndex").asInt()-1,
										score
								)
						);			
					}
				}
			}
		}
		return matchTargets;
	}
	
	public List<String> loadMatchTargets() {
		List<String> matchTargets = Arrays.asList(
				"sentences[0].tokens[8].OOPActionlessVerbsAnnotation.bring",
				"sentences[0].tokens[30].OOPActionlessVerbsAnnotation.be",
				"sentences[1].tokens[2].OOPActionlessVerbsAnnotation.be",
				"sentences[2].tokens[1].OOPActionlessVerbsAnnotation.be",
				"sentences[3].tokens[1].OOPActionlessVerbsAnnotation.have",
				"sentences[3].tokens[2].OOPActionlessVerbsAnnotation.come",
				"sentences[4].tokens[1].OOPActionlessVerbsAnnotation.be",
				"sentences[4].tokens[9].OOPActionlessVerbsAnnotation.do",
				"sentences[6].tokens[12].OOPActionlessVerbsAnnotation.have",
				"sentences[7].tokens[8].OOPActionlessVerbsAnnotation.remember",
				"sentences[7].tokens[11].OOPActionlessVerbsAnnotation.say",
				"sentences[7].tokens[18].OOPActionlessVerbsAnnotation.forget",
				"sentences[7].tokens[21].OOPActionlessVerbsAnnotation.do",
				"sentences[8].tokens[1].OOPActionlessVerbsAnnotation.be",
				"sentences[8].tokens[10].OOPActionlessVerbsAnnotation.be",
				"sentences[8].tokens[22].OOPActionlessVerbsAnnotation.have"				
		);
		return matchTargets;
	}
	
	public void testMatchTargets(JsonNode doc, List<String> matchTargets) {
		JmesPath<JsonNode> jmespath = new JacksonRuntime();
		
		for (String expr : matchTargets) {
			Expression<JsonNode> expression = jmespath.compile(expr);
			JsonNode result = expression.search(doc);
			if (result.isNull()) {
				getLogger().debug("FAILED: " + expr);
			}
			else {
				getLogger().debug("PASSED: " + expr);
			}
		}
	}
	
	public void test() throws JsonProcessingException, IOException {

		String json = "C:\\Users\\rsada\\eclipse-workspace\\oopcorenlp_web\\WebContent\\Corpora\\IntegrationTest\\OOP_GettysburgAddress.json";
		ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
		JsonNode doc = mapper.readTree(new File(json));
		testMatchTargets(doc, generateMatchTargets(doc));
//		JmesPath<JsonNode> jmespath = new JacksonRuntime();
//		String expr = "sentences[].tokens[?TokensAnnotation.lemma=='be']";
//		Expression<JsonNode> expression = jmespath.compile(expr);
//		JsonNode result = expression.search(doc);
//		System.out.println(result.toPrettyString());
	}
	
	public static void main(String[] argv) throws JsonProcessingException, IOException {
		ActionlessVerbsAnnotator_Test me = new ActionlessVerbsAnnotator_Test();
		me.test();
	}

}
