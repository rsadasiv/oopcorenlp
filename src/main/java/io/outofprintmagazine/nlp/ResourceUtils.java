package io.outofprintmagazine.nlp;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.stanford.nlp.io.IOUtils;

public class ResourceUtils {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(ResourceUtils.class);
	
	private static ResourceUtils single_instance = null; 

    public static ResourceUtils getInstance() throws IOException { 
        if (single_instance == null) 
            single_instance = new ResourceUtils(); 
  
        return single_instance; 
    }
	private HashMap<String, HashMap<String, String>> dictionaries = new HashMap<String, HashMap<String, String>>();
	private HashMap<String, List<String>> lists = new HashMap<String, List<String>>();
	
	private ResourceUtils() {
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
