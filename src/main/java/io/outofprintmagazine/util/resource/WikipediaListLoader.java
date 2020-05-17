package io.outofprintmagazine.util.resource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.stanford.nlp.io.IOUtils;
import io.outofprintmagazine.nlp.pipeline.annotators.AbstractAnnotator;

public class WikipediaListLoader {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(WikipediaListLoader.class);

	public WikipediaListLoader() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws IOException {
		WikipediaListLoader me = new WikipediaListLoader();
//		List<String> wikiResources = me.loadWikipediaFile("C:\\Users\\rsada\\eclipse-workspace\\oopcorenlp\\src\\main\\resources\\io\\outofprintmagazine\\nlp\\models\\List of Indian film actors - Wikipedia.html");
//		List<String> wikiResources = me.loadWikipediaFile("C:\\Users\\rsada\\eclipse-workspace\\oopcorenlp\\src\\main\\resources\\io\\outofprintmagazine\\nlp\\models\\List of Pakistani actors - Wikipedia.html");
//		List<String> wikiResources = me.loadWikipediaFile("C:\\Users\\rsada\\eclipse-workspace\\oopcorenlp\\src\\main\\resources\\io\\outofprintmagazine\\nlp\\models\\List of Indian film actresses - Wikipedia.html");
		//List<String> wikiResources = me.loadWikipediaFile("C:\\Users\\rsada\\eclipse-workspace\\oopcorenlp\\src\\main\\resources\\io\\outofprintmagazine\\nlp\\models\\List of Pakistani actresses - Wikipedia.html");

		//List<String> existingResources = me.readResourceFromFile("C:\\Users\\rsada\\eclipse-workspace\\oopcorenlp\\src\\main\\resources\\io\\outofprintmagazine\\nlp\\models\\FemaleNames.txt");

		List<String> wikiResources = me.loadWikipediaFile("C:\\Users\\rsada\\eclipse-workspace\\oopcorenlp\\src\\main\\resources\\io\\outofprintmagazine\\nlp\\models\\List of culinary herbs and spices - Wikipedia.html");
		List<String> existingResources = new ArrayList<String>();
		for (String resource : wikiResources) {
			if (!existingResources.contains(resource)) {
				existingResources.add(resource);
			}
		}
		Collections.sort(existingResources);
		me.writeResourceToFile(existingResources, "C:\\Users\\rsada\\eclipse-workspace\\oopcorenlp\\src\\main\\resources\\io\\outofprintmagazine\\nlp\\models\\Spices.txt");
	}
	
	protected List<String> loadWikipediaFile(String fileName) throws IOException {
		logger.debug(fileName);
		if (fileName.startsWith("http")) {
			return parseWikipediaPage(Jsoup.parse(fileName));
		}
		else {
			File input = new File(fileName);
			Document doc = Jsoup.parse(input, "UTF-8", "");
			return parseWikipediaPage(doc);
		}
	}
	
	protected List<String> parseWikipediaPage(Document doc) {
		List<String> resources = new ArrayList<String>();
		//#mw-content-text > div > div:nth-child(13) > ul > li:nth-child(8) > a
		Elements mwpages = doc.select("#mw-content-text > div > div > ul > li > a");
		for (Element body : mwpages) {
			String[] linkTextEntries = body.html().split(" ");
			if (linkTextEntries.length > 0) {
				//if (linkTextEntries[0].length()>2) {
				if (! resources.contains(linkTextEntries[0].replaceAll(",","").toLowerCase())) {
					resources.add(linkTextEntries[0].replaceAll(",","").toLowerCase());
				}
			}
		}
		return resources;
	}
	
	protected void writeResourceToFile(List<String> arr, String fileName) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(fileName);

			for(String str: arr) {
				writer.write(str + System.lineSeparator());
			}
			writer.close();
		}
		catch (Exception e) {
			logger.error(e);
		}
		finally {
			if (writer != null) {
				try {
					writer.close();
				}
				catch (Exception e) {
					logger.error(e);
				}
			}
		}
	}
	
	protected List<String> readResourceFromFile(String fileName) {
		List<String> resources = new ArrayList<String>();
	    List<String> nameFileEntries = IOUtils.linesFromFile(fileName);
	    for (String nameCSV : nameFileEntries) {
	    	String[] nameValue = nameCSV.split(" ");
	    	if (nameValue.length > 1) {
	    		resources.add(nameValue[0].toLowerCase());
	    	}
	    	else {
	    		String[] namesForThisLine = nameCSV.split(",");
	    		for (String name : namesForThisLine) {
	    			resources.add(name.trim().toLowerCase());
	    		}
	    	}
	    }
	    return resources;
	}

}
