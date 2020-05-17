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

public class LikeAnnotator extends AbstractTreeAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(LikeAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}
	
	public LikeAnnotator() {
		super();
		this.setTags(Arrays.asList("like"));
	}
	
	public LikeAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPLikeAnnotation.class;
	}
	
	
	@Override
	protected void scoreTree(Tree tree, List<String> tags, List<PhraseAnnotation> scoreMap) throws IOException {
		if (tree.label().toString().equals("PP")) {
			super.scoreTree(tree, tags, scoreMap);
		}
	}

	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			//Map<String, BigDecimal> scoreMap = new HashMap<String, BigDecimal>();
			List<PhraseAnnotation> scoreMap= new ArrayList<PhraseAnnotation>();
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
		return "Constituency Parse: PP, like. https://gist.github.com/nlothian/9240750"; 
	}
	
	
}
