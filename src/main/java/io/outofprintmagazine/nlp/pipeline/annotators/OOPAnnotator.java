package io.outofprintmagazine.nlp.pipeline.annotators;

import java.util.Map;

import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.stanford.nlp.pipeline.CoreDocument;

public interface OOPAnnotator {
	
	public Class getAnnotationClass();
	
	public String getDescription();
	
	public void score(CoreDocument document);
	
	public void serialize(CoreDocument document, ObjectNode json);
	
	public void init(Map<String,Object> properties);

}
