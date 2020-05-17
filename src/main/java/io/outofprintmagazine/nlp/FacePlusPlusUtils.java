package io.outofprintmagazine.nlp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
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

public class FacePlusPlusUtils {
	
	private static final Logger logger = LogManager.getLogger(FacePlusPlusUtils.class);
	
	private Properties props = null;
	private ObjectMapper mapper = null;
	
	private FacePlusPlusUtils() throws IOException {
		InputStream input = new FileInputStream("data/faceplusplus_api_key.txt");
        props = new Properties();
        props.load(input);
		mapper = new ObjectMapper();
	}
	
	private static FacePlusPlusUtils single_instance = null; 

    public static FacePlusPlusUtils getInstance() throws IOException { 
        if (single_instance == null) 
            single_instance = new FacePlusPlusUtils(); 
  
        return single_instance; 
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

    public static void main(String[] argv) throws IOException, URISyntaxException {
    	System.out.println(FacePlusPlusUtils.getInstance().imageHasOneFace("https://farm6.static.flickr.com/5211/5516604741_b88c12e6d8_o.jpg"));
    	System.out.println(FacePlusPlusUtils.getInstance().imageHasOneFace("https://farm2.static.flickr.com/1145/1079478724_49ebe7867d_o.jpg"));
    	
    }
    
}
