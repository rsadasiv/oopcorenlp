package io.outofprintmagazine.nlp.pipeline.annotators;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.pipeline.scorers.MapListScorer;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapListSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public class WhaAnnotator extends AbstractListAggregatePosAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(WhaAnnotator.class);
	
	public WhaAnnotator() {
		super();
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass(), this.getAggregateClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass(), this.getAggregateClass()));
		this.setListScorer((Scorer)new MapListScorer(this.getListClass()));
		this.setListSerializer((Serializer)new MapListSerializer(this.getListClass()));
	}
	
	public WhaAnnotator(Properties properties) {
		this();
		this.properties = properties;

	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}

	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(
			new ArraySet<>(
				Arrays.asList(
					CoreAnnotations.TextAnnotation.class, 
					CoreAnnotations.TokensAnnotation.class,
					CoreAnnotations.SentencesAnnotation.class,
					TreeCoreAnnotations.TreeAnnotation.class,
					SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class
				)
			)
		);
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPWhaAnnotation.class;
	}
	
	
	@Override
	public Class getAggregateClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPWhaAnnotationAggregate.class;
	}
	
	@Override
	public Class getListClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPWhaAnnotationList.class;
	}
	
	private void traverseTree(Tree tree, Map<String,List<String>> scoreMap) {
		if (!tree.label().toString().equals("ROOT")) {
			scoreTree(tree, scoreMap);
		}
		if (!tree.isLeaf()) {
			for (Tree child : tree.children()) {
				traverseTree(child, scoreMap);
			}
		}
	}
	
	private void scoreTree(Tree tree, Map<String,List<String>> scoreMap) {
		if (tree.label().toString().equals("SBARQ")) {
			for (int pIdx=0; pIdx<tree.children().length; pIdx++ ) {
				Tree pTree = tree.children()[pIdx];
				if (pTree.label().toString().equals("WHNP") || pTree.label().toString().equals("WHADVP")) {
					List<String> scoreList = scoreMap.get(getOwnText(pTree.firstChild()));
					if (scoreList == null) {
						scoreList = new ArrayList<String>();
					}
					scoreList.add(getOwnText(tree));
					scoreMap.put(getOwnText(pTree.firstChild()), scoreList);
				}
			}
		}
	}
	
	private String getOwnText(Tree tree) {
		StringBuffer buf = new StringBuffer();
		for (Label label : tree.yield()) {
			buf.append(label.value().trim());
			buf.append(" ");
		}
		return buf.toString().trim().toLowerCase();
	}
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			Map<String, List<String>> scoreMap = new HashMap<String, List<String>>();
			Tree docTree = sentence.constituencyParse();
			traverseTree(docTree, scoreMap);
			if (scoreMap.size() > 0) {
				Map<String,BigDecimal> scores = new HashMap<String,BigDecimal>();
				for (String score : scoreMap.keySet()) {
					scores.put(score, new BigDecimal(scoreMap.get(score).size()));
				}
				sentence.coreMap().set(getAnnotationClass(), scores);
				sentence.coreMap().set(getListClass(), scoreMap);
			}
		}
		score(document);
	}

	@Override
	public String getDescription() {
		return "Constituency Parse: SBARQ, WHNP, WHADVP. https://gist.github.com/nlothian/9240750"; 
	}
}
