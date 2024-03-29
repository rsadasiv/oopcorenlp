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
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import io.outofprintmagazine.nlp.pipeline.scorers.BigDecimalSumSentenceScorer;
import io.outofprintmagazine.nlp.pipeline.scorers.IScorer;
import io.outofprintmagazine.nlp.pipeline.serializers.BigDecimalSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.ISerializer;

public class VerblessSentencesAnnotator extends AbstractPosAnnotator implements Annotator, IOOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(VerblessSentencesAnnotator.class);
	
	@SuppressWarnings("unused")
	private Logger getLogger() {
		return logger;
	}
	
	public VerblessSentencesAnnotator() {
		super();
		this.setTags(Arrays.asList("VB","VBD","VBG","VBN","VBP","VBZ","MD"));
		this.setScorer((IScorer) new BigDecimalSumSentenceScorer(this.getAnnotationClass()));
		this.setSerializer((ISerializer) new BigDecimalSerializer(this.getAnnotationClass()));
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerblessSentencesAnnotation.class;
	}

	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			boolean shouldScore = true;
			for (CoreLabel token : sentence.tokens()) {
				if (scoreTag(token) != null) {
					shouldScore = false;
					break;
				}
			}
			if (shouldScore) {
				sentence.coreMap().set(getAnnotationClass(), new BigDecimal(1));
			}
		}
	}

	@Override
	public String getDescription() {
		return "VB,VBD,VBG,VBN,VBP,VBZ,MD not in sentence.";
	}
		
}
