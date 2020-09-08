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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.outofprintmagazine.util.IParameterStore;
/**
 * <p>You will need to set azure_apiKey in your IParameterStore.</p>
 * <p>Visit: <a href="https://azure.microsoft.com/en-us/services/cognitive-services/bing-image-search-api/">Azure</a> to sign up.</p> 
 * @author Ram Sadasiv
 */
public class BingUtils {
	
	private static final Logger logger = LogManager.getLogger(BingUtils.class);
	
	@SuppressWarnings("unused")
	private Logger getLogger() {
		return logger;
	}
	
	private IParameterStore parameterStore = null;

	private static Map<IParameterStore, BingUtils> instances = new HashMap<IParameterStore, BingUtils>();
	
	private BingUtils(IParameterStore parameterStore) throws IOException {
		super();
		this.parameterStore = parameterStore;
	}
	    
    public static BingUtils getInstance(IParameterStore parameterStore) throws IOException { 
        if (instances.get(parameterStore) == null) {
            BingUtils instance = new BingUtils(parameterStore);
            instances.put(parameterStore, instance);
        }
        return instances.get(parameterStore); 
    }
    
    public List<String> getImagesByText(String text) throws IOException, URISyntaxException {
    	return getImages(text, "Face");
    }
    
    public List<String> getImagesByTag(String text) throws IOException, URISyntaxException {
    	return getImages(text, null);
    }

    public List<String> getImages(String text, String imageContent) throws IOException, URISyntaxException {
    	URI uri = new URI("https", "api.cognitive.microsoft.com", "bing/v7.0/images/search", null);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("q", text));
		nvps.add(new BasicNameValuePair("imageType", "Photo"));
        if (imageContent != null) {
    		nvps.add(new BasicNameValuePair("imageContent", imageContent));
        }
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Ocp-Apim-Subscription-Key", parameterStore.getProperty("azure_apiKey")));
        JsonNode doc = 
        		HttpUtils.getInstance(parameterStore).httpGetJson(
        				HttpUtils.getInstance(parameterStore).buildUri(uri, nvps), 
        				headers
        		);
    	if (doc.has("value") && doc.get("value").size() > 0) {
    		List<String> retval = new ArrayList<String>();
        	for (JsonNode val : doc.get("value")) {
        		retval.add(val.get("thumbnailUrl").asText()+"&w=240&h=240");
        		//getImageTags(text, val.get("imageInsightsToken").asText());
        	}
        	return retval;
    	}
    	else {
    		return new ArrayList<String>();
    	}
    }
    
    public List<String> getImageTags(String text, String insightsToken) throws IOException, URISyntaxException {
    	URI uri = new URI("https", "api.cognitive.microsoft.com", "bing/v7.0/images/details", null);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("q", text));
		nvps.add(new BasicNameValuePair("insightsToken", insightsToken));
		nvps.add(new BasicNameValuePair("modules", "All"));
		nvps.add(new BasicNameValuePair("mkt", "en-us"));
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Ocp-Apim-Subscription-Key", parameterStore.getProperty("azure_apiKey")));
        JsonNode doc = 
        		HttpUtils.getInstance(parameterStore).httpGetJson(
        				HttpUtils.getInstance(parameterStore).buildUri(uri, nvps), 
        				headers
        		);
    	if (doc.has("imageTags") && doc.get("imageTags").has("value") && doc.get("imageTags").get("value").size() > 0) {
    		List<String> retval = new ArrayList<String>();
        	for (JsonNode val : doc.get("imageTags").get("value")) {
        		Iterator<String> fieldNameIter = val.fieldNames();
        		while (fieldNameIter.hasNext()) {
        			String fieldName = fieldNameIter.next();
        			getLogger().debug(fieldName + ": " + val.get(fieldName).asText());
        		}
        	}
        	return retval;
    	}
    	else {
    		return new ArrayList<String>();
    	}
    }    
    
}
