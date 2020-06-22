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
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
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


public class PerfecttenseUtils {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(PerfecttenseUtils.class);
	
	private String apiKey = null;
	private String appKey = null;

	
	private PerfecttenseUtils(ParameterStore parameterStore) throws IOException {
		this.apiKey = parameterStore.getProperty("perfecttense_apikey");
		this.appKey = parameterStore.getProperty("perfecttense_appkey");
	}
	
	private static Map<ParameterStore, PerfecttenseUtils> instances = new HashMap<ParameterStore, PerfecttenseUtils>();
	
    public static PerfecttenseUtils getInstance(ParameterStore parameterStore) throws IOException { 
        if (instances.get(parameterStore) == null) {
        	PerfecttenseUtils instance = new PerfecttenseUtils(parameterStore);
            instances.put(parameterStore, instance);
        }
        return instances.get(parameterStore); 
    }
    
    public String correct(String text, List<String> nnp) throws UnsupportedCharsetException, ClientProtocolException, IOException {
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

            HttpPost http = new HttpPost("https://api.perfecttense.com/correct");
            http.addHeader("Authorization", apiKey);
            http.addHeader("AppAuthorization", appKey);
            http.addHeader("Content-Type", "application/json");
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode params = mapper.createObjectNode();
            ObjectNode options = params.putObject("options");
            options.put("dialect", "british");
            ArrayNode dictionary = options.putArray("dictionary");
            for (String word : nnp) {
            	dictionary.add(word);
            }
            ArrayNode responseType = params.putArray("responseType");
            responseType.add("offset");
 //           responseType.add("grammarScore");
 //           responseType.add("summary");
            params.put("text", text);
            StringEntity requestEntity = new StringEntity(mapper.writeValueAsString(params), ContentType.APPLICATION_JSON);
            http.setEntity(requestEntity);
            //'{"text": "This articl have some errors", "responseType": ["corrected", "grammarScore", "rulesApplied", "offset", "summary"]}'
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
}
