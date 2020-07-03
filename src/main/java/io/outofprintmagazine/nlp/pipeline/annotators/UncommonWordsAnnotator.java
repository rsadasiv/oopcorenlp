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
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;


public class UncommonWordsAnnotator extends AbstractPosAnnotator implements Annotator, OOPAnnotator{
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(UncommonWordsAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}
	
	public UncommonWordsAnnotator() {
		super();
		this.appendTagsFromFile("io/outofprintmagazine/nlp/models/COCA/en_20k.txt");
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass()));			
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPUncommonWordsAnnotation.class;
	}

	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (isDictionaryWord(token)) {
					if ((!getTags().contains(token.lemma())) && (!getTags().contains(token.lemma().toLowerCase()))) {
						Map<String, BigDecimal> scoreMap = new HashMap<String, BigDecimal>();
						addToScoreMap(scoreMap, token.lemma(), new BigDecimal(1));
						token.set(getAnnotationClass(), scoreMap);
					}
				}
			}
		}
	}

	@Override
	public String getDescription() {
		return "not in io/outofprintmagazine/nlp/models/COCA/en_20k.txt";
	}
}
