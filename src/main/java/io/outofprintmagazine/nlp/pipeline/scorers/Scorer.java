package io.outofprintmagazine.nlp.pipeline.scorers;

import edu.stanford.nlp.pipeline.CoreDocument;

public interface Scorer {
	
	public void score(CoreDocument document);
	
	public Object aggregateDocument(CoreDocument document);

}
