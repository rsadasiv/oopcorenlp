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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.stanford.nlp.io.IOUtils;
import io.outofprintmagazine.util.ParameterStore;

public class ResourceUtils {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(ResourceUtils.class);
	
	private static Map<ParameterStore, ResourceUtils> instances = new HashMap<ParameterStore, ResourceUtils>();
	
    public static ResourceUtils getInstance(ParameterStore parameterStore) throws IOException { 
        if (instances.get(parameterStore) == null) {
        	ResourceUtils instance = new ResourceUtils(parameterStore);
            instances.put(parameterStore, instance);
        }
        return instances.get(parameterStore); 
    }
    
	private HashMap<String, HashMap<String, String>> dictionaries = new HashMap<String, HashMap<String, String>>();
	private HashMap<String, List<String>> lists = new HashMap<String, List<String>>();
	
	private ResourceUtils(ParameterStore parameterStore) {
		super();
	}
	
	public HashMap<String, HashMap<String, String>> getDictionaries() {
		return dictionaries;
	}

	public HashMap<String, String> getDictionary(String name) throws IOException {
		HashMap<String, String> retval = dictionaries.get(name);
		if (retval == null) {
			setDictionary(name);
			retval = dictionaries.get(name);
		}
		return retval;
	}
	
	public void setDictionary(String name, HashMap<String, String> dictionary) {
		dictionaries.put(name, dictionary);
	}
	
	public void setDictionary(String filePath) throws IOException  {
		if (filePath.endsWith(".json")) {
			ObjectMapper mapper = new ObjectMapper();
			setDictionary(filePath, (HashMap<String, String>) mapper.readValue(IOUtils.readerFromString(filePath), Map.class));
		}
		else {
			HashMap<String, String> dict = new HashMap<String, String>();
			List<String> nameFileEntries = IOUtils.linesFromFile(filePath);
			for (String line : nameFileEntries) {
				String[] name_value = line.split(" ");
				dict.put(name_value[0], name_value[1]);
			}
			setDictionary(filePath, dict);
		}
	}
	
	
	public HashMap<String, List<String>> getLists() {
		return lists;
	}

	public List<String> getList(String name) throws IOException {
		if (lists.get(name) == null) {
			setList(name);
		}
		return lists.get(name);
	}
	
	public void setList(String name, List<String> list) {
		lists.put(name, list);
	}
	
	public void setList(String filePath) throws IOException {
		setList(filePath, IOUtils.linesFromFile(filePath));
	}

}
