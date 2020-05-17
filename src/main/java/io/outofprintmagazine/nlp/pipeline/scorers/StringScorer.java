package io.outofprintmagazine.nlp.pipeline.scorers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.pipeline.CoreDocument;

public class StringScorer implements Scorer {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(StringScorer.class);


	protected Class annotationClass = null;


	public StringScorer() {
		super();
	}

	public StringScorer(Class annotationClass) {
		super();
		this.setAnnotationClass(annotationClass);
	}
	

	public void setAnnotationClass(Class annotationClass) {
		this.annotationClass = annotationClass;
	}

	public Class getAnnotationClass() {
		return this.annotationClass;
	}
	
	@Override
	public void score(CoreDocument document) {
		//pass
	}

	@Override
	public Object aggregateDocument(CoreDocument document) {
		// pass
		return null;
	}
	
}
