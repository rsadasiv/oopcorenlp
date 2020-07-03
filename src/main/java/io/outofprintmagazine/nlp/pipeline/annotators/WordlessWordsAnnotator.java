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
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;
import io.outofprintmagazine.nlp.utils.WiktionaryUtils;
import io.outofprintmagazine.nlp.utils.WordnetUtils;

public class WordlessWordsAnnotator extends AbstractPosAnnotator implements Annotator, OOPAnnotator {
	
	private static final Logger logger = LogManager.getLogger(WordlessWordsAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}
	
	public WordlessWordsAnnotator() {
		super();
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass()));
		this.appendTagsFromFile("io/outofprintmagazine/nlp/models/COCA/Dolch.txt");
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPWordlessWordsAnnotation.class;
	}

	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		List<String> queries = new ArrayList<String>();
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (isDictionaryWord(token)) {
					if (!getTags().contains(token.lemma())) {
						try {
							if (WordnetUtils.getInstance(getParameterStore()).getIndexWord(token) == null) {
								queries.add(token.originalText().toLowerCase());
							}
						}
						catch (Exception e) {
							queries.add(token.originalText().toLowerCase());
						}
					}
				}
			}
		}
		try {
			WiktionaryUtils.getInstance(getParameterStore()).addToWordCache(queries);
			for (int i=0;i<document.tokens().size();i++) {
				CoreLabel token = document.tokens().get(i);
				try {
					if (isDictionaryWord(token)) {
						if (!getTags().contains(token.lemma())) {
							if (WordnetUtils.getInstance(getParameterStore()).getIndexWord(token) == null) {
								String wordCache = WiktionaryUtils.getInstance(getParameterStore()).getWordCache(token.originalText().toLowerCase());
								if (wordCache == null) {
									Map<String,BigDecimal> scoreMap = new HashMap<String,BigDecimal>();
									addToScoreMap(scoreMap, token.lemma(), new BigDecimal(1));
									token.set(getAnnotationClass(), scoreMap);
								}
							}
						}
					}
				}
				catch (Throwable t) {
					logger.error("wtf?", t);
				}
			}
			WiktionaryUtils.getInstance(getParameterStore()).pruneWordCache();
		}
		catch (Exception e) {
			logger.error(e);
		}
	}

	@Override
	public String getDescription() {
		return "Wiktionary lookup is null";
	}
	
}
