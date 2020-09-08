package io.outofprintmagazine.nlp.utils;

import com.fasterxml.jackson.databind.JsonNode;

public interface IJsonResponseHandler {
	
	public void onPage(JsonNode page);

}
