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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.outofprintmagazine.util.ParameterStore;
import io.phrasefinder.Corpus;
import io.phrasefinder.Phrase;
import io.phrasefinder.Phrase.Token;
import io.phrasefinder.PhraseFinder;
import io.phrasefinder.SearchOptions;
import io.phrasefinder.SearchResult;

public class NGramUtils {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(NGramUtils.class);
	private static final int BATCH_SIZE = 100;
	private static final int CACHE_SIZE = 10000;
	
	public class NGramScore {
		public NGramPhraseScore totals = null;
		public NGramPhraseScore match = null;
	}
	
	public class NGramPhraseScore {
		public List<String> tokens = new ArrayList<String>();
		public double phraseScore = 0;
		public long matchCount = 0;
		public long volumeCount = 0;
		public int firstYear = 2018;
		public int lastYear = 0;
	}
	
	private SearchOptions wildcardOptions = new SearchOptions();
	private SearchOptions ngramOptions = new SearchOptions();
	private String apiKey = null;
	private Deque mruWordList = new LinkedList<String>();
	private Map<String, List<NGramPhraseScore>> wordCache = new HashMap<String, List<NGramPhraseScore>>();
	
	private NGramUtils(ParameterStore parameterStore) throws IOException {
		wildcardOptions.setMaxResults(100);
		ngramOptions.setMaxResults(10);
		//InputStream input = new FileInputStream("data/phrasefinder_credentials.properties");
        //Properties props = new Properties();
        //props.load(input);
		//Properties props = ParameterStore.getInstance().getProperties("data", "phrasefinder_credentials.properties");
        //this.apiKey = props.getProperty("phrasefinderApiKey");
		this.apiKey = parameterStore.getProperty("phrasefinder_ApiKey");

	}
	
	private static Map<ParameterStore, NGramUtils> instances = new HashMap<ParameterStore, NGramUtils>();
	
    public static NGramUtils getInstance(ParameterStore parameterStore) throws IOException { 
        if (instances.get(parameterStore) == null) {
        	NGramUtils instance = new NGramUtils(parameterStore);
            instances.put(parameterStore, instance);
        }
        return instances.get(parameterStore); 
    }
    
    public List<NGramPhraseScore> getWordCache(String token) {
    	List<NGramPhraseScore> retval = wordCache.get(token);
    	return retval;
    }
    
    public boolean checkWordCache(String token) {
    	List<NGramPhraseScore> retval = wordCache.get(token);
    	if (retval != null) {
    		mruWordList.remove(token);
    		mruWordList.push(token);
    		return true;
    	}
    	return false;
    }
    
    public void pruneWordCache() {
    	for (int i=NGramUtils.CACHE_SIZE;i<mruWordList.size();i++) {
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
			if (queries.size() == NGramUtils.BATCH_SIZE) {
				try {
					List<List<NGramPhraseScore>> scores = getSearchResultsBatch(queries);
					for (int j=0;j<queries.size() && j<scores.size();j++) {
						mruWordList.push(queries.get(j));
						wordCache.put(queries.get(j), scores.get(j));
					}
				}
				catch (Exception e) {
					logger.error("phrasefinder error", e);
				}
				queries.clear();
			}
		}
		if (queries.size() > 0) {
			try {
				List<List<NGramPhraseScore>> scores = getSearchResultsBatch(queries);
				for (int j=0;j<queries.size() && j<scores.size();j++) {
					mruWordList.push(queries.get(j));
					wordCache.put(queries.get(j), scores.get(j));
				}
			}
			catch (Exception e) {
				logger.error("phrasefinder error", e);
			}
		}
		logger.debug("wordCache length: " + wordCache.size());
    }
    
    
    private String getPhrasefinderWildcardQuery(List<String> tokens, int tokenIndex, int ngramSize) {
	    StringBuffer query = new StringBuffer();
	    for (int i=tokenIndex;i<tokenIndex+ngramSize&&i<tokens.size();i++) {
	    	query.append(tokens.get(i));
	    	query.append(" ");
	    }
	    if (query.toString().trim().length()==0) {
	    	return null;
	    }
	    return query.toString().trim()  + " ?";
    }
    
