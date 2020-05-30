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
package io.outofprintmagazine.nlp;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import io.outofprintmagazine.util.ParameterStore;

public class WikipediaUtils {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(WikipediaUtils.class);
	private static final int CACHE_SIZE = 5000;
	
	
//	private Deque mruPageList = new LinkedList<String>();
//	private Map<String, String> pageCache = new HashMap<String, String>();
//	private List<String> failedPages = new ArrayList<String>();
	private Deque mruPageviewList = new LinkedList<String>();
	private Map<String, BigDecimal> pageviewCache = new HashMap<String, BigDecimal>();
	private Deque mruCategoriesList = new LinkedList<String>();
	private Map<String, Map<String,BigDecimal>> categoriesCache = new HashMap<String, Map<String,BigDecimal>>();

	
/*    public String getPageCache(String topic) {
    	String retval = pageCache.get(topic);
    	if (retval != null) {
    		mruPageList.remove(topic);
    		mruPageList.push(topic);
    	}
    	return retval;
    }
    
	public void putPageCache(String topic, String score) {
		mruPageviewList.push(topic);
		pageCache.put(topic, score);
    	for (int i=WikipediaUtils.CACHE_SIZE;i<mruPageviewList.size();i++) {
    		String token = (String)mruPageList.pollLast();
    		if (token != null) {
    			pageCache.remove(token);
    		}
    	}
    }
	
	public List<String> getFailedPages() {
		return failedPages;
	}*/
	
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
	
	private StanfordCoreNLP wikiPipeline = null;
	
	public static Properties getWikiProps() {
		Properties props = new Properties();
	    props.put("customAnnotatorClass.oop_nouns",
	            "io.outofprintmagazine.nlp.pipeline.annotators.NounsAnnotator");
		props.setProperty("annotators","tokenize,ssplit,pos,lemma,oop_nouns");
		return props;
	}

//	public class TopicPage {
//		public String title;
//		public String page;
//		
//		public TopicPage() {
//			super();
//		}
//	}
	
	private WikipediaUtils(ParameterStore parameterStore) throws IOException {
		super();

	}
	
	private static Map<ParameterStore, WikipediaUtils> instances = new HashMap<ParameterStore, WikipediaUtils>();
	
    public static WikipediaUtils getInstance(ParameterStore parameterStore) throws IOException { 
        if (instances.get(parameterStore) == null) {
        	WikipediaUtils instance = new WikipediaUtils(parameterStore);
            instances.put(parameterStore, instance);
        }
        return instances.get(parameterStore); 
    }
    
    public StanfordCoreNLP getWikiPipeline() {
    	if (wikiPipeline == null) {
    		wikiPipeline = new StanfordCoreNLP(WikipediaUtils.getWikiProps());
    	}
    	return wikiPipeline;
    }
    
	public CoreDocument annotate(String text) {
		CoreDocument document = new CoreDocument(text);
		getWikiPipeline().annotate(document);
		return document;
	}
	
/*	public Class getTopicsAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPTopicsAnnotation.class;
	}
	
	public Class getNounsAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounsAnnotation.class;
	}*/
    
