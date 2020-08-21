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
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.pipeline.PhraseAnnotation;
import io.outofprintmagazine.nlp.pipeline.scorers.PhraseScorer;
import io.outofprintmagazine.nlp.pipeline.scorers.IScorer;
import io.outofprintmagazine.nlp.pipeline.serializers.PhraseSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.ISerializer;

public class LocationsAnnotator extends AbstractPosAnnotator implements Annotator, IOOPAnnotator{
	
	private static final Logger logger = LogManager.getLogger(LocationsAnnotator.class);
	
	@SuppressWarnings("unused")
	private Logger getLogger() {
		return logger;
	}

	public LocationsAnnotator() {
		super();
		this.setScorer((IScorer) new PhraseScorer(this.getAnnotationClass()));
		this.setSerializer((ISerializer) new PhraseSerializer(this.getAnnotationClass()));			
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPLocationsAnnotation.class;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(
			new ArraySet<>(
				Arrays.asList(
					CoreAnnotations.NamedEntityTagAnnotation.class, 
					CoreAnnotations.NormalizedNamedEntityTagAnnotation.class,
					CoreAnnotations.CanonicalEntityMentionIndexAnnotation.class
				)
			)
		);
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			List<CoreEntityMention> entityMentions = sentence.entityMentions();
			for (CoreEntityMention mention : entityMentions) {
				if (mention.entityType().equals("LOCATION")) {
					List<PhraseAnnotation> scoreList = new ArrayList<PhraseAnnotation>();
					if (mention.canonicalEntityMention().isPresent()) {
						addToScoreList(scoreList, new PhraseAnnotation(mention.canonicalEntityMention().get().toString(), new BigDecimal(1)));
					}
					else {
						addToScoreList(scoreList, new PhraseAnnotation(mention.tokens().get(0).originalText(), new BigDecimal(1)));
					}
					mention.tokens().get(0).set(getAnnotationClass(), scoreList);
				}
			}
		}
	}

	@Override
	public String getDescription() {
		return "CoreNLP NER mentions: LOCATION";
	}

}
