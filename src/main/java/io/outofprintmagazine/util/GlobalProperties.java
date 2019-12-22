package io.outofprintmagazine.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class GlobalProperties extends Properties {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(GlobalProperties.class);
		
	private GlobalProperties() throws IOException {
		super();
		try (final InputStream stream = this.getClass().getResourceAsStream("/oopcorenlp.properties")) {
		    load(stream);
		}
	}
	
	private static GlobalProperties single_instance = null; 

    public static GlobalProperties getInstance() throws IOException { 
        if (single_instance == null) 
            single_instance = new GlobalProperties(); 
  
        return single_instance; 
    }
    
    public static void main(String[] argv) throws IOException {
    	Properties p = new Properties();
    	InputStream stream = GlobalProperties.class.getResourceAsStream("/oopcorenlp.properties");
    	p.load(stream);
    	stream.close();
    	
    }

}
