package io.outofprintmagazine.nlp.pipeline.annotators;

import edu.stanford.nlp.pipeline.CoreDocument;

public class RunnableOOPAnnotator extends Thread {

	private CoreDocument document;
	private IOOPAnnotator annotator;
	
	public RunnableOOPAnnotator(IOOPAnnotator annotator, CoreDocument document) {
		super();
		this.annotator = annotator;
		this.document = document;
	}
	
	@Override
	public void run() {
		annotator.annotate(document.annotation());
	}

	public CoreDocument getDocument() {
		return document;
	}

	public void setDocument(CoreDocument document) {
		this.document = document;
	}

	public IOOPAnnotator getAnnotator() {
		return annotator;
	}

	public void setAnnotator(IOOPAnnotator annotator) {
		this.annotator = annotator;
	}

}
