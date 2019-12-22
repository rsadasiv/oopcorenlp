package io.outofprintmagazine.nlp.pipeline.annotators;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.trees.Tree;
import io.outofprintmagazine.nlp.pipeline.scorers.MapListScorer;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapListSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public class SimileAnnotator extends AbstractListAggregatePosAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(SimileAnnotator.class);
	
	List<String> include = new ArrayList<String>(Arrays.asList("like", "as"));
	
	public SimileAnnotator() {
		super();
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass(), this.getAggregateClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass(), this.getAggregateClass()));	
		this.setListScorer((Scorer)new MapListScorer(this.getListClass()));
		this.setListSerializer((Serializer)new MapListSerializer(this.getListClass()));
	}
	
	public SimileAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPSimileAnnotation.class;
	}
	
	@Override
	public Class getAggregateClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPSimileAnnotationAggregate.class;
	}
	
	@Override
	public Class getListClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPSimileAnnotationList.class;
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
		if (tree.label().toString().equals("PP")) {
			String likeOrAs = null;
			for (Label label : tree.yield()) {
				if (include.contains(label.value().trim())) {
					likeOrAs = label.value().trim();
					break;
				}
			}
			if (likeOrAs != null) {
				List<String> scoreList = scoreMap.get(likeOrAs);
				if (scoreList == null) {
					scoreList = new ArrayList<String>();
				}
				scoreList.add(getOwnText(tree));
				scoreMap.put(likeOrAs, scoreList);
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
				sentence.coreMap().set(getListClass(), scoreMap);
			}
			for (CoreLabel token : sentence.tokens()) {
				if (!token.tag().startsWith("VB") && include.contains(token.lemma())) {
					Map<String,BigDecimal> tokenScoreMap = new HashMap<String,BigDecimal>();
					tokenScoreMap.put(token.lemma(), new BigDecimal(1));
					token.set(getAnnotationClass(), tokenScoreMap);

				}
			}
		}
		score(document);
	}

	@Override
	public String getDescription() {
		return "like or as";
	}
}
