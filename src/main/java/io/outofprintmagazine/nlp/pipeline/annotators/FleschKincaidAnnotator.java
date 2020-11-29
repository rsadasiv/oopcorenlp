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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
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
import io.outofprintmagazine.nlp.pipeline.scorers.BigDecimalAvg;
import io.outofprintmagazine.nlp.pipeline.scorers.IScorer;
import io.outofprintmagazine.nlp.pipeline.serializers.BigDecimalSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.ISerializer;

public class FleschKincaidAnnotator extends AbstractPosAnnotator implements Annotator, IOOPAnnotator {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(FleschKincaidAnnotator.class);
	
	@SuppressWarnings("unused")
	private Logger getLogger() {
		return logger;
	}
	
	public FleschKincaidAnnotator() {
		super();
		this.setTags(Arrays.asList("NNP", "NNPS", "FW"));
		this.setScorer((IScorer)new BigDecimalAvg(this.getAnnotationClass()));
		this.setSerializer((ISerializer)new BigDecimalSerializer(this.getAnnotationClass()));
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPFleschKincaidAnnotation.class;
	}
	
	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(
			new ArraySet<>(
				Arrays.asList(
					CoreAnnotations.TextAnnotation.class, 
					CoreAnnotations.TokensAnnotation.class,
					CoreAnnotations.SentencesAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPSyllableCountAnnotation.class
				)
			)
		);
	}

	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		int wordCount = 0;
		int syllableCount = 0;
		int sentenceCount = 0;
		for (CoreSentence sentence : document.sentences()) {
			int sentenceWordCount = 0;
			int sentenceSyllableCount = 0;
			List<CoreLabel> tokens = sentence.tokens();
			boolean hasSyllable = false;
			for (int i = 0; i < tokens.size(); i++) {
				CoreLabel token = tokens.get(i);
				if (isDictionaryWord(token)) {
					if (token.containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPSyllableCountAnnotation.class)) {
						wordCount++;
						wordCount+=StringUtils.countMatches(token.lemma(), "-");
						sentenceWordCount++;
						sentenceWordCount+=StringUtils.countMatches(token.lemma(), "-");
						BigDecimal rawScore = (BigDecimal) token.get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPSyllableCountAnnotation.class);
						syllableCount += rawScore.intValue();
						sentenceSyllableCount += rawScore.intValue();
						hasSyllable = true;
					}
				}
			}
			if (hasSyllable) {
				sentenceCount++;
			}
			double fk = (206.835 - (1.015*sentenceWordCount) - (84.6*sentenceSyllableCount/sentenceWordCount))/100;
			try {
				sentence.coreMap().set(getAnnotationClass(), new BigDecimal(fk));
			}
			catch (NumberFormatException nfe) {
				sentence.coreMap().set(getAnnotationClass(), new BigDecimal(0));
			}
		}

		double fk = (206.835 - (1.015*wordCount/sentenceCount) - (84.6*syllableCount/wordCount))/100;
        document.annotation().set(getAnnotationClass(), new BigDecimal(fk));
	}

	@Override
	public String getDescription() {
		return "(206.835 - (1.015*wordCount/sentenceCount) - (84.6*syllableCount/wordCount))/100";
	}
	
	/*
	 * 
	 * 100.00–90.00	5th grade	Very easy to read. Easily understood by an average 11-year-old student.
	 * 90.0–80.0	6th grade	Easy to read. Conversational English for consumers.
	 * 80.0–70.0	7th grade	Fairly easy to read.
	 * 70.0–60.0	8th & 9th grade	Plain English. Easily understood by 13- to 15-year-old students.
	 * 60.0–50.0	10th to 12th grade	Fairly difficult to read.
	 * 50.0–30.0	College	Difficult to read.
	 * 30.0–0.0		College graduate	Very difficult to read. Best understood by university graduates.
	 */
}
