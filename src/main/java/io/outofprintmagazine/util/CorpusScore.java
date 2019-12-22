package io.outofprintmagazine.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CorpusScore {

	public CorpusScore() {
		super();
	}
	
	public CorpusScore(String name) {
		this();
		setName(name);
	}
	
	//Submissions
	private String name = "";
	
	//OOPLocationsAnnotation, OOPPeopleAnnotation
	private Map<String, CorpusDocumentAggregateScore> corpusAggregateScore = new HashMap<String, CorpusDocumentAggregateScore>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, CorpusDocumentAggregateScore> getCorpusAggregateScore() {
		return corpusAggregateScore;
	}

	public void setCorpusAggregateScore(Map<String, CorpusDocumentAggregateScore> corpusAggregateScore) {
		this.corpusAggregateScore = corpusAggregateScore;
	}


	
	

}
