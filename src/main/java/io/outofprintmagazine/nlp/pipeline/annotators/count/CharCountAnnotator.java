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
package io.outofprintmagazine.nlp.pipeline.annotators.count;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.pipeline.annotators.AbstractAnnotator;
import io.outofprintmagazine.nlp.pipeline.annotators.IOOPAnnotator;
import io.outofprintmagazine.nlp.pipeline.scorers.BigDecimalSumTokenScorer;
import io.outofprintmagazine.nlp.pipeline.scorers.IScorer;
import io.outofprintmagazine.nlp.pipeline.serializers.BigDecimalSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.ISerializer;

public class CharCountAnnotator extends AbstractAnnotator implements Annotator, IOOPAnnotator {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(CharCountAnnotator.class);
	
	@SuppressWarnings("unused")
	private Logger getLogger() {
		return logger;
	}

	
	public CharCountAnnotator() {
		super();
		this.setScorer((IScorer) new BigDecimalSumTokenScorer(this.getAnnotationClass()));
		this.setSerializer((ISerializer) new BigDecimalSerializer(this.getAnnotationClass()));
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPCharCountAnnotation.class;
	}

		
	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(
			new ArraySet<>(
				Arrays.asList(
					CoreAnnotations.TextAnnotation.class, 
					CoreAnnotations.TokensAnnotation.class,
					CoreAnnotations.SentencesAnnotation.class,
					CoreAnnotations.ParagraphIndexAnnotation.class
				)
			)
		);
	}

	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);		
		for (CoreSentence sentence : document.sentences()) {
			sentence.coreMap().set(getAnnotationClass(), new BigDecimal(sentence.charOffsets().second().intValue()-sentence.charOffsets().first().intValue()));
			for (CoreLabel token : sentence.tokens()) {
				token.set(getAnnotationClass(), new BigDecimal(token.endPosition()-token.beginPosition()));
			}
		}
		document.annotation().set(getAnnotationClass(), new BigDecimal(document.text().length()));
	}

	@Override
	public String getDescription() {
		return "characterCount";
	}

}