    private String getPhrasefinderMatchQuery(List<String> tokens, int tokenIndex, int ngramSize) {
	    StringBuffer query = new StringBuffer();
	    for (int i=tokenIndex;i<tokenIndex+ngramSize&&i<tokens.size();i++) {
	    	query.append(tokens.get(i));
	    	query.append(" ");
	    }
	    if (query.toString().trim().length()==0) {
	    	return null;
	    }
	    return query.toString().trim();
    }
    
    private NGramPhraseScore getTotalsFromSearchResult(SearchResult wildcardResult) {
    	NGramPhraseScore totals = new NGramPhraseScore();
		for (Phrase phrase : wildcardResult.getPhrases()) {
			totals.matchCount+=phrase.getMatchCount();
			totals.volumeCount+=phrase.getVolumeCount();
			if (totals.firstYear>phrase.getFirstYear()) {
				totals.firstYear = phrase.getFirstYear();
			}
			if (totals.lastYear<phrase.getLastYear()) {
				totals.lastYear = phrase.getLastYear();
			}
		}
		return totals;
    }
    
    private NGramPhraseScore getMatchFromSearchResult(SearchResult wildcardResult, List<String> tokens, int tokenIndex, int ngramSize) {
    	NGramPhraseScore score = null;
		for (Phrase phrase : wildcardResult.getPhrases()) {
			Token[] phraseTokens = phrase.getTokens();
			if (ngramSize < phraseTokens.length) {
				if (tokens.get(tokenIndex+ngramSize).equalsIgnoreCase(phraseTokens[ngramSize].getText())) {
					score = new NGramPhraseScore();
					score.phraseScore = phrase.getScore();
	    			score.matchCount = phrase.getMatchCount();
	    			score.volumeCount = phrase.getVolumeCount();
	    			score.firstYear = phrase.getFirstYear();
	    			score.lastYear = phrase.getLastYear();
	    			break;
				}
			}
		}
		return score;
    }
    
    public NGramScore nGramSearch(List<String> tokens, int tokenIndex, int ngramSize) throws IOException {
    	SearchResult wildcardResult = PhraseFinder.search(Corpus.AMERICAN_ENGLISH, getPhrasefinderWildcardQuery(tokens,tokenIndex,ngramSize-1), wildcardOptions);
    	NGramScore score = new NGramScore();
    	score.totals = getTotalsFromSearchResult(wildcardResult);
    	score.match = getMatchFromSearchResult(wildcardResult, tokens, tokenIndex, ngramSize);
    	if (score.match == null) {
    		SearchResult result = PhraseFinder.search(Corpus.AMERICAN_ENGLISH, getPhrasefinderMatchQuery(tokens,tokenIndex,ngramSize), ngramOptions);
    		score.match = getMatchFromSearchResult(result, tokens, tokenIndex, ngramSize);
    	}
    	return score;
    }
    
