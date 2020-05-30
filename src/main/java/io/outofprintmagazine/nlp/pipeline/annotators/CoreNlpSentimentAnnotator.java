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
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.pipeline.scorers.BigDecimalAvg;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.BigDecimalSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public class CoreNlpSentimentAnnotator extends AbstractAnnotator implements Annotator, OOPAnnotator {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(CoreNlpSentimentAnnotator.class);
	
	public CoreNlpSentimentAnnotator() {
		super();
		this.setScorer((Scorer)new BigDecimalAvg(this.getAnnotationClass()));
		this.setSerializer((Serializer)new BigDecimalSerializer(this.getAnnotationClass()));
	}
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			String sentiment = sentence.sentiment();
			BigDecimal score = new BigDecimal(.5);
			if (sentiment.equals("Very negative")) {
				score = new BigDecimal(0);
			}
			else if (sentiment.equals("Negative")) {
				score = new BigDecimal(.25);
			}
			else if (sentiment.equals("Neutral")) {
				score = new BigDecimal(.5);
			}
			else if (sentiment.equals("Positive")) {
				score = new BigDecimal(.75);
			}
			else if (sentiment.equals("Very positive")) {
				score = new BigDecimal(1);
			}
			sentence.coreMap().set(getAnnotationClass(), score);
			for (CoreLabel token : sentence.tokens()) {
				if (token.containsKey(getAnnotationClass())) {
					token.set(getAnnotationClass(), score);
				}
			}
		}
		score(document);
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.CoreNlpSentimentAnnotation.class;
	}
	
	/*requirementsSatisfied() on SentimentAnnotator is broken. 
	 * public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
     * 	return Collections.emptySet();
  	 * }
	 * Set this to run after some more expensive annotators and hope for the best
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(
			new ArraySet<>(
				//Arrays.asList(edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentClass.class)
					Arrays.asList(
							CoreAnnotations.TextAnnotation.class, 
							CoreAnnotations.TokensAnnotation.class,
							CoreAnnotations.LemmaAnnotation.class,
							CoreAnnotations.SentencesAnnotation.class,
							CoreAnnotations.NamedEntityTagAnnotation.class,
							CoreAnnotations.NormalizedNamedEntityTagAnnotation.class,
							CoreAnnotations.CanonicalEntityMentionIndexAnnotation.class,
							CorefCoreAnnotations.CorefChainAnnotation.class,
							SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class
					)
			)
		);
	  }

	@Override
	public String getDescription() {
		return "edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentClass scaled from 0 to 1";
	}
	
	@Override
	public void serializeAggregateDocument(CoreDocument document, ObjectNode json) {
		getSerializer().serializeAggregate(getScorer().aggregateDocument(document), json);
	}

}
