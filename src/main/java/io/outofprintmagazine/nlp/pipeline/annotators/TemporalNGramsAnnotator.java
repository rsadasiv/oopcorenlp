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
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.IScorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.ISerializer;
import io.outofprintmagazine.nlp.utils.NGramUtils;
import io.outofprintmagazine.nlp.utils.NGramUtils.NGramPhraseScore;
import io.outofprintmagazine.nlp.utils.NGramUtils.NGramScore;

public class TemporalNGramsAnnotator extends AbstractPosAnnotator implements Annotator, IOOPAnnotator{

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(TemporalNGramsAnnotator.class);

	@SuppressWarnings("unused")
	private Logger getLogger() {
		return logger;
	}
	
	public TemporalNGramsAnnotator() {
		super();
		this.setScorer((IScorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((ISerializer)new MapSerializer(this.getAnnotationClass()));
		this.appendTagsFromFile("io/outofprintmagazine/nlp/models/COCA/Dolch.txt");
	}
	
    public Map<String,BigDecimal> nGramScoreToTemporalAnnotation(NGramScore nGramScore) {
    	Map<String,BigDecimal> retval = new HashMap<String,BigDecimal>();
    	retval.put(nGramScoreToTemporalString(nGramScore), new BigDecimal(1));
    	return retval;
    }
    
    public String nGramScoreToTemporalString(NGramScore nGramScore) {
    	return nGramPhraseScoreToTemporalString(nGramScore.match);
    }
    
    public String nGramPhraseScoreToTemporalString(NGramPhraseScore nGramPhraseScore) {
		if (nGramPhraseScore == null) {
			return "novel";
		}
		else if (nGramPhraseScore.lastYear < 1900) {
			return "archaic";
		}
		else if (nGramPhraseScore.lastYear < 1950) {
			return "dated";
		}
		else if (nGramPhraseScore.firstYear > 1999) {
			return "millenial";
		}
		else if (nGramPhraseScore.firstYear > 1899) {
			return "modern";
		}
		else if (nGramPhraseScore.firstYear > 1799) {
			return "victorian";
		}
		else if (nGramPhraseScore.firstYear > 1699) {
			return "enlightenment";
		}
		else if (nGramPhraseScore.firstYear > 1599) {
			return "elizabethan";
		}
		else {
			return "core";
		}
    }
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		List<String> queries = new ArrayList<String>();
		for (int i=0;i<document.tokens().size();i++) {
			CoreLabel token = document.tokens().get(i);
			if (isDictionaryWord(token)) {
				if (!getTags().contains(token.lemma())) {
					queries.add(token.originalText().toLowerCase());
				}
			}
		}
		try {
			NGramUtils.getInstance(getParameterStore()).addToWordCache(queries);
			for (int i=0;i<document.tokens().size();i++) {
				CoreLabel token = document.tokens().get(i);
				try {
					if (isDictionaryWord(token)) {
						if (!getTags().contains(token.lemma())) {
							List<NGramPhraseScore> wordCache = NGramUtils.getInstance(getParameterStore()).getWordCache(token.originalText().toLowerCase());
							if (wordCache != null && wordCache.size() > 0) {
								String key = nGramPhraseScoreToTemporalString(wordCache.get(0));
								Map<String,BigDecimal> scoreMap = new HashMap<String,BigDecimal>();
								addToScoreMap(scoreMap, key, new BigDecimal(1));
								token.set(getAnnotationClass(), scoreMap);
							}
						}
					}
				}
				catch (Throwable t) {
					logger.error("wtf?", t);
				}
			}
			NGramUtils.getInstance(getParameterStore()).pruneWordCache();
		}
		catch (Exception e) {
			logger.error(e);
		}
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPTemporalNGramsAnnotation.class;
	}

	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(
			new ArraySet<>(
				Arrays.asList(
					CoreAnnotations.TextAnnotation.class, 
					CoreAnnotations.TokensAnnotation.class,
					CoreAnnotations.SentencesAnnotation.class
				)
			)
		);
	}

	@Override
	public String getDescription() {
		return "Google nGram first/last recorded date of token. "
				+ "not found: novel, "
				+ "last < 1900: archaic, "
				+ "last < 1950: dated, "
				+ "first > 1999: millenial, "
				+ "first > 1899: modern, "
				+ "first > 1799: victorian, "
				+ "first > 1699: enlightenment, "
				+ "first > 1599: elizabethan, "
				+ "else: core";
	}

}