    public NGramScore nGramSearch(String phrase, int tokenIndex, int ngramSize) throws IOException {
    	List<String> tokens = Arrays.asList(phrase.split(" "));
    	/*
    	 * https://phrasefinder.io/documentation
    	 * It is important to note that the tokenization of a query must match the tokenization applied to the original text data in order to find matching phrases. 
    	 * Punctuation characters, for example, are always expected to be separate whitespace-delimited tokens, as in hello ., hello !, or hello \?.
    	 */
    	
    	
    	SearchResult wildcardResult = PhraseFinder.search(Corpus.AMERICAN_ENGLISH, getPhrasefinderWildcardQuery(tokens,tokenIndex,ngramSize-1), wildcardOptions);
    	NGramScore score = new NGramScore();
    	score.totals = getTotalsFromSearchResult(wildcardResult);
    	score.match = getMatchFromSearchResult(wildcardResult, tokens, tokenIndex, ngramSize);
    	if (score.match == null) {
    		SearchResult result = PhraseFinder.search(Corpus.AMERICAN_ENGLISH, getPhrasefinderMatchQuery(tokens,tokenIndex,ngramSize), ngramOptions);
    		score.match = getMatchFromSearchResult(result, tokens, tokenIndex, ngramSize);
    	}
    	return score;
    }

    
    public List<List<NGramPhraseScore>> getSearchResultsBatch(List<String> queries) throws IOException, URISyntaxException {
    	return processSearchResultsBatch(runSearchResultsBatch(queries));
    }
    
    
    public String runSearchResultsBatch(List<String> queries) throws IOException, URISyntaxException {
    	String responseBody = null;
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode json = mapper.createObjectNode();
		json.put("corpus", "eng-us");
		json.put("topk", 5);
		ArrayNode jsonQueries = json.putArray("batch");
		for (String query : queries) {
			ObjectNode js = mapper.createObjectNode();
			js.put("query", query);
			//logger.debug("query is: " + query);
			jsonQueries.add(js);
		}
		

        CloseableHttpClient httpclient = HttpClients.custom()
                .setServiceUnavailableRetryStrategy(
                		new ServiceUnavailableRetryStrategy() {
                			@Override
                			public boolean retryRequest(
                					final HttpResponse response, final int executionCount, final HttpContext context) {
                					int statusCode = response.getStatusLine().getStatusCode();
                					return (statusCode == 503 || statusCode == 500) && executionCount < 5;
                			}

                			@Override
                			public long getRetryInterval() {
                				return 5;
                			}
                		})
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();
        try {
            HttpPost http = new HttpPost("https://api.phrasefinder.io/batch");
            http.addHeader("X-API-Key", apiKey);
            HttpEntity stringEntity = new StringEntity(mapper.writeValueAsString(json),ContentType.APPLICATION_JSON);
            http.setEntity(stringEntity);
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                @Override
                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } 
                    else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }
            };
            responseBody = httpclient.execute(http, responseHandler);

        } finally {
            httpclient.close();
        }
        return responseBody;
    }
 
    public List<List<NGramPhraseScore>> processSearchResultsBatch(String responseBody) {
    	BufferedReader reader = new BufferedReader(new StringReader(responseBody));
    	List<List<NGramPhraseScore>> retval = new ArrayList<List<NGramPhraseScore>>();
    	try {
    		retval = processSearchResultsBatch(reader);
    	}
    	catch (Exception e) {
    		logger.error(e);
    	}
    	finally {
    		try {
    			reader.close();
    		}
        	catch (Exception e) {
        		logger.error(e);
        	}
    		reader = null;
    	}
    	return retval;
    }
    
    
    public List<List<NGramPhraseScore>> processSearchResultsBatch(BufferedReader reader) throws NumberFormatException, IOException {
        String line = null;
        List<List<NGramPhraseScore>> allResults = new ArrayList<List<NGramPhraseScore>>();
        while ((line = reader.readLine()) != null) {
        	//logger.debug(line);
        	if (line.startsWith("ERROR")) {
        		List<NGramPhraseScore> results = new ArrayList<NGramPhraseScore>();
        		allResults.add(results);
        	}
        	else if (line.startsWith("OK")) {
        		List<NGramPhraseScore> results = new ArrayList<NGramPhraseScore>();
        		allResults.add(results);
        		String[] tokens = line.split(" ");
        		int recordCount = Integer.parseInt(tokens[1]);
        		if (recordCount == 0) {
        			results.add(null);
        		}
        		for (int i=0; i<recordCount;i++) {
        			NGramPhraseScore result = processOneResult(reader);
        			results.add(result);
        			if (result == null) {
        				break;
        			}

        		}
        	}
        	
        }
        return allResults;
    }
    
	public NGramPhraseScore processOneResult(BufferedReader reader) throws NumberFormatException, IOException {
		String line = null;
		NGramPhraseScore phrase = null;
		if ((line = reader.readLine()) != null) {
			//logger.debug(line);
			String[] fields = line.split("\t");
			if (fields.length > 4) {
				String[] tokens = fields[0].split(" ");
				phrase = new NGramPhraseScore();

				for (int i = 0; i < tokens.length; i++) {
					int tokenLength = tokens[i].length();

					phrase.tokens.add(tokens[i].substring(0, tokenLength - 2));
				}
				phrase.matchCount = Long.parseLong(fields[1]);
				phrase.volumeCount = Integer.parseInt(fields[2]);
				phrase.firstYear = Integer.parseInt(fields[3]);
				phrase.lastYear = Integer.parseInt(fields[4]);
			}
		}
		return phrase;
	}
}
