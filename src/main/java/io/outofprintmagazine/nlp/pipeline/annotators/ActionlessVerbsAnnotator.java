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
import java.util.HashMap;
import java.util.List;
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

public class ActionlessVerbsAnnotator extends AbstractPosAnnotator implements Annotator, IOOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(ActionlessVerbsAnnotator.class);
	
	@SuppressWarnings("unused")
	private Logger getLogger() {
		return logger;
	}
	
	private List<String> posTags = Arrays.asList("VB","VBD","VBG","VBN","VBP","VBZ");
	
	public ActionlessVerbsAnnotator() {
		super();
		this.appendTagsFromFile("io/outofprintmagazine/nlp/models/StativeVerbs.txt");
		this.setScorer((IScorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((ISerializer)new MapSerializer(this.getAnnotationClass()));			
	}	

	/**
	 * @see io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPActionlessVerbsAnnotation 
	 */
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPActionlessVerbsAnnotation.class;
	}
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (int i=0;i<sentence.tokens().size();i++) {
				CoreLabel token = sentence.tokens().get(i);
				if (posTags.contains(token.tag())) {
					if (getTags().contains(token.lemma().toLowerCase())) {
						if (!token.lemma().startsWith("'")) {
							Map<String,BigDecimal> scoreMap = new HashMap<String,BigDecimal>();
							addToScoreMap(scoreMap, token.lemma(), new BigDecimal(1));
							token.set(getAnnotationClass(), scoreMap);
						}
					}
				}
			}
		}
	}

	/**
	 * @return Intransitive Verbs. VB,VBD,VBG,VBN,VBP,VBZ in io/outofprintmagazine/nlp/models/StativeVerbs.txt
	 */
	@Override
	public String getDescription() {
		return "Intransitive Verbs. VB,VBD,VBG,VBN,VBP,VBZ in io/outofprintmagazine/nlp/models/StativeVerbs.txt";
	}
}
