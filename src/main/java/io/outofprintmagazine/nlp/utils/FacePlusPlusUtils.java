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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.outofprintmagazine.util.IParameterStore;
/**
 * <p>You will need to set faceplusplus_apiKey and faceplusplus_secret in your IParameterStore.</p>
 * <p>Visit: <a href="https://www.faceplusplus.com/face-detection/">Faceplusplus</a> to sign up.</p> 
 * @author Ram Sadasiv
 */
public class FacePlusPlusUtils {
	
	private static final Logger logger = LogManager.getLogger(FacePlusPlusUtils.class);
	
	private Properties props = null;
	private ObjectMapper mapper = new ObjectMapper();

	
	private FacePlusPlusUtils(IParameterStore parameterStore) throws IOException {
		props = new Properties();
		props.setProperty("apiKey", parameterStore.getProperty("faceplusplus_apiKey"));
		props.setProperty("secret", parameterStore.getProperty("faceplusplus_secret"));
	}
	
	private static Map<IParameterStore, FacePlusPlusUtils> instances = new HashMap<IParameterStore, FacePlusPlusUtils>();
	
    public static FacePlusPlusUtils getInstance(IParameterStore parameterStore) throws IOException { 
        if (instances.get(parameterStore) == null) {
        	FacePlusPlusUtils instance = new FacePlusPlusUtils(parameterStore);
            instances.put(parameterStore, instance);
        }
        return instances.get(parameterStore); 
    }
    
    public JsonNode imageHasOneFace(String imageUrl) throws IOException, URISyntaxException {
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
            HttpPost http = new HttpPost("https://api-us.faceplusplus.com/facepp/v3/detect");
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("api_key", props.getProperty("apiKey")));
            nvps.add(new BasicNameValuePair("api_secret", props.getProperty("secret")));
            nvps.add(new BasicNameValuePair("image_url", imageUrl));
            http.setEntity(new UrlEncodedFormEntity(nvps));

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
                    	logger.error("imageUrl: " + imageUrl);
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }
            };
            responseBody = httpclient.execute(http, responseHandler);
            //logger.debug(responseBody);
        	JsonNode doc = mapper.readTree(responseBody);
        	if (doc.get("face_num").asInt() == 1) {
        		return doc.get("faces").get(0).get("face_rectangle");
        	}
        	else {
        		return null;
        	}

        } finally {
            httpclient.close();
        }

    }    
}
