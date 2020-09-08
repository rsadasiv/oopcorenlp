package io.outofprintmagazine.nlp.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.outofprintmagazine.util.IParameterStore;

public class HttpUtils {

	public HttpUtils() {
		super();
	}
	
	private static final Logger logger = LogManager.getLogger(HttpUtils.class);
	
	private Logger getLogger() {
		return logger;
	}
	
	private ObjectMapper mapper = null;
	private HttpClientConnectionManager connManager = null;
	
	private HttpUtils(IParameterStore parameterStore) throws IOException {
		this();
		mapper = new ObjectMapper();
		connManager = new PoolingHttpClientConnectionManager();
		//((PoolingHttpClientConnectionManager)connManager).setMaxTotal(5);
		//((PoolingHttpClientConnectionManager)connManager).setDefaultMaxPerRoute(4);
		//((PoolingHttpClientConnectionManager)connManager).setMaxPerRoute(
		//		new HttpRoute(
		//				new HttpHost("en.wikipedia.org", -1)
		//		),
		//		5
		//);
	}
	
	private static Map<IParameterStore, HttpUtils> instances = new HashMap<IParameterStore, HttpUtils>();
	
    public static HttpUtils getInstance(IParameterStore parameterStore) throws IOException { 
        if (instances.get(parameterStore) == null) {
        	HttpUtils instance = new HttpUtils(parameterStore);
            instances.put(parameterStore, instance);
        }
        return instances.get(parameterStore); 
    }
    
    class JsonResponseHandler implements ResponseHandler<JsonNode> {
    	
