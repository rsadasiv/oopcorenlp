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

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.outofprintmagazine.util.IParameterStore;

/**
 * <p>You will need to set perfecttense_apikey and perfecttense_appkey in your IParameterStore.</p>
 * <p>Visit: <a href="https://www.perfecttense.com/">Perfecttense</a> to sign up.</p> 
 * @author Ram Sadasiv
 */
public class PerfecttenseUtils {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(PerfecttenseUtils.class);
	private IParameterStore parameterStore = null;

	private PerfecttenseUtils(IParameterStore parameterStore) throws IOException {
		this.parameterStore = parameterStore;
	}
	
	private static Map<IParameterStore, PerfecttenseUtils> instances = new HashMap<IParameterStore, PerfecttenseUtils>();
	
    public static PerfecttenseUtils getInstance(IParameterStore parameterStore) throws IOException { 
        if (instances.get(parameterStore) == null) {
        	PerfecttenseUtils instance = new PerfecttenseUtils(parameterStore);
            instances.put(parameterStore, instance);
        }
        return instances.get(parameterStore); 
    }
    
    public JsonNode correctJson(String text, List<String> nnp) throws UnsupportedCharsetException, IOException {
        HttpPost http = new HttpPost("https://api.perfecttense.com/correct");
        http.addHeader("Authorization", parameterStore.getProperty("perfecttense_apikey"));
        http.addHeader("AppAuthorization", parameterStore.getProperty("perfecttense_appkey"));
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
        return HttpUtils.getInstance(parameterStore).httpPostJson(http);
    }
    
}
