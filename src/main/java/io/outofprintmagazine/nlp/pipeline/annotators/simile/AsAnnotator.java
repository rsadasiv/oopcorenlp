package io.outofprintmagazine.nlp.pipeline.annotators.simile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.trees.Tree;
import io.outofprintmagazine.nlp.pipeline.PhraseAnnotation;
import io.outofprintmagazine.nlp.pipeline.annotators.AbstractTreeAnnotator;
import io.outofprintmagazine.nlp.pipeline.annotators.OOPAnnotator;

public class AsAnnotator extends AbstractTreeAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(AsAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}
	
	
	public AsAnnotator() {
		super();
		this.setTags(Arrays.asList("as"));
	}
	
	public AsAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAsAnnotation.class;
	}
	
	
	@Override
	protected void scoreTree(Tree tree, List<String> tags, List<PhraseAnnotation> scoreMap) throws IOException {
		if (tree.label().toString().equals("SBAR")) {
			super.scoreTree(tree, tags, scoreMap);
		}
	}

	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			List<PhraseAnnotation> scoreMap = new ArrayList<PhraseAnnotation>();
			Tree docTree = sentence.constituencyParse();
			try {
				traverseTree(docTree, getTags(), scoreMap);
				if (scoreMap.size() > 0) {
					sentence.coreMap().set(getAnnotationClass(), scoreMap);
				}
			}
			catch (Exception e) {
				logger.error(e);
			}
		}
		score(document);
	}
	

	@Override
	public String getDescription() {
		return "Constituency Parse: PP, as. https://gist.github.com/nlothian/9240750"; 
	}


	
	
}
