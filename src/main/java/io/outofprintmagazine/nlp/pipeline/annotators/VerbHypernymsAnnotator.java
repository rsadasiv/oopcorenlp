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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;
import io.outofprintmagazine.nlp.utils.WordnetUtils;

public class VerbHypernymsAnnotator extends AbstractPosAnnotator implements Annotator, OOPAnnotator{
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(VerbHypernymsAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}
	
	private List<String> posTags = Arrays.asList("VB","VBD","VBG","VBN","VBP","VBZ");
	
	public VerbHypernymsAnnotator() {
		super();
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass()));
		this.appendTagsFromFile("io/outofprintmagazine/nlp/models/StativeVerbs.txt");
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbHypernymsAnnotation.class;
	}

	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(
			new ArraySet<>(
				Arrays.asList(
					CoreAnnotations.TextAnnotation.class, 
					CoreAnnotations.TokensAnnotation.class,
					CoreAnnotations.SentencesAnnotation.class, 
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbsAnnotation.class
				)
			)
		);
	}
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		Map<String,BigDecimal> allVerbs = new HashMap<String,BigDecimal>();
		if (!annotation.containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbsAnnotation.class)) {
			logger.error("requires() not satisfied");
		}
		else {
			allVerbs = annotation.get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbsAnnotation.class);
		}
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (posTags.contains(token.tag())) {
					if (!getTags().contains(token.lemma().toLowerCase())) {
						if (!token.lemma().startsWith("'")) {
							try {
								Map<String, BigDecimal> scoreMap = WordnetUtils.getInstance(getParameterStore()).scoreTokenHypernym(token, getContextWords(document, token), allVerbs);
								if (scoreMap.size() > 0) {
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
		}
	}
	
	@Override
	public String getDescription() {
		return "Wordnet hypernym.";
	}

}
