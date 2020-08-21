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
package io.outofprintmagazine.nlp.pipeline.annotators.conditional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.pipeline.PhraseAnnotation;
import io.outofprintmagazine.nlp.pipeline.annotators.AbstractTreeAnnotator;
import io.outofprintmagazine.nlp.pipeline.annotators.IOOPAnnotator;

public class IfAnnotator extends AbstractTreeAnnotator implements Annotator, IOOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(IfAnnotator.class);
	
	@SuppressWarnings("unused")
	private Logger getLogger() {
		return logger;
	}

	public IfAnnotator() {
		super();
		this.setTags(Arrays.asList("if"));
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
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPIfAnnotation.class;
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
	}
	
	@Override
	public String getDescription() {
		return "Constituency Parse: SBAR, if. https://gist.github.com/nlothian/9240750"; 
	}
	
}
