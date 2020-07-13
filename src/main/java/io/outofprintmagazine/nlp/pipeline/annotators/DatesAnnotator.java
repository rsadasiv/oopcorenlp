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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.pipeline.PhraseAnnotation;

public class DatesAnnotator extends AbstractTreeAnnotator implements Annotator, OOPAnnotator {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(DatesAnnotator.class);
	
	@SuppressWarnings("unused")
	private Logger getLogger() {
		return logger;
	}

	public DatesAnnotator() {
		super();
	}

	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPDatesAnnotation.class;
	}

	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(CoreAnnotations.NamedEntityTagAnnotation.class,
				CoreAnnotations.NormalizedNamedEntityTagAnnotation.class,
				CoreAnnotations.CanonicalEntityMentionIndexAnnotation.class)));
	}

	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			List<CoreEntityMention> entityMentions = sentence.entityMentions();
			for (CoreEntityMention mention : entityMentions) {
				if (mention.entityType().equals("DATE") || mention.entityType().equals("TIME")) {
					List<PhraseAnnotation> scoreMap = new ArrayList<PhraseAnnotation>();
					if (mention.canonicalEntityMention().isPresent()) {
						addToScoreList(scoreMap, new PhraseAnnotation(mention.canonicalEntityMention().get().toString().toLowerCase(), new BigDecimal(1)));
						for (CoreLabel token : mention.tokens()) {
							if (!token.containsKey(getAnnotationClass())) {
								token.set(getAnnotationClass(), scoreMap);
								break;
							}
						}
					}

				}
			}
		}
	}

	@Override
	public String getDescription() {
		return "CoreNLP NER mentions: DATE or TIME";
	}
}
