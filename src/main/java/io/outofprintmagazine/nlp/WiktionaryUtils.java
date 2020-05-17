package io.outofprintmagazine.nlp;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class WiktionaryUtils {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(WiktionaryUtils.class);
	private static final int BATCH_SIZE = 50;
	private static final int CACHE_SIZE = 10000;
	
	private String apiKey = null;
	private Deque mruWordList = new LinkedList<String>();
	private Map<String, String> wordCache = new HashMap<String, String>();

	
	private WiktionaryUtils() throws IOException {
		//InputStream input = new FileInputStream("data/credentials.properties");
        //Properties props = new Properties();
        //props.load(input);
        //this.apiKey = props.getProperty("wikionaryApiKey");
        //input.close();
		this.apiKey = "OOPCoreNlp/0.9 (rsadasiv@gmail.com) httpclient/4.5.6";
	}
	
	private static WiktionaryUtils single_instance = null; 

    public static WiktionaryUtils getInstance() throws IOException { 
        if (single_instance == null) 
            single_instance = new WiktionaryUtils(); 
  
        return single_instance; 
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
    	for (int i=WiktionaryUtils.CACHE_SIZE;i<mruWordList.size();i++) {
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
			if (queries.size() == WiktionaryUtils.BATCH_SIZE) {
				try {
					Map<String, String> scores = getSearchResultsBatch(queries);
					for (String key : scores.keySet()) {
						mruWordList.push(key);
						wordCache.put(key, scores.get(key));
					}
				}
				catch (Exception e) {
					logger.error("wiktionary error", e);
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
				logger.error("wiktionary error", e);
			}
		}
		logger.debug("wordCache length: " + wordCache.size());
    }
        
    public Map<String, String> getSearchResultsBatch(List<String> queries) throws IOException, URISyntaxException {
    	return processSearchResultsBatch(runSearchResultsBatch(queries));
    }
    
    public String runSearchResultsBatch(List<String> queries) throws IOException, URISyntaxException {
    	String responseBody = null;
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

            HttpGet http = new HttpGet("https://en.wiktionary.org/w/api.php?format=json&maxlag=1&action=query&titles="+URLEncoder.encode(String.join("|", queries), "UTF-8"));
            http.addHeader("User-Agent", apiKey);
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                @Override
                public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
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
 
    public Map<String, String> processSearchResultsBatch(String responseBody) {
    	Map<String, String> retval = new HashMap<String, String>();
    	try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode doc = mapper.readTree(responseBody);
			if (doc.get("query") != null && doc.get("query").get("pages") != null) {
				JsonNode pages = doc.get("query").get("pages");
				Iterator<String> resultIter = pages.fieldNames();
				while (resultIter.hasNext()) {
					String resultName = resultIter.next();
					JsonNode result = pages.get(resultName);
					if (result.has("pageid")) {
						retval.put(result.get("title").asText(),result.get("pageid").asText() );
					}
		    	}
			}
    	}
    	catch (IOException e) {
    		logger.error(e);
    	}
    	return retval;
    }    
}