		@Override
		public JsonNode handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
			int status = response.getStatusLine().getStatusCode();
			JsonNode retval = null;
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                retval = mapper.readTree(EntityUtils.toString(entity, StandardCharsets.UTF_8.name()));
                EntityUtils.consume(entity);
            } 
            else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            return retval;
		}
	}
    
    class StringResponseHandler implements ResponseHandler<String> {
    	
		@Override
		public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
			int status = response.getStatusLine().getStatusCode();
			String retval = null;
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                retval = EntityUtils.toString(entity, StandardCharsets.UTF_8.name());
                EntityUtils.consume(entity);
            } 
            else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            return retval;
		}
	}
    
	protected CloseableHttpClient getHttpClient() {
		int timeout = 5;
		return HttpClients.custom()
				.setConnectionManager(connManager)
                .setServiceUnavailableRetryStrategy(
                		new ServiceUnavailableRetryStrategy() {
                			@Override
							public boolean retryRequest(
                					final HttpResponse response, final int executionCount, final HttpContext context) {
                					int statusCode = response.getStatusLine().getStatusCode();
                					return (statusCode == 503 || statusCode == 500 || statusCode == 429) && executionCount < 5;
                			}

                			@Override
							public long getRetryInterval() {
                				return new Long(5).longValue();
                			}
                		})
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setDefaultRequestConfig(
                		RequestConfig.custom()
                		.setConnectTimeout(timeout * 1000)
                		.setConnectionRequestTimeout(timeout * 1000)
                		.setSocketTimeout(timeout * 1000)
                		.setCookieSpec(CookieSpecs.STANDARD)
                		.build())
                .build();
	}

	public URI buildUri(URI uri, List<NameValuePair> nvps) throws URISyntaxException {
		URIBuilder builder = new URIBuilder(uri);
		builder.setParameters(nvps);
		return builder.build();
	}
	
	public JsonNode httpGetJson(URI url, List<Header> headers) throws IOException {
		return httpGetJson(url, headers.toArray(new Header[0]));
	}
	
	public JsonNode httpGetJson(URI url, Header[] headers) throws IOException {
		//getLogger().info(url);

    	HttpGet http = new HttpGet(url);
    	http.setHeaders(headers);
        return getHttpClient().execute(
        		http, 
        		new JsonResponseHandler()
        );
	}
	
	public void httpGetJsonCB(URI url, List<Header> headers, IJsonResponseHandler handler) throws IOException {
		handler.onPage(httpGetJson(url, headers.toArray(new Header[0])));
	}
	
	public void httpGetJsonCB(URI url, Header[] headers, IJsonResponseHandler handler) throws IOException {
		handler.onPage(httpGetJson(url, headers));
	}
	
	public String httpGetString(URI url, List<Header>headers) throws IOException {
		return httpGetString(url, headers.toArray(new Header[0]));
	}
		
	public String httpGetString(URI url, Header[] headers) throws IOException {
		//getLogger().info(url);

    	HttpGet http = new HttpGet(url);
    	http.setHeaders(headers);
        return getHttpClient().execute(
        		http, 
        		new StringResponseHandler()
        );
	}
	
	public JsonNode httpPostJson(HttpPost http) throws IOException {
        return getHttpClient().execute(
        		http, 
        		new JsonResponseHandler()
        );
	}
	
	public String httpPostString(HttpPost http) throws IOException {
        return getHttpClient().execute(
        		http, 
        		new StringResponseHandler()
        );
	}
	
	public URI getWikipediaApi() throws URISyntaxException {
		return new URI("https", "en.wikipedia.org", "/w/api.php", null);
	}
	
	public List<NameValuePair> getWikimediaImageParameters() {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("utf8", "1"));
		nvps.add(new BasicNameValuePair("format", "json"));
		nvps.add(new BasicNameValuePair("action", "query"));
		nvps.add(new BasicNameValuePair("maxlag", "1"));		
		nvps.add(new BasicNameValuePair("generator", "images"));
		nvps.add(new BasicNameValuePair("prop", "info"));
		return nvps;
	}
	
	public List<NameValuePair> getWikimediaImageInfoParameters() {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("utf8", "1"));		
		nvps.add(new BasicNameValuePair("format", "json"));
		nvps.add(new BasicNameValuePair("action", "query"));
		nvps.add(new BasicNameValuePair("maxlag", "1"));		
		nvps.add(new BasicNameValuePair("prop", "imageinfo"));
		nvps.add(new BasicNameValuePair("iiprop", "timestamp|user|userid|comment|canonicaltitle|url|size|dimensions|sha1|mime|thumbmime|mediatype|bitdepth"));
		return nvps;
	}
	
	public List<NameValuePair> getWikionaryParameters() {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("utf8", "1"));
		nvps.add(new BasicNameValuePair("format", "json"));
		nvps.add(new BasicNameValuePair("action", "query"));
		nvps.add(new BasicNameValuePair("maxlag", "1"));
		return nvps;
	}
	
	public List<NameValuePair> getWikipediaCategoriesParameters() {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("utf8", "1"));		
		nvps.add(new BasicNameValuePair("format", "json"));
		nvps.add(new BasicNameValuePair("action", "query"));
		nvps.add(new BasicNameValuePair("maxlag", "1"));		
		nvps.add(new BasicNameValuePair("prop", "categories"));
		return nvps;
	}
	
	public List<NameValuePair> getWikipediaPagesParameters() {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("utf8", "1"));		
		nvps.add(new BasicNameValuePair("format", "json"));
		nvps.add(new BasicNameValuePair("action", "query"));
		nvps.add(new BasicNameValuePair("maxlag", "1"));
		nvps.add(new BasicNameValuePair("formatversion", "2"));
		nvps.add(new BasicNameValuePair("redirects", null));	
		return nvps;
	}
	
	public List<NameValuePair> getWikipediaExtractsParameters() {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("utf8", "1"));		
		nvps.add(new BasicNameValuePair("format", "json"));
		nvps.add(new BasicNameValuePair("action", "query"));
		nvps.add(new BasicNameValuePair("maxlag", "1"));
		nvps.add(new BasicNameValuePair("prop", "extracts"));
		nvps.add(new BasicNameValuePair("exlimit", "20"));
		nvps.add(new BasicNameValuePair("exsentences", "1"));
		nvps.add(new BasicNameValuePair("exintro", null));
		nvps.add(new BasicNameValuePair("explaintext", null));
		return nvps;
	}
	
	public void httpGetJsonPaginated(
			URI baseUri, 
			List<NameValuePair> parameters, 
			List<Header> headers, 
			String gimcontinueName, 
			IJsonResponseHandler handler 
		) throws IOException, URISyntaxException {
		String gimcontinue = "init";
		while (gimcontinue != null) {
			JsonNode rootNode = null;
			if (gimcontinue.equals("init")) {
				rootNode = httpGetJson(
								buildUri(
										baseUri, 
										parameters
								),
								headers
						);
			}
			else {
				List<NameValuePair> mynvps = new ArrayList<NameValuePair>();
				mynvps.addAll(parameters);
				mynvps.add(new BasicNameValuePair(gimcontinueName, gimcontinue));
				rootNode = httpGetJson(
								buildUri(
										baseUri, 
										mynvps
								),
								headers
						);
			}
			if (rootNode.has("continue")) {
				gimcontinue = rootNode.get("continue").get(gimcontinueName).asText();
				gimcontinue = gimcontinue.replace(' ', '_');
			}
			else {
				gimcontinue = null;
			}
			if (rootNode != null && rootNode.has("errors")) {
				for (JsonNode err : ((ArrayNode)rootNode.get("errors"))) {
					if (err.get("code").asText().equals("ratelimited")) {
						try {
							Thread.sleep(1000);
						}
						catch (Exception e) {
							getLogger().error(e);
						}
					}
				}
			}
			else {
				handler.onPage(rootNode);
			}
		}		
	}
}
