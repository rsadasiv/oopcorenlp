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
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.outofprintmagazine.util.ParameterStore;

public class WikimediaUtils {
	
	private static final Logger logger = LogManager.getLogger(WikimediaUtils.class);
	
	private static final int BATCH_SIZE = 20;
	private ObjectMapper mapper = null;
	private String apiKey = null;
	
	private WikimediaUtils(ParameterStore parameterStore) throws IOException {
		//this.apiKey = "OOPCoreNlp/0.9 (rsadasiv@gmail.com) httpclient/4.5.6";
		this.apiKey = parameterStore.getProperty("wikipedia_apikey");
		mapper = new ObjectMapper();
	}
	
	private static Map<ParameterStore, WikimediaUtils> instances = new HashMap<ParameterStore, WikimediaUtils>();
	
    public static WikimediaUtils getInstance(ParameterStore parameterStore) throws IOException { 
        if (instances.get(parameterStore) == null) {
        	WikimediaUtils instance = new WikimediaUtils(parameterStore);
            instances.put(parameterStore, instance);
        }
        return instances.get(parameterStore); 
    }
    
    public List<String> getImagesByText(String text) throws IOException, URISyntaxException {
    	logger.debug("byText: " + text);
    	List<String> retval = getImages(text);
    	for (String imgurl : retval) {
    		logger.debug(imgurl);
    	}
    	return retval;
    }
    
    public List<String> getImagesByTag(String text) throws IOException, URISyntaxException {
    	logger.debug("byTag: " + text);
    	List<String> retval = getImages(text);
    	for (String imgurl : retval) {
    		logger.debug(imgurl);
    	}
    	return retval;
    }
    
    //https://en.wikipedia.org/w/api.php?action=query&generator=images&titles=Coimbatore&prop=info
    //https://en.wikipedia.org/w/api.php?action=query&titles=File:Coimbatore-TNSTC-JnNURM-Bus.JPG&prop=imageinfo&iiprop=timestamp|user|userid|comment|canonicaltitle|url|size|dimensions|sha1|mime|thumbmime|mediatype|bitdepth
    
    public List<String> getImages(String title) throws IOException, URISyntaxException {
    	List<String> retval = new ArrayList<String>();
    	List<String> imageTitles = new ArrayList<String>();
		title = title.replace(' ', '_');
		String gimcontinue = "init";
		while (gimcontinue != null) {
			JsonNode rootNode = null;
	
			if (gimcontinue.equals("init")) {
				rootNode = mapper.readValue(new URL("https://en.wikipedia.org/w/api.php?format=json&action=query&generator=images&prop=info&titles="+(URLEncoder.encode(title, "UTF-8"))), JsonNode.class);
			}
			else {
				rootNode = mapper.readValue(new URL("https://en.wikipedia.org/w/api.php?format=json&action=query&generator=images&prop=info&titles="+(URLEncoder.encode(title, "UTF-8")+"&gimcontinue="+gimcontinue)), JsonNode.class);
			}
			if (rootNode.get("continue") != null) {
				gimcontinue = rootNode.get("continue").get("gimcontinue").asText();
			}
			else {
				gimcontinue = null;
			}
			if (rootNode != null && rootNode.get("query") != null && rootNode.get("query").get("pages") != null) {
				JsonNode pagesNode = rootNode.get("query").get("pages");
				Iterator<Entry<String, JsonNode>> pagesIter = pagesNode.fields();
				while (pagesIter.hasNext()) {
					imageTitles.add(pagesIter.next().getValue().get("title").asText());
				}
			}
		}
		
		Iterator<String> imageTitlesIter = imageTitles.iterator();
		while (imageTitlesIter.hasNext()) {
			List<String> queries = new ArrayList<String>();
			for (int i=0;i<BATCH_SIZE&&imageTitlesIter.hasNext();i++) {
				queries.add(imageTitlesIter.next().replace(' ', '_'));
			}
			JsonNode rootNode = mapper.readValue(
					new URL(
							"https://en.wikipedia.org/w/api.php?format=json&action=query&prop=imageinfo&iiprop=timestamp|user|userid|comment|canonicaltitle|url|size|dimensions|sha1|mime|thumbmime|mediatype|bitdepth&titles="
							+(URLEncoder.encode(
									String.join("|", queries)
									, "UTF-8")
							)
					), 
					JsonNode.class
			);
			JsonNode pagesNode = rootNode.get("query").get("pages");
			Iterator<Entry<String, JsonNode>> pagesIter = pagesNode.fields();
			while (pagesIter.hasNext()) {
				JsonNode pageNode = pagesIter.next().getValue().get("imageinfo").get(0);
				if (pageNode.get("mediatype").asText().equalsIgnoreCase("BITMAP")) {
					retval.add(pageNode.get("url").asText());
				}
			}
			
		}
		return retval;
    }
    
}