	public BigDecimal getWikipediaPageviewsForTopic(String topic) throws MalformedURLException, UnsupportedEncodingException, IOException {
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
							+ URLEncoder.encode(title,"utf-8") 
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
	//https://wikimedia.org/api/rest_v1/metrics/pageviews/per-article/en.wikipedia.org/all-access/all-agents/Hair/monthly/20190601/20190701

	public Map<String,BigDecimal> getWikipediaCategoriesForTopic(String topic) throws MalformedURLException, UnsupportedEncodingException, IOException {
		Map<String,BigDecimal> retval = getCategoriesCache(topic);
		if (retval != null) {
			return retval;
		}
		ObjectMapper mapper = new ObjectMapper();
    	Map<String,BigDecimal> scoreMap = new HashMap<String,BigDecimal>();
		for (String title : getWikipediaPagesForTopic(topic)) {
			title = title.replace(' ', '_');
			String clcontinue = "init";
			while (clcontinue != null) {
				JsonNode categoryRootNode = null;
		
				if (clcontinue.equals("init")) {
					categoryRootNode = mapper.readValue(new URL("https://en.wikipedia.org/w/api.php?format=json&action=query&prop=categories&utf8=1&titles="+(URLEncoder.encode(title, "UTF-8"))), JsonNode.class);
				}
				else {
					categoryRootNode = mapper.readValue(new URL("https://en.wikipedia.org/w/api.php?format=json&action=query&prop=categories&utf8=1&titles="+(URLEncoder.encode(title, "UTF-8")+"&clcontinue="+clcontinue)), JsonNode.class);
				}
				if (categoryRootNode.get("continue") != null) {
					clcontinue = categoryRootNode.get("continue").get("clcontinue").asText();
				}
				else {
					clcontinue = null;
				}
				//System.out.println(categoryRootNode);
				JsonNode pageCategoriesNode = categoryRootNode.get("query").get("pages");
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
		putCategoryCache(topic, scoreMap);
		return scoreMap;
	}
	
    public List<String> getWikipediaPagesForTopic(String topic) throws IOException {
    	List<String> topicPages = new ArrayList<String>();
    	ObjectMapper objectMapper = new ObjectMapper();
		topic = topic.replace(' ', '_');
		JsonNode rootNode = objectMapper.readValue(new URL("https://en.wikipedia.org/w/api.php?action=query&maxlag=1&redirects&format=json&formatversion=2&titles="+(URLEncoder.encode(topic, "UTF-8"))), JsonNode.class);
		//TODO - pagination?
		if (rootNode.get("query") != null && rootNode.get("query").get("pages") != null) {
			JsonNode pagesNode = rootNode.get("query").get("pages");
			if (pagesNode != null && pagesNode.isArray()) {
				for (final JsonNode pageNode : pagesNode) {
					if (pageNode.get("missing") == null || !pageNode.get("missing").asBoolean()) {
						topicPages.add(pageNode.get("title").asText());
					}
				}
			}
		}
		return topicPages;
    }
	
    public List<String> getWikipediaPagesForList(String topic) throws IOException {
    	List<String> topicPages = new ArrayList<String>();
    	ObjectMapper objectMapper = new ObjectMapper();
		topic = topic.replace(' ', '_');
		JsonNode rootNode = objectMapper.readValue(new URL("https://en.wikipedia.org/w/api.php?action=parse&format=json&prop=links&page="+(URLEncoder.encode(topic, "UTF-8"))), JsonNode.class);
		//TODO - pagination?
		if (rootNode.get("parse") != null && rootNode.get("parse").get("links") != null) {
			JsonNode pagesNode = rootNode.get("parse").get("links");
			if (pagesNode != null && pagesNode.isArray()) {
				for (final JsonNode pageNode : pagesNode) {
					if (pageNode.get("*") != null ) {
						topicPages.add(pageNode.get("*").asText().replace(' ', '_'));
					}
				}
			}
		}
		return topicPages;
    }
    
    
    public String getWikipediaPageText(String topic) {
		topic = topic.replace(' ', '_');
		try {
			StringWriter buf = new StringWriter();
			Document homepage = Jsoup.connect("https://en.wikipedia.org/wiki/" + topic).get();
			Elements authorParas = homepage.select("#mw-content-text > div > p");
			for (Element para : authorParas) {
				buf.write(para.wholeText());
				buf.write('\n');
				buf.write('\n');
			}

			return buf.toString();
		}
		catch (IOException e) {
			return null;
		}
    }
    
/*    public Map<String,BigDecimal> getNounsForTopicPages(List<String> topicPages) {
    	Map<String,BigDecimal> retval = new HashMap<String,BigDecimal>();
    	for (String pageText : topicPages) {
    		try {
    			Map<String, BigDecimal> topicPageScores = (Map<String, BigDecimal>)annotate(pageText).annotation().get(getNounsAnnotationClass());
    			for(String topic : topicPageScores.keySet()) {
    				BigDecimal existingScore = new BigDecimal(0);
    				if (retval.get(topic) != null) {
    					existingScore = retval.get(topic);
    				}
    				retval.put(topic, existingScore.add(topicPageScores.get(topic)));
    			}
    		}
    		catch (Exception e) {
    			logger.error(e);
    		}
    	}
    	return retval;
    }*/
 
    
//	@SuppressWarnings("unchecked")
//	@Override
//	public void annotate(Annotation annotation) {
//		CoreDocument document = new CoreDocument(annotation);
//		Map<String,BigDecimal> topics = scoreNouns(document);
//		String topic = "topic";
//		List<WikipediaUtils.TopicPage> topicPages;
//		try {
//			topicPages = WikipediaUtils.getInstance().getWikipediaPagesForTopic(topic);
//			Map<String,BigDecimal> topicPageTopics = WikipediaUtils.getInstance().getTopicsForTopicPages(topicPages);
//			Map<String, BigDecimal> topicIdf = WikipediaUtils.getInstance().getTopicTopicPageIdf(topics, topicPageTopics);
//			Map<String, Map<String,BigDecimal>> topicPageTf = WikipediaUtils.getInstance().getTopicTopicPageTf(topics, topicPageTopics);
//			Map<String, BigDecimal> scores = WikipediaUtils.getInstance().getTopicPageTfIdf(topics, topicPageTopics, topicIdf, topicPageTf);
//			//document.annotation().set(getAnnotationClass(), scores);
//			//TODO - THIS NEEDS A LOT MORE WORK
//		} 
//		catch (IOException e) {
//			logger.error(e);
//		}
//	}    
//    public Map<String, BigDecimal> getTopicTopicPageIdf(Map<String,BigDecimal> topics, Map<String,BigDecimal> topicPageTopics) {
//		Map<String, BigDecimal> topicIdf = new HashMap<String,BigDecimal>();
//		for (String topic : topics.keySet()) {
//			for (String topicPageTopic : topicPageTopics.keySet()) {
//				int idf = 0;
//				if (topicPageTopic.equals(topic)) {
//					idf++;
//					continue;
//				}
//				topicIdf.put(topic, new BigDecimal(idf));
//			}
//		}
//		return topicIdf;
//    }
//    
//    public Map<String, Map<String,BigDecimal>> getTopicTopicPageTf(Map<String,BigDecimal> topics, Map<String,BigDecimal> topicPageTopics) {
//		Map<String, Map<String,BigDecimal>> topicPageTf = new HashMap<String,Map<String,BigDecimal>>();
//		for (String topic : topics.keySet()) {
//			Map<String,BigDecimal> topicPageScore = topicPageTf.get(topic);
//			if (topicPageScore == null) {
//				topicPageScore = new HashMap<String,BigDecimal>();
//				topicPageTf.put(topic, topicPageScore);
//			}
//			for (String topicPageTopic : topicPageTopics.keySet()) {
//				if (topicPageScore.get(topicPageTopic) == null) {
//					topicPageScore.put(topicPageTopic, new BigDecimal(0.0));
//				}
//				if (topicPageTopic.equals(topic)) {
//					BigDecimal existingScore = topicPageScore.get(topicPageTopic);
//					BigDecimal newScore = topicPageTopics.get(topicPageTopic);
//					topicPageScore.put(topicPageTopic, existingScore.add(newScore));						
//					continue;
//				}
//			}
//		}
//		return topicPageTf;
//    }
//    
//    
///*
//Suppose that we have term count tables of a corpus consisting of only two documents, as listed on the right.
//
//Document 2
//Term	Term Count
//this	1
//is		1
//another	2
//example	3
//
//Document 1
//Term	Term Count
//this	1
//is		1
//a		2
//sample	1
//
//
//The calculation of tf–idf for the term "this" is performed as follows:
//
//In its raw frequency form, tf is just the frequency of the "this" for each document. 
//In each document, the word "this" appears once; but as the document 2 has more words, its relative frequency is smaller.
//
//An idf is constant per corpus, and accounts for the ratio of documents that include the word "this". 
//In this case, we have a corpus of two documents and all of them include the word "this".
//
//So tf–idf is zero for the word "this", which implies that the word is not very informative as it appears in all documents.
//The word "example" is more interesting - it occurs three times, but only in the second document.
//
//*/
//	//calculate similarity
//    public Map<String,BigDecimal> getTopicPageTfIdf(
//    		Map<String,BigDecimal> topics, 
//    		Map<String,BigDecimal> topicPageTopics,
//    		Map<String, BigDecimal> topicIdf,
//    		Map<String, Map<String,BigDecimal>> topicPageTf) {
//		Map<String,BigDecimal> pageSimilarity = new HashMap<String,BigDecimal>();
//		for (String pageName : topicPageTopics.keySet()) {		
//			for (String topic : topics.keySet()) {
//				BigDecimal mainPageTopicScore = topics.get(topic);
//				BigDecimal topicIdfScore = topicIdf.get(topic);
//				if (topicPageTf.get(topic) != null) {
//					BigDecimal topicPageTfScore = topicPageTf.get(topic).get(pageName);
//					BigDecimal answer = mainPageTopicScore.multiply(
//							topicPageTfScore.multiply(
//									new BigDecimal(
//											Math.log(
//											topicPageTopics.size()/(
//													1+Math.abs(
//															topicIdfScore.intValue()
//															)
//													)
//											)
//									)
//							)
//					);
//					BigDecimal existingValue = pageSimilarity.get(pageName);
//					if (existingValue == null) {
//						existingValue = new BigDecimal(0);
//					}
//					existingValue = existingValue.add(answer);
//					pageSimilarity.put(pageName, existingValue);
//				}
//			}
//		}
//		return pageSimilarity;
//    }
}
