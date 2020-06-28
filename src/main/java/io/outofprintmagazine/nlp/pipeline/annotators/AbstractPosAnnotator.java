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
package io.outofprintmagazine.nlp.pipeline.annotators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;

/**
 * <p>Base class for custom annotators that work with part of speech tags (Core Nlp pos).</p>
 * <p>tags is conventionally a list of the part of speech tags that the annotator is interested in handling (or ignoring).</p>
 * @author Ram Sadasiv
 *
 */
public abstract class AbstractPosAnnotator extends AbstractAnnotator implements Annotator, OOPAnnotator  {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(AbstractPosAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}

	protected List<String> tags = new ArrayList<String>();

	public AbstractPosAnnotator() {
		super();
	}
	
	protected void appendTagsFromFile(String fileName) {
	    List<String> nameFileEntries = IOUtils.linesFromFile(fileName);
	    for (String nameCSV : nameFileEntries) {
	    	String[] nameValue = nameCSV.split(" ");
	    	if (nameValue.length > 1) {
	    		getTags().add(nameValue[0].toLowerCase());
	    	}
	    	else {
	    		String[] namesForThisLine = nameCSV.split(",");
	    		for (String name : namesForThisLine) {
	    			getTags().add(name.trim().toLowerCase());
	    		}
	    	}
	    }
	}
	
	//is this right generally?
	//not sure that it matters, pipeline does not grok custom annotators in requirements satisfied
	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(
			new ArraySet<>(
				Arrays.asList(
					CoreAnnotations.TextAnnotation.class, 
					CoreAnnotations.TokensAnnotation.class,
					CoreAnnotations.SentencesAnnotation.class,
					CoreAnnotations.PartOfSpeechAnnotation.class,
					CoreAnnotations.LemmaAnnotation.class
				)
			)
		);
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	
	public String scoreToken(CoreLabel token) {
		return scoreToken(token, getTags());
	}
	
	public String scoreLemma(CoreLabel token) {
		return scoreLemma(token, getTags());
	}
	
	public String scoreTag(CoreLabel token) {
		return scoreTag(token, getTags());
	}
	
	public String scoreToken(CoreLabel token, List<String> tags) {
		String retval = null;
		if (tags.contains(token.tag())) {
			retval = token.originalText();
		}
		return retval;
	}
	
	public String scoreLemma(CoreLabel token, List<String> tags) {
		String retval = null;
		if (tags.contains(token.tag())) {
			retval = token.lemma();
		}
		return retval;
	}
	
	public String scoreTag(CoreLabel token, List<String> tags) {
		String retval = null;
		if (tags.contains(token.tag())) {
			retval = token.tag();
		}
		return retval;
	}

}
