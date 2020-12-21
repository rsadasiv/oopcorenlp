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
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.outofprintmagazine.util.IParameterStore;

public class WikipediaUtils {
	
	private static final Logger logger = LogManager.getLogger(WikipediaUtils.class);
	
	@SuppressWarnings("unused")
	private Logger getLogger() {
		return logger;
	}

	private static final int WORD_BATCH_SIZE = 20;
	private static final int WORD_CACHE_SIZE = 10000;
	private static final int CACHE_SIZE = 5000;
	private IParameterStore parameterStore = null;	
	
	private Deque mruPageviewList = new LinkedList<String>();
	private Map<String, BigDecimal> pageviewCache = new HashMap<String, BigDecimal>();
	private Deque mruCategoriesList = new LinkedList<String>();
	private Map<String, Map<String,BigDecimal>> categoriesCache = new HashMap<String, Map<String,BigDecimal>>();
	private Deque mruWordList = new LinkedList<String>();
	private Map<String, String> wordCache = new HashMap<String, String>();
	
	
    public BigDecimal getPageviewCache(String topic) {
    	BigDecimal retval = pageviewCache.get(topic);
    	if (retval != null) {
    		mruPageviewList.remove(topic);
    		mruPageviewList.push(topic);
    	}
    	return retval;
    }
    
	public void putPageviewCache(String topic, BigDecimal score) {
		mruPageviewList.push(topic);
		pageviewCache.put(topic, score);
    	for (int i=WikipediaUtils.CACHE_SIZE;i<mruPageviewList.size();i++) {
    		String token = (String)mruPageviewList.pollLast();
    		if (token != null) {
    			pageviewCache.remove(token);
    		}
    	}
    }
    
    public Map<String,BigDecimal> getCategoriesCache(String topic) {
    	Map<String,BigDecimal> retval = categoriesCache.get(topic);
    	if (retval != null) {
    		mruCategoriesList.remove(topic);
    		mruCategoriesList.push(topic);
    	}
    	return retval;
    }
    
	public void putCategoryCache(String topic, Map<String,BigDecimal> score) {
		mruCategoriesList.push(topic);
		categoriesCache.put(topic, score);
    	for (int i=WikipediaUtils.CACHE_SIZE;i<mruCategoriesList.size();i++) {
    		String token = (String)mruCategoriesList.pollLast();
    		if (token != null) {
    			categoriesCache.remove(token);
    		}
    	}
    }

	
	private WikipediaUtils(IParameterStore parameterStore) throws IOException {
		super();
		this.parameterStore = parameterStore;
	}
	
	private static Map<IParameterStore, WikipediaUtils> instances = new HashMap<IParameterStore, WikipediaUtils>();
	
    public static WikipediaUtils getInstance(IParameterStore parameterStore) throws IOException { 
        if (instances.get(parameterStore) == null) {
        	WikipediaUtils instance = new WikipediaUtils(parameterStore);
            instances.put(parameterStore, instance);
        }
        return instances.get(parameterStore); 
    }
    
	public BigDecimal getWikipediaPageviewsForTopic(String topic) throws MalformedURLException, UnsupportedEncodingException, IOException, URISyntaxException {
		BigDecimal retval = getPageviewCache(topic);
		if (retval != null) {
			return retval;
		}
		ObjectMapper mapper = new ObjectMapper();
		Calendar calendar = Calendar.getInstance();
		String endDate = Integer.toString(calendar.get(Calendar.YEAR)) + String.format("%02d", calendar.get(Calendar.MONTH)+1) + "01";
		calendar.add(Calendar.MONTH, -1);
		String startDate = Integer.toString(calendar.get(Calendar.YEAR)) + String.format("%02d", calendar.get(Calendar.MONTH)+1) + "01";
		
		retval = new BigDecimal(0);
		for (String title : getWikipediaPagesForTopic(topic)) {
			title = title.replace(' ', '_');
			JsonNode obj = mapper.readTree(
					new URL(
							"https://wikimedia.org/api/rest_v1/metrics/pageviews/per-article/en.wikipedia.org/all-access/all-agents/" 
							+ URLEncoder.encode(title,StandardCharsets.UTF_8.name()) 
							+ "/monthly/"
							+ startDate
							+ "/"
							+ endDate
							)
					);
			retval = retval.add(new BigDecimal(obj.get("items").get(0).get("views").asInt()));
		}
		putPageviewCache(topic, retval);
		return retval;
	
	}
	
    protected class CategoriesForTopicPageHandler implements IJsonResponseHandler {
    	Map<String,BigDecimal> scoreMap = new HashMap<String,BigDecimal>();
    	
    	public CategoriesForTopicPageHandler() {
    		super();
    	}
    	
    	public Map<String,BigDecimal> getValues() {
    		return scoreMap;
    	}
    	
