package io.outofprintmagazine.nlp;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WikimediaUtils {
	
	private static final Logger logger = LogManager.getLogger(WikimediaUtils.class);
	
	private static final int BATCH_SIZE = 20;
	private ObjectMapper mapper = null;
	private String apiKey = null;
	
	private WikimediaUtils() throws IOException {
		this.apiKey = "OOPCoreNlp/0.9 (rsadasiv@gmail.com) httpclient/4.5.6";
		mapper = new ObjectMapper();
	}
	
	private static WikimediaUtils single_instance = null; 

    public static WikimediaUtils getInstance() throws IOException { 
        if (single_instance == null) 
            single_instance = new WikimediaUtils(); 
  
        return single_instance; 
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
			JsonNode pagesNode = rootNode.get("query").get("pages");
			Iterator<Entry<String, JsonNode>> pagesIter = pagesNode.fields();
			while (pagesIter.hasNext()) {
				imageTitles.add(pagesIter.next().getValue().get("title").asText());
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

    public static void main(String[] argv) throws IOException, URISyntaxException {
    	List<String> values = WikimediaUtils.getInstance().getImagesByText("Coimbatore");
    	for (String val : values) {
    		logger.debug(val);
    	}
    	
    }
    
}
