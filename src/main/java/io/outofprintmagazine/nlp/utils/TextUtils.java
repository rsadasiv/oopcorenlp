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
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.stanford.nlp.util.StringUtils;
import io.outofprintmagazine.util.IParameterStore;


public class TextUtils {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(TextUtils.class);
	
	private static Map<IParameterStore, TextUtils> instances = new HashMap<IParameterStore, TextUtils>();
	
    public static TextUtils getInstance(IParameterStore parameterStore) throws IOException { 
        if (instances.get(parameterStore) == null) {
        	TextUtils instance = new TextUtils(parameterStore);
            instances.put(parameterStore, instance);
        }
        return instances.get(parameterStore); 
    }
	
	private TextUtils(IParameterStore parameterStore) {
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
	
	public String processPlainUnicode(String input) throws IOException {
		Pattern startLine = Pattern.compile("^\\u2018");
		Pattern startWord = Pattern.compile("\\s\\u2018(\\S)");
		Pattern endLine = Pattern.compile("\\u2019$");
		Pattern endWord = Pattern.compile("(\\S)\\u2019\\s");
		Pattern endSentence = Pattern.compile("\\u2019(\\.)");
		StringBuffer output = new StringBuffer();
		for (String line : IOUtils.readLines(new StringReader(input))) {
			line = line.replaceAll("[\\u00A0\\u2007\\u202F]+", " ").trim();
			line = line.replaceAll("[\\u2028]", "/n").trim();
			if (line.length() > 0) {
				line = startLine.matcher(line).replaceAll("\"");
				line = startWord.matcher(line).replaceAll(" \"$1");
				line = endLine.matcher(line).replaceAll("\"");
				line = endWord.matcher(line).replaceAll("$1\" ");
				line = endSentence.matcher(line).replaceAll("\"$1");
				line = StringUtils.toAscii(StringUtils.normalize(line));
				output.append(line);
				output.append('\n');
				output.append('\n');
			}
		}
		return output.toString();
	}
}