    	@Override
		public void onPage(JsonNode page) {
    		if (page.has("query") && page.get("query").has("pages")) {
	    		JsonNode pageCategoriesNode = page.get("query").get("pages");
				Iterator<Entry<String, JsonNode>> fieldsIter = pageCategoriesNode.fields();
				while (fieldsIter.hasNext()) {
					JsonNode categoriesNode = fieldsIter.next().getValue().get("categories");
					if (categoriesNode != null && categoriesNode.isArray()) {
						for (final JsonNode categoryNode : categoriesNode) {
							String categoryName = categoryNode.get("title").asText().substring("Category:".length());
							if (!categoryName.startsWith("All") 
									&& !categoryName.startsWith("Articles")
									&& !categoryName.startsWith("Wikipedia") 
									&& !categoryName.startsWith("CS1") 
									&& !categoryName.startsWith("Pages") 
									&& !categoryName.startsWith("Commons") 
									&& !categoryName.startsWith("Disambiguation")
									&& !categoryName.contains("disambiguation")
									&& !categoryName.contains("wayback")
									&& !categoryName.contains("dates")
									&& !categoryName.contains("articles")
									&& !categoryName.contains("inventions")
									&& !categoryName.contains("time")									
									&& !categoryName.contains("Orders of magnitude")
									&& !categoryName.startsWith("Cleanup")
									&& !categoryName.startsWith("Lists")
									&& !categoryName.startsWith("Vague")
									&& !categoryName.startsWith("Interlanguage")
									&& !categoryName.startsWith("Webarchive")
									&& !categoryName.startsWith("SI")
									&& !categoryName.startsWith("Use")
									&& !categoryName.startsWith("Portal")
									&& !categoryName.startsWith("Coordinates")
									&& !categoryName.startsWith("Redirects")
									&& !categoryName.startsWith("Unprintworthy")
									&& !categoryName.startsWith("Printworthy")
									&& !categoryName.startsWith("Days")
									&& !categoryName.startsWith("Months")
									&& !categoryName.startsWith("Dynamic lists")
									&& !categoryName.startsWith("Given names")
									&& !categoryName.startsWith("Surnames")
									&& !categoryName.startsWith("Year")
									&& !categoryName.startsWith("Languages")
									&& !categoryName.startsWith("ISO")
									&& !categoryName.startsWith("Types")
									&& !categoryName.startsWith("Concepts")
									&& !categoryName.startsWith("Names")
									&& !categoryName.startsWith("Harv and Sfn")
									&& !categoryName.startsWith("Engvar")
									&& !categoryName.contains(" Wikidata")
									&& !categoryName.contains(" ISO ")) {
								//System.out.println(categoryName);
								BigDecimal existingScore = scoreMap.get(categoryName);
								if (existingScore == null) {
									existingScore = new BigDecimal(0);
								}
								scoreMap.put(categoryName, existingScore.add(new BigDecimal(1)));
							}
						}
					}
				}
	    	}
	    }
    }
	
	
	//https://wikimedia.org/api/rest_v1/metrics/pageviews/per-article/en.wikipedia.org/all-access/all-agents/Hair/monthly/20190601/20190701
	public Map<String,BigDecimal> getWikipediaCategoriesForTopic(String topic) throws IOException, URISyntaxException {
		Map<String,BigDecimal> retval = getCategoriesCache(topic);
		if (retval != null) {
			return retval;
		}
		CategoriesForTopicPageHandler handler = new CategoriesForTopicPageHandler();
		for (String title : getWikipediaPagesForTopic(topic)) {
			title = title.replace(' ', '_');
	    	
			List<NameValuePair> params = HttpUtils.getInstance(parameterStore).getWikipediaCategoriesParameters();
			params.add(new BasicNameValuePair("titles", title));
			List<Header> headers = new ArrayList<Header>();
			headers.add(new BasicHeader("User-Agent", parameterStore.getProperty("wikipedia_apikey")));
			HttpUtils.getInstance(parameterStore).httpGetJsonPaginated(
					HttpUtils.getInstance(parameterStore).getWikipediaApi(), 
					params, 
					headers, 
					"clcontinue",
					handler
			);
		}
		putCategoryCache(topic, handler.getValues());
		return handler.getValues();
	
	}
	
	
    protected class PagesForTopicPageHandler implements IJsonResponseHandler {
    	List<String> retval = new ArrayList<String>();
    	
    	public PagesForTopicPageHandler() {
    		super();
    	}
    	
    	public List<String> getValues() {
    		return retval;
    	}
    	
