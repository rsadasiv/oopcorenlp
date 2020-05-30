/*******************************************************************************
 * Copyright (C) 2020 Ram Sadasiv
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package io.outofprintmagazine.nlp.pipeline.annotators;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.trees.Tree;
import io.outofprintmagazine.nlp.pipeline.PhraseAnnotation;
import io.outofprintmagazine.nlp.pipeline.scorers.PhraseScorer;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.PhraseSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public abstract class AbstractTreeAnnotator extends AbstractPosAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(AbstractTreeAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}

	protected Scorer listScorer;
	protected Serializer listSerializer;
	
	protected List<String> omitPunctuationMarks = Arrays.asList("``", "''", "?", "??", "!", ".", "\"", "`", "‘", "’", "“", "”", ".", "*");
	protected List<String> removeLeadingSpace = Arrays.asList("n\'t", "\'s", ",", ":", ";");
	
	public AbstractTreeAnnotator() {
		super();
		this.setScorer((Scorer) new PhraseScorer(this.getAnnotationClass()));
		this.setSerializer((Serializer) new PhraseSerializer(this.getAnnotationClass()));
	}
	
	protected void traverseTree(Tree tree, Map<String,BigDecimal> scoreMap) throws IOException {
		if (!tree.label().toString().equals("ROOT")) {
			scoreTree(tree, scoreMap);
		}
		if (!tree.isLeaf()) {
			for (Tree child : tree.children()) {
				traverseTree(child, scoreMap);
			}
		}
	}
	
	protected void traverseTree(Tree tree, List<String> tags, Map<String,BigDecimal> scoreMap) throws IOException {
		if (!tree.label().toString().equals("ROOT")) {
			scoreTree(tree, tags, scoreMap);
		}
		if (!tree.isLeaf()) {
			for (Tree child : tree.children()) {
				traverseTree(child, tags, scoreMap);
			}
		}
	}
	
	protected void scoreTree(Tree pTree, Map<String, BigDecimal> scoreMap) throws IOException {
		String phrase = getOwnText(pTree, getTags());
		addToScoreMap(scoreMap, phrase, new BigDecimal(1));
	}
	
	protected void scoreTree(Tree pTree, List<String> tags, Map<String, BigDecimal> scoreMap) throws IOException {
		String phrase = getOwnText(pTree, getTags());
		addToScoreMap(scoreMap, phrase, new BigDecimal(1));
	}
	
	protected void traverseTree(Tree tree, List<PhraseAnnotation> scoreMap) throws IOException {
		if (!tree.label().toString().equals("ROOT")) {
			scoreTree(tree, scoreMap);
		}
		if (!tree.isLeaf()) {
			for (Tree child : tree.children()) {
				traverseTree(child, scoreMap);
			}
		}
	}
	
	protected void traverseTree(Tree tree, List<String> tags, List<PhraseAnnotation> scoreMap) throws IOException {
		if (!tree.label().toString().equals("ROOT")) {
			scoreTree(tree, tags, scoreMap);
		}
		if (!tree.isLeaf()) {
			for (Tree child : tree.children()) {
				traverseTree(child, tags, scoreMap);
			}
		}
	}
	
	protected void scoreTree(Tree pTree, List<PhraseAnnotation> scoreMap) throws IOException {
		String phrase = getOwnText(pTree, getTags());
		if (phrase.length() > 0) {
			addToScoreList(scoreMap, new PhraseAnnotation(phrase, new BigDecimal(1)));
		}
	}
	
	protected void scoreTree(Tree pTree, List<String> tags, List<PhraseAnnotation> scoreMap) throws IOException {
		String phrase = getOwnText(pTree, getTags());
		if (phrase.length() > 0) {
			addToScoreList(scoreMap, new PhraseAnnotation(phrase, new BigDecimal(1)));
		}
	}
	
	protected String getOwnText(Tree tree) {
		StringBuffer buf = new StringBuffer();
		for (Label label : tree.yield()) {
			if (!omitPunctuationMarks.contains(label.value())) {
				if (removeLeadingSpace.contains(label.value())) {
					if (buf.length() > 0 && Character.isWhitespace(buf.charAt(buf.length()-1))) {
						buf.deleteCharAt(buf.length()-1);
					}
				}
				buf.append(label.value());
				buf.append(" ");
			}
		}
		return buf.toString().trim();
	}
	
	protected String getOwnText(Tree tree, List<String> tags) {
		StringBuffer buf = new StringBuffer();
		boolean startPrinting = false;
		for (Label label : tree.yield()) {
			if (!startPrinting && tags.contains(label.value().toLowerCase())) {
				startPrinting = true;
			}
			if (startPrinting) {
				if (!omitPunctuationMarks.contains(label.value())) {
					if (removeLeadingSpace.contains(label.value())) {
						if (buf.length() > 0 && Character.isWhitespace(buf.charAt(buf.length()-1))) {
							buf.deleteCharAt(buf.length()-1);
						}
					}
					buf.append(label.value());
					buf.append(" ");
				}
			}
		}
		return buf.toString().trim();
	}

}
