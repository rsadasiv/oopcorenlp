package io.outofprintmagazine.nlp.pipeline.annotators;

import java.util.Properties;
import java.util.Set;

import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public abstract class AbstractListAggregatePosAnnotator extends AbstractAggregatePosAnnotator implements Annotator, OOPAnnotator {

	protected Scorer listScorer;
	protected Serializer listSerializer;
	
	public AbstractListAggregatePosAnnotator() {
		super();
	}

	public AbstractListAggregatePosAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	public Scorer getListScorer() {
		return listScorer;
	}

	public void setListScorer(Scorer listScorer) {
		this.listScorer = listScorer;
	}

	public Serializer getListSerializer() {
		return listSerializer;
	}

	public void setListSerializer(Serializer listSerializer) {
		this.listSerializer = listSerializer;
	}

	public abstract Class getListClass();
	
	@Override
	public void score(CoreDocument document) {
		getScorer().score(document);
		getListScorer().score(document);
	}
	
	@Override
	public void serialize(CoreDocument document, ObjectNode json) {
		getSerializer().serialize(document, json);
		getListSerializer().serialize(document, json);
	}
	
	@Override
	public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
		Set<Class<? extends CoreAnnotation>> retval = super.requirementsSatisfied();
		retval.add(getListClass());
		return retval;
	}

}