    	@Override
		public void onPage(JsonNode page) {
    		if (page != null && page.has("query") && page.get("query").has("pages")) {
				JsonNode pagesNode = page.get("query").get("pages");
				if (pagesNode != null && pagesNode.isArray()) {
					for (final JsonNode pageNode : pagesNode) {
						if (!pageNode.has("missing") || !pageNode.get("missing").asBoolean()) {
							retval.add(pageNode.get("title").asText());
						}
					}
				}
			}   		
    	}
    }
	
    private List<String> getWikipediaPagesForTopic(String title) throws IOException, URISyntaxException {
    	title = title.replace(' ', '_');
    	PagesForTopicPageHandler handler = new PagesForTopicPageHandler();
		List<NameValuePair> params = HttpUtils.getInstance(parameterStore).getWikipediaPagesParameters();
		params.add(new BasicNameValuePair("titles", title));
		List<Header> headers = new ArrayList<Header>();
		headers.add(new BasicHeader("User-Agent", parameterStore.getProperty("wikipedia_apikey")));
		HttpUtils.getInstance(parameterStore).httpGetJsonPaginated(
				HttpUtils.getInstance(parameterStore).getWikipediaApi(), 
				params, 
				headers, 
				"clcontinue",
				handler
		);
    	
		return handler.getValues();
	}

    public String getWordCache(String token) {
    	String retval = wordCache.get(token);
    	return retval;
    }
    
    public boolean checkWordCache(String token) {
    	String retval = wordCache.get(token);
    	if (retval != null) {
    		mruWordList.remove(token);
    		mruWordList.push(token);
    		return true;
    	}
    	return false;
    }
    
    public void pruneWordCache() {
    	for (int i=WORD_CACHE_SIZE;i<mruWordList.size();i++) {
    		String token = (String)mruWordList.pollLast();
    		if (token != null) {
    			wordCache.remove(token);
    		}
    	}
    }
    
    public void addToWordCache(List<String> tokens) {
		List<String> queries = new ArrayList<String>();
		for (int i=0;i<tokens.size();i++) {
			String token = tokens.get(i);
			if (!checkWordCache(token) && !queries.contains(token)) {
				queries.add(token);
			}
			if (queries.size() == WORD_BATCH_SIZE) {
				try {
					Map<String, String> scores = getSearchResultsBatch(queries);
					for (String key : scores.keySet()) {
						mruWordList.push(key);
						wordCache.put(key, scores.get(key));
					}
				}
				catch (Exception e) {
					logger.error("wikipedia error", e);
				}
				queries.clear();
			}
		}
		if (queries.size() > 0) {
			try {
				Map<String, String> scores = getSearchResultsBatch(queries);
				for (String key : scores.keySet()) {
					mruWordList.push(key);
					wordCache.put(key, scores.get(key));
				}
			}
			catch (Exception e) {
				logger.error("wikipedia error", e);
			}
		}
		logger.debug("wordCache length: " + wordCache.size());
    }
    
    private Map<String, String> getSearchResultsBatch(List<String> queries) throws IOException, URISyntaxException {
    	ExtractsPageHandler handler = new ExtractsPageHandler();
		List<NameValuePair> params = HttpUtils.getInstance(parameterStore).getWikipediaExtractsParameters();
		params.add(new BasicNameValuePair("titles", String.join("|", queries)));
		List<Header> headers = new ArrayList<Header>();
		headers.add(new BasicHeader("User-Agent", parameterStore.getProperty("wikipedia_apikey")));
		HttpUtils.getInstance(parameterStore).httpGetJsonPaginated(
				HttpUtils.getInstance(parameterStore).getWikipediaApi(), 
				params, 
				headers, 
				"clcontinue",
				handler
		);
    	
		return handler.getValues();
	}
    
    protected class ExtractsPageHandler implements IJsonResponseHandler {
    	Map<String, String> retval = new HashMap<String, String>();
    	
    	public ExtractsPageHandler() {
    		super();
    	}
    	
    	public Map<String, String> getValues() {
    		return retval;
    	}
    	
    	
    	@Override
		public void onPage(JsonNode page) {
    		if (page != null && page.has("query") && page.get("query").has("pages")) {
				JsonNode pagesNode = page.get("query").get("pages");
				Iterator<Entry<String, JsonNode>> pagesIter = pagesNode.fields();
				while (pagesIter.hasNext()) {
					Entry<String, JsonNode> x = pagesIter.next();
					if (
							x.getValue().has("pageid") 
							&& x.getValue().has("extract") 
							&& !(
									x.getValue().get("extract").asText().endsWith("may refer to:") 
									|| x.getValue().get("extract").asText().endsWith("commonly refers to:")
							)
					) {
						retval.put(
								x.getValue().get("title").asText(),
								edu.stanford.nlp.util.StringUtils.toAscii(
										edu.stanford.nlp.util.StringUtils.normalize(
												x.getValue().get("extract").asText()
										)
								).trim()
						);
					}
				}
			}   		
    	}
    }    
    
}
