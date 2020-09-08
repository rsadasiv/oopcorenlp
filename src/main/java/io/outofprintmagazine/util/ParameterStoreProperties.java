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
package io.outofprintmagazine.util;

import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.node.ObjectNode;

/*
 * KNOWN PARAMETER FILES
 * ----oopcorenlp
 * --data/azure_api_key.txt
 * azure_apiKey
 * ??azure_apiKey2
 * --data/faceplusplus_api_key.txt
 * faceplusplus_apiKey
 * faceplusplus_secret
 * --data/flickr_api_key.txt
 * flickr_apiKey
 * flickr_secret
 * data/phrasefinder_credentials.properties
 * data/perfecttense.properties
 * data/wikipedia_api_key.txt
 * data/oopcorenlp.properties
 * 
 * --oopcorenlp_corpus
 * data/dropbox.properties
 * data/twitter.properties
 * data/mongodb.properties
 * data/postgresql.properties
 * data/aws.properties
 * data/corpus_bucket.txt
 * data/corpus_directory.txt
 */
//"data", "oopcorenlp.properties"


public class ParameterStoreProperties implements IParameterStore {

	private static final Logger logger = LogManager.getLogger(ParameterStoreProperties.class);
	
	private Properties props = new Properties();
	
	protected Logger getLogger() {
		return logger;
	}
	
	public ParameterStoreProperties() throws IOException {
		super();
    }
	
	public Properties getProperties() {
		return props;
	}

	@Override
    public String getProperty(String name) throws IOException {
    	return props.getProperty(name);
    }

	@Override
	public void init(ObjectNode properties) throws IOException {
		//pass
	}
	
	public void init(Properties p) throws IOException {
		props = p;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof ParameterStoreProperties) {
			return ((ParameterStoreProperties)o).getProperties().equals(getProperties());
		}
		else {
			return false;
		}
	}
}
