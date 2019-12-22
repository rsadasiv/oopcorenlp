package io.outofprintmagazine.nlp.pipeline.annotators;

import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.pipeline.Annotator;

public abstract class AbstractAggregatePosAnnotator extends AbstractPosAnnotator implements Annotator, OOPAnnotator  {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(AbstractAggregatePosAnnotator.class);

	public AbstractAggregatePosAnnotator() {
		super();
	}

	public AbstractAggregatePosAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	public abstract Class getAggregateClass();
	
	@Override
	public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
		Set<Class<? extends CoreAnnotation>> retval = super.requirementsSatisfied();
		retval.add(getAggregateClass());
		return retval;
	}

}
