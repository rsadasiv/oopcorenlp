package io.outofprintmagazine.nlp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.stanford.nlp.io.IOUtils;


public class PerfecttenseUtils {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(PerfecttenseUtils.class);
	
	private String apiKey = null;
	private String appKey = null;

	
	private PerfecttenseUtils() throws IOException {
		InputStream input = new FileInputStream("data/perfecttense.properties");
        Properties props = new Properties();
        props.load(input);
        this.apiKey = props.getProperty("apikey");
        this.appKey = props.getProperty("appkey");
        input.close();

	}
	
	private static PerfecttenseUtils single_instance = null; 

    public static PerfecttenseUtils getInstance() throws IOException { 
        if (single_instance == null) 
            single_instance = new PerfecttenseUtils(); 
  
        return single_instance; 
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

    public static void main(String[] argv) throws IOException {
    	String text = IOUtils.slurpFile(
				"C:\\Users\\rsada\\eclipse-workspace\\oopcorenlp_web\\WebContent\\Corpora\\Published\\Text\\0c06370b-8401-4aac-98be-ed61c184a264.txt");
    	List<String> nnp = new ArrayList<String>();
    	String response = PerfecttenseUtils.getInstance().correct(text, nnp);
		ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
		mapper.configure(com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);

        JsonNode document = mapper.readTree(response);
		System.out.println(mapper.writeValueAsString(document));
        
    }
    
}
