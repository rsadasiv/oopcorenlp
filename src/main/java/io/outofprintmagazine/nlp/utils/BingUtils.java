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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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

import io.outofprintmagazine.util.ParameterStore;
/**
 * <p>You will need to set azure_apiKey in your ParameterStore.</p>
 * <p>Visit: <a href="https://azure.microsoft.com/en-us/services/cognitive-services/bing-image-search-api/">Azure</a> to sign up.</p> 
 * @author Ram Sadasiv
 */
public class BingUtils {
	
	private static final Logger logger = LogManager.getLogger(BingUtils.class);
	
	private Properties props = new Properties();
	private ObjectMapper mapper = new ObjectMapper();
	private static Map<ParameterStore, BingUtils> instances = new HashMap<ParameterStore, BingUtils>();
	
	private BingUtils(ParameterStore parameterStore) throws IOException {
		super();
		props = new Properties();
		props.setProperty("apiKey", parameterStore.getProperty("azure_apiKey"));
	}
	    
    public static BingUtils getInstance(ParameterStore parameterStore) throws IOException { 
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
        	String queryString = "https://api.cognitive.microsoft.com/bing/v7.0/images/search?q="+ URLEncoder.encode(text, "UTF-8")+"&imageType=Photo";
        	if (imageContent != null) {
        		queryString += ("&imageContent="+imageContent);
        	}
            HttpGet http = new HttpGet(queryString);
            http.addHeader("Ocp-Apim-Subscription-Key", props.getProperty("apiKey"));

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
                    	logger.error("imageUrl: " + text);
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }
            };
            responseBody = httpclient.execute(http, responseHandler);
            //logger.debug(responseBody);
        	JsonNode doc = mapper.readTree(responseBody);
        	if (doc.get("value").size() > 0) {
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

        } finally {
            httpclient.close();
        }

    }
    
    public List<String> getImageTags(String text, String insightsToken) throws IOException, URISyntaxException {
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
                .build();
        try {
            HttpGet http = new HttpGet("https://api.cognitive.microsoft.com/bing/v7.0/images/details?q="+ URLEncoder.encode(text, "UTF-8")+"&insightsToken="+insightsToken+"&modules=All&mkt=en-us");
            http.addHeader("Ocp-Apim-Subscription-Key", props.getProperty("apiKey"));

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
                    	logger.error("imageUrl: " + text);
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }
            };
            responseBody = httpclient.execute(http, responseHandler);
            logger.debug(responseBody);
        	JsonNode doc = mapper.readTree(responseBody);
        	if (doc.get("imageTags").get("value").size() > 0) {
        		List<String> retval = new ArrayList<String>();
            	for (JsonNode val : doc.get("imageTags").get("value")) {
            		Iterator<String> fieldNameIter = val.fieldNames();
            		while (fieldNameIter.hasNext()) {
            			String fieldName = fieldNameIter.next();
            			logger.debug(fieldName + ": " + val.get(fieldName).asText());
            			
            		}
            		
            	}
            	return retval;
        	}
        	else {
        		return new ArrayList<String>();
        	}

        } finally {
            httpclient.close();
        }

    }
}
