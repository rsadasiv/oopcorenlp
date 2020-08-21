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
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.flickr4java.flickr.FlickrException;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreQuote;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.pipeline.ActorAnnotation;
import io.outofprintmagazine.nlp.pipeline.ContextualAnnotation;
import io.outofprintmagazine.nlp.utils.BingUtils;
import io.outofprintmagazine.nlp.utils.FlickrUtils;
import io.outofprintmagazine.nlp.utils.WikimediaUtils;

public class ActorsAnnotator extends AbstractContextualAnnotator implements Annotator, IOOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(ActorsAnnotator.class);
	
	@SuppressWarnings("unused")
	private Logger getLogger() {
		return logger;
	}

	@Override
	protected ContextualAnnotation getConcreteAnnotation() {
		return new ActorAnnotation();
	}
	
	public ActorsAnnotator() {
		super();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(
			new ArraySet<>(
				Arrays.asList(
					CoreAnnotations.TextAnnotation.class, 
					CoreAnnotations.TokensAnnotation.class,
					CoreAnnotations.LemmaAnnotation.class,
					CoreAnnotations.SentencesAnnotation.class,
					CoreAnnotations.NamedEntityTagAnnotation.class,
					CoreAnnotations.NormalizedNamedEntityTagAnnotation.class,
					CoreAnnotations.CanonicalEntityMentionIndexAnnotation.class,
					CorefCoreAnnotations.CorefChainAnnotation.class,
					SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class,
					CoreAnnotations.QuotationsAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPPeopleAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.VaderSentimentAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.CoreNlpSentimentAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.CoreNlpGenderAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPGenderAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbGroupsAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbsAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounsAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdjectivesAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdverbsAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPMyersBriggsAnnotation.class
					
				)
			)
		);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPActorsAnnotation.class;
	}
	
	@Override
	public Class getEntityAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPPeopleAnnotation.class;
	}

	
	@Override
	protected void scoreDocument(CoreDocument document, ContextualAnnotation annotation) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, URISyntaxException, FlickrException {
		super.scoreDocument(document, annotation);
		scoreQuotes(document, annotation.getCanonicalName(), (ActorAnnotation)annotation);
	}
	
	@Override
	protected void scoreToken(CoreDocument document, CoreLabel token, ContextualAnnotation annotation) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		super.scoreToken(document, token, annotation);
		scoreOOPGender(token, (ActorAnnotation)annotation);
		scoreCoreGender(token, (ActorAnnotation)annotation);
		
	}
	
	
	protected void scoreQuotes(CoreDocument document, String canonicalEntityName, ActorAnnotation characterAnnotation) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		for (CoreQuote quote : document.quotes()) {
			if (quote.hasCanonicalSpeaker) {
				if (quote.canonicalSpeaker().get().equals(canonicalEntityName)) {
					characterAnnotation.getQuotes().add(quote.text());
				}
			}
		}
	}
	
	protected void scoreCoreGender(CoreLabel token, ActorAnnotation characterAnnotation) {
		if (token.containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.CoreNlpGenderAnnotation.class)) {
			for (String gender : token.get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.CoreNlpGenderAnnotation.class).keySet())
				characterAnnotation.setCoreNlpGender(gender);
		}		
	}
	
	protected void scoreOOPGender(CoreLabel token, ActorAnnotation characterAnnotation) {
		if (token.containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPGenderAnnotation.class)) {
			for (String gender : token.get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPGenderAnnotation.class).keySet())
				characterAnnotation.setOOPGender(gender);
		}		
	}
	
	@Override
	protected void scoreThumbnails(ContextualAnnotation actor) throws IOException, URISyntaxException, FlickrException {
		String characterName = actor.getCanonicalName();
		if (!characterName.equalsIgnoreCase("I")) {

			String[] names = characterName.split(" ");
			if (names.length > 1) {
				if (names[0].startsWith("Mr") || names[0].startsWith("Mrs") || names[0].startsWith("Dr")) {
					characterName = names[1];
				}
				else {
					characterName = names[0];
				}
			}
			if (getParameterStore().getProperty("azure_apiKey") != null) {
				actor.getThumbnails().addAll(BingUtils.getInstance(getParameterStore()).getImagesByText(characterName));
			}
			else if (getParameterStore().getProperty("flickr_apiKey") != null && getParameterStore().getProperty("faceplusplus_apiKey") != null) {
				actor.getThumbnails().addAll(FlickrUtils.getInstance(getParameterStore()).getFacesByText(characterName));
			}
			else {
				actor.getThumbnails().addAll(WikimediaUtils.getInstance(getParameterStore()).getImagesByText(characterName));
			}
		}		
	}
	
	
	protected void scoreSentence(CoreDocument document, CoreSentence sentence, ActorAnnotation characterAnnotation) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		super.scoreSentence(document, sentence, (ContextualAnnotation) characterAnnotation);
		scoreMyersBriggs(sentence, characterAnnotation);
	}
	
	
	protected void scoreMyersBriggs(CoreSentence sentence, ActorAnnotation characterAnnotation ) {
		if (sentence.coreMap().containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPMyersBriggsAnnotation.class)) {
			Map<String,BigDecimal> annotationScore = sentence.coreMap().get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPMyersBriggsAnnotation.class);
			if (annotationScore.containsKey("extrovert")) {
				BigDecimal existingScore = new BigDecimal(0);
				if (characterAnnotation.getOOPMyersBriggs().containsKey("extrovert")) {
					existingScore = characterAnnotation.getOOPMyersBriggs().get("extrovert");
				}
				characterAnnotation.getOOPMyersBriggs().put("extrovert", existingScore.add(annotationScore.get("extrovert")));
			}
			if (annotationScore.containsKey("introvert")) {
				BigDecimal existingScore = new BigDecimal(0);
				if (characterAnnotation.getOOPMyersBriggs().containsKey("introvert")) {
					existingScore = characterAnnotation.getOOPMyersBriggs().get("introvert");
				}
				characterAnnotation.getOOPMyersBriggs().put("introvert", existingScore.add(annotationScore.get("introvert")));
			}
			if (annotationScore.containsKey("sensing")) {
				BigDecimal existingScore = new BigDecimal(0);
				if (characterAnnotation.getOOPMyersBriggs().containsKey("sensing")) {
					existingScore = characterAnnotation.getOOPMyersBriggs().get("sensing");
				}
				characterAnnotation.getOOPMyersBriggs().put("sensing", existingScore.add(annotationScore.get("sensing")));
			}
			if (annotationScore.containsKey("intuitive")) {
				BigDecimal existingScore = new BigDecimal(0);
				if (characterAnnotation.getOOPMyersBriggs().containsKey("intuitive")) {
					existingScore = characterAnnotation.getOOPMyersBriggs().get("intuitive");
				}
				characterAnnotation.getOOPMyersBriggs().put("intuitive", existingScore.add(annotationScore.get("intuitive")));
			}
			if (annotationScore.containsKey("thinking")) {
				BigDecimal existingScore = new BigDecimal(0);
				if (characterAnnotation.getOOPMyersBriggs().containsKey("thinking")) {
					existingScore = characterAnnotation.getOOPMyersBriggs().get("thinking");
				}
				characterAnnotation.getOOPMyersBriggs().put("thinking", existingScore.add(annotationScore.get("thinking")));
			}
			if (annotationScore.containsKey("feeling")) {
				BigDecimal existingScore = new BigDecimal(0);
				if (characterAnnotation.getOOPMyersBriggs().containsKey("feeling")) {
					existingScore = characterAnnotation.getOOPMyersBriggs().get("feeling");
				}
				characterAnnotation.getOOPMyersBriggs().put("feeling", existingScore.add(annotationScore.get("feeling")));
			}
			if (annotationScore.containsKey("judging")) {
				BigDecimal existingScore = new BigDecimal(0);
				if (characterAnnotation.getOOPMyersBriggs().containsKey("judging")) {
					existingScore = characterAnnotation.getOOPMyersBriggs().get("judging");
				}
				characterAnnotation.getOOPMyersBriggs().put("judging", existingScore.add(annotationScore.get("judging")));
			}
			if (annotationScore.containsKey("perceiving")) {
				BigDecimal existingScore = new BigDecimal(0);
				if (characterAnnotation.getOOPMyersBriggs().containsKey("perceiving")) {
					existingScore = characterAnnotation.getOOPMyersBriggs().get("perceiving");
				}
				characterAnnotation.getOOPMyersBriggs().put("perceiving", existingScore.add(annotationScore.get("perceiving")));
			}
		}
	}
	

	@Override
	public String getDescription() {
		return "OOPPeopleAnnotation with sentiment, quotes, coref, and universal dependencies nsubj, nmod:poss, advmod, amod (http://universaldependencies.org/docsv1/en/dep/index.html)." ;
	}

}
