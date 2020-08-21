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
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.IScorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.ISerializer;
import io.outofprintmagazine.nlp.utils.WordnetUtils;

public class VerbGroupsAnnotator extends AbstractPosAnnotator implements Annotator, IOOPAnnotator{
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(VerbGroupsAnnotator.class);
	
	@SuppressWarnings("unused")
	private Logger getLogger() {
		return logger;
	}

	public VerbGroupsAnnotator() {
		super();
		this.setScorer((IScorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((ISerializer)new MapSerializer(this.getAnnotationClass()));
		this.setTags(Arrays.asList("VB","VBD","VBG","VBN","VBP","VBZ"));
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbGroupsAnnotation.class;
	}

	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (int i=0;i<document.sentences().size();i++) {
			CoreSentence sentence = document.sentences().get(i);			
			for (CoreLabel token : sentence.tokens()) {
				if (scoreTag(token) != null) {
					try {
						String score = WordnetUtils.getInstance(getParameterStore()).getLexicalFileName(token, getContextWords(document, token));
						if (score != null) {
							if (score.lastIndexOf('.') > -1) {
								score = score.substring(score.lastIndexOf('.')+1);
							}
							Map<String,BigDecimal> scoreMap = new HashMap<String,BigDecimal>();
							addToScoreMap(scoreMap, score, new BigDecimal(1));
							token.set(getAnnotationClass(), scoreMap);
						}
					} 
					catch (IOException e) {
						logger.error(e);
					}
				}
			}
		}
	}

	@Override
	public String getDescription() {
		return "Wordnet lexical file name. https://wordnet.princeton.edu/documentation/lexnames5wn.";
	}
}
