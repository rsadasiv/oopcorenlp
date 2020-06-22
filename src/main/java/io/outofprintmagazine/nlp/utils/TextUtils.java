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
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.stanford.nlp.util.StringUtils;
import io.outofprintmagazine.util.ParameterStore;


public class TextUtils {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(TextUtils.class);
	
	private static Map<ParameterStore, TextUtils> instances = new HashMap<ParameterStore, TextUtils>();
	
    public static TextUtils getInstance(ParameterStore parameterStore) throws IOException { 
        if (instances.get(parameterStore) == null) {
        	TextUtils instance = new TextUtils(parameterStore);
            instances.put(parameterStore, instance);
        }
        return instances.get(parameterStore); 
    }
	
	private TextUtils(ParameterStore parameterStore) {
		super();
	}


	//need to convert single angle quote to double vertical quote
	//if the single quote starts the line - yes
	// ^\u2018
	//if the single quote ends the line - yes
	// \u2019$
	//if the single quote has printing characters on both sides - no
	//else - yes
	// \s\u2018
	// \u2019\s
	//
	//still having a problem with plural possessive. The Smiths' house.
	
	public String processPlainUnicode(String input) {
		input = Pattern.compile("^\\u2018", Pattern.MULTILINE).matcher(input).replaceAll("\"");
		input = Pattern.compile("\\s\\u2018(\\S)", Pattern.MULTILINE).matcher(input).replaceAll(" \"$1");
		input = Pattern.compile("\\u2019$", Pattern.MULTILINE).matcher(input).replaceAll("\"");
		input = Pattern.compile("(\\S)\\u2019\\s", Pattern.MULTILINE).matcher(input).replaceAll("$1\" ");
		input = Pattern.compile("\\u2019(\\.)", Pattern.MULTILINE).matcher(input).replaceAll("\"$1");
		return StringUtils.toAscii(StringUtils.normalize(input)).trim();
	}
}
