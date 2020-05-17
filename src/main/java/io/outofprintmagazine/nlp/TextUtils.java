package io.outofprintmagazine.nlp;


import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.stanford.nlp.util.StringUtils;


public class TextUtils {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(TextUtils.class);
	
	private static TextUtils single_instance = null;


    public static TextUtils getInstance() throws IOException { 
        if (single_instance == null) 
            single_instance = new TextUtils(); 
  
        return single_instance; 
    }
	
	private TextUtils() {
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
