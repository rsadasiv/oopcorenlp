package io.outofprintmagazine.nlp.pipeline.annotators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;

public abstract class AbstractPosAnnotator extends AbstractAnnotator implements Annotator, OOPAnnotator  {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(AbstractPosAnnotator.class);

	protected List<String> tags = new ArrayList<String>();

	public AbstractPosAnnotator() {
		super();
	}
	
	public AbstractPosAnnotator(Properties properties) {
		this();
		this.properties = properties;
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
