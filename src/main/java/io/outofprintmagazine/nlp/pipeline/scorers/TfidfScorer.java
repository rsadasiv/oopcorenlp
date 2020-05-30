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
package io.outofprintmagazine.nlp.pipeline.scorers;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.stanford.nlp.ling.CoreAnnotations;

public class TfidfScorer {

	public TfidfScorer() {
		super();
	}
	
	public TfidfScorer(Map<String, JsonNode> corpusAggregates) {
		this();
		this.setCorpusAggregates(corpusAggregates);
	}
	
	private Map<String, JsonNode> corpusAggregates = new HashMap<String, JsonNode>();
	
	public Map<String, JsonNode> getCorpusAggregates() {
		return corpusAggregates;
	}

	public void setCorpusAggregates(Map<String, JsonNode> corpusAggregates) {
		this.corpusAggregates = corpusAggregates;
	}
	
	public String makeDisplayName(String aggregateName) {
		return aggregateName.substring(0, aggregateName.lastIndexOf("Aggregate"))+"Tfidf";
	}

	//for a given corpus
	//for each document
	//for each document aggregate score in 
	//	OOPNounsAnnotationAggregate
	//	OOPWordsAnnotationAggregate
	//	OOPWikipediaCategoriesAnnotationAggregate
	//	OOPVerbsAnnotationAggregate
	//for each score in aggregatedScores
	//get score.name
	//tf = get score.raw
	//for the document_aggregate_score_name in corpus scores
	//idf_n = get aggregatedScores.size()
	//where detailScores.name == score.name
	//idf_d = get raw.scoreStats.score.count
	//tfidf = tf * (ln((idf_n/(1+idf_d))))
	//write document.tfidf file <- document_aggregate_score_name.score_name = tfidf
	
	//https://en.wikipedia.org/wiki/Tf%E2%80%93idf
	
	public ObjectNode scoreDocument(ObjectNode documentAggregate) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode tfidf = mapper.createObjectNode();
		tfidf.put(CoreAnnotations.DocIDAnnotation.class.getName(), documentAggregate.get(CoreAnnotations.DocIDAnnotation.class.getName()).asText());
		tfidf.put(CoreAnnotations.DocTitleAnnotation.class.getName(), documentAggregate.get(CoreAnnotations.DocTitleAnnotation.class.getName()).asText());
		tfidf.put(CoreAnnotations.DocSourceTypeAnnotation.class.getName(), documentAggregate.get(CoreAnnotations.DocSourceTypeAnnotation.class.getName()).asText());
		tfidf.put(CoreAnnotations.DocTypeAnnotation.class.getName(), documentAggregate.get(CoreAnnotations.DocTypeAnnotation.class.getName()).asText());
		tfidf.put(CoreAnnotations.AuthorAnnotation.class.getName(), documentAggregate.get(CoreAnnotations.AuthorAnnotation.class.getName()).asText());
		tfidf.put(CoreAnnotations.DocDateAnnotation.class.getName(), documentAggregate.get(CoreAnnotations.DocDateAnnotation.class.getName()).asText());		
		for (String scoreName : corpusAggregates.keySet()) {
			ObjectNode tfidfScore = tfidf.putObject(makeDisplayName(scoreName));
			ObjectNode documentAggregateScore = (ObjectNode) documentAggregate.get(scoreName);
			ArrayNode aggregatedScores = (ArrayNode)documentAggregateScore.get("aggregatedScores");
			for (JsonNode aggregatedScore : aggregatedScores) {
				String subScoreName = aggregatedScore.get("name").asText();
				int subScoreRaw = aggregatedScore.get("score").get("raw").asInt();
				JsonNode corpusAggregateScore = corpusAggregates.get(scoreName);
				int corpusDocumentsCount = corpusAggregateScore.get("aggregatedScores").size();
				for (JsonNode corpusAggregatedScore : corpusAggregateScore.get("detailScores")) {
					if (corpusAggregatedScore.get("name").asText().equals(subScoreName)) {
						int corpusDocumentsMatchedCount = corpusAggregatedScore.get("aggregatedScores").size();
						BigDecimal tfidfValue = new BigDecimal(subScoreRaw * (java.lang.Math.log((corpusDocumentsCount/(1+corpusDocumentsMatchedCount)))));
						tfidfScore.put(subScoreName, tfidfValue);
						break;
					}
				}
			}
		}
		return tfidf;
	}
	
}
