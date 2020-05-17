package io.outofprintmagazine.nlp.pipeline.serializers;

import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.stanford.nlp.pipeline.CoreDocument;

public interface Serializer {

	public void serialize(CoreDocument document, ObjectNode json);
	
	public void serializeAggregate(Object aggregate, ObjectNode json);
}
