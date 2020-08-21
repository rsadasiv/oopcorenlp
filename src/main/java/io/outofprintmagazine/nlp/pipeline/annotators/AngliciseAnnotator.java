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
import io.outofprintmagazine.nlp.utils.ResourceUtils;

public class AngliciseAnnotator extends AbstractPosAnnotator implements Annotator, IOOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(AngliciseAnnotator.class);
	
	@SuppressWarnings("unused")
	private Logger getLogger() {
		return logger;
	}
	
	public AngliciseAnnotator() {
		super();
		this.setScorer((IScorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((ISerializer)new MapSerializer(this.getAnnotationClass()));
	}

	@Override
	public String getDescription() {
		return "io/outofprintmagazine/nlp/models/EN_GB/american_spellings.json";
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAngliciseAnnotation.class;
	}
	
	@Override
	public void annotate(Annotation annotation) {
		try {
			Map<String,String> dictionary = ResourceUtils.getInstance(getParameterStore()).getDictionary("io/outofprintmagazine/nlp/models/EN_GB/american_spellings.json");
			CoreDocument document = new CoreDocument(annotation);
			for (CoreSentence sentence : document.sentences()) {
				for (CoreLabel token : sentence.tokens()) {
					String score = dictionary.get(token.originalText().toLowerCase());
					if (score != null) {
						Map<String, BigDecimal> scoreMap = new HashMap<String, BigDecimal>();
						addToScoreMap(scoreMap, score, new BigDecimal(1));
						token.set(getAnnotationClass(), scoreMap);
					}
				}
			}
		}
		catch (IOException e) {
			getLogger().error(e);
		}
	}
}
