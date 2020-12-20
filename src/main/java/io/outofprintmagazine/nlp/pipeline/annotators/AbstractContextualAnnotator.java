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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flickr4java.flickr.FlickrException;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.TypedDependency;
import io.outofprintmagazine.nlp.pipeline.ContextualAnnotation;
import io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPWikipediaGlossAnnotation;
import io.outofprintmagazine.nlp.pipeline.PhraseAnnotation;
import io.outofprintmagazine.nlp.utils.BingUtils;
import io.outofprintmagazine.nlp.utils.CoreNlpUtils;
import io.outofprintmagazine.nlp.utils.FlickrUtils;
import io.outofprintmagazine.nlp.utils.WikimediaUtils;

/**
 * <p>Base class for custom annotators that work with dependency trees (Core Nlp depparse).</p>
 * @author Ram Sadasiv
 *
 */
public abstract class AbstractContextualAnnotator extends AbstractPosAnnotator implements Annotator, IOOPAnnotator {
	
	private static final Logger logger = LogManager.getLogger(AbstractContextualAnnotator.class);
	
	@SuppressWarnings("unused")
	private Logger getLogger() {
		return logger;
	}
	
	public AbstractContextualAnnotator() {
		super();
	}
		
	@SuppressWarnings("rawtypes")
	public abstract Class getEntityAnnotationClass();

	protected abstract ContextualAnnotation getConcreteAnnotation();
	
	@SuppressWarnings("rawtypes")
	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		Set<Class<? extends CoreAnnotation>> retval = new HashSet<Class<? extends CoreAnnotation>>();
		retval.addAll(
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
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPLocationsAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.VaderSentimentAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.CoreNlpSentimentAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.CoreNlpGenderAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPGenderAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbGroupsAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbsAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounsAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounGroupsAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdjectivesAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdverbsAnnotation.class
					
				)

		);
		return retval;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		List<ContextualAnnotation> scoreList = new ArrayList<ContextualAnnotation>();
		List<PhraseAnnotation> entities = (List<PhraseAnnotation>) document.annotation().get(getEntityAnnotationClass());
		for (PhraseAnnotation phraseAnnotation : entities) {
	    	ContextualAnnotation contextualAnnotation = getConcreteAnnotation();
	    	contextualAnnotation.setCanonicalName(phraseAnnotation.getName());
	    	try {
	    		scoreDocument(document, contextualAnnotation);
	    		for (CoreSentence sentence: document.sentences()) {
	    			if (sentence.coreMap().containsKey(getEntityAnnotationClass())) {
		    			List<PhraseAnnotation> sentencePeople = (List<PhraseAnnotation>) sentence.coreMap().get(getEntityAnnotationClass());
		    			for (PhraseAnnotation sentenceAnnotation : sentencePeople) {
			    			if (sentenceAnnotation.getName().equals(phraseAnnotation.getName())) {
			    				scoreSentence(document, sentence, contextualAnnotation);
			    			}
		    			}
		    			for (CoreLabel token : sentence.tokens()) {
		    				if (token.containsKey(getEntityAnnotationClass())) {
			    				List<PhraseAnnotation> tokenPeople = (List<PhraseAnnotation>) token.get(getEntityAnnotationClass());
			    				for (PhraseAnnotation tokenAnnotation : tokenPeople ) {
					    			if (tokenAnnotation.getName().equals(phraseAnnotation.getName())) {
					    				scoreToken(document, token, contextualAnnotation);
					    			}
				    			}
		    				}
		    			}
	    			}
	    		}
	    	}
	    	catch (Exception e) {
	    		e.printStackTrace();
	    		getLogger().error(e);
	    	}
	    	if (contextualAnnotation.getImportance().compareTo(new BigDecimal(1)) > 0) {
	    		contextualAnnotation.setVaderSentimentAvg();
	    		contextualAnnotation.setCoreNlpSentimentAvg();
	    		scoreList.add(contextualAnnotation);
	    	}
	    }
	    document.annotation().set(getAnnotationClass(), scoreList);
	}
	
	@Override
	public void score(CoreDocument document) {
		//pass
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void serialize(CoreDocument document, ObjectNode json) {
		if (document.annotation().containsKey(getAnnotationClass())) {
			ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
			mapper.configure(com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
			json.set(getAnnotationClass().getSimpleName(), mapper.valueToTree(document.annotation().get(getAnnotationClass())));
		}
	}
	
	@Override
	public void serializeAggregateDocument(CoreDocument document, ObjectNode json) {
		//TODO
	}
	
	protected void scoreDocument(CoreDocument document, ContextualAnnotation annotation) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, URISyntaxException, FlickrException {
		scoreThumbnails(annotation);
	}
	
	
	protected void scoreThumbnails(ContextualAnnotation annotation) throws IOException, URISyntaxException, FlickrException {
		if (getParameterStore().getProperty("azure_apiKey") != null) {
			annotation.getThumbnails().addAll(BingUtils.getInstance(getParameterStore()).getImagesByText(annotation.getCanonicalName()));
		}
		else if (getParameterStore().getProperty("flickr_apiKey") != null && getParameterStore().getProperty("faceplusplus_apiKey") != null) {
			annotation.getThumbnails().addAll(FlickrUtils.getInstance(getParameterStore()).getImagesByText(annotation.getCanonicalName()));
		}
		else {
			annotation.getThumbnails().addAll(WikimediaUtils.getInstance(getParameterStore()).getImagesByText(annotation.getCanonicalName()));
		}
		
	}
	
	
	protected void scoreSentence(CoreDocument document, CoreSentence sentence, ContextualAnnotation annotation) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		annotation.addImportance(1);
		
		BigDecimal sentenceId = new BigDecimal(CoreNlpUtils.getInstance(getParameterStore()).getSentenceIdFromSentence(document, sentence));
		if (annotation.getFirstAppearance() < 0 ) {
			annotation.setFirstAppearance(sentenceId.intValue());
		}
		if (annotation.getLastAppearance() < sentenceId.intValue()) {
			annotation.setLastAppearance(sentenceId.intValue());
		}
		
		annotation.addVaderSentiment(sentence.coreMap().get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.VaderSentimentAnnotation.class));
		annotation.addCoreNlpSentiment(sentence.coreMap().get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.CoreNlpSentimentAnnotation.class));

	}
	
	protected void scoreToken(CoreDocument document, CoreLabel token, ContextualAnnotation annotation) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		scoreDependencies(document, token, annotation);
		scoreWikipediaGloss(document, token, annotation);
	}

	
	protected void scoreWikipediaGloss(CoreDocument document, CoreLabel token, ContextualAnnotation annotation) {
		if (token.containsKey(OOPWikipediaGlossAnnotation.class)) {
			if (!annotation.getWikipediaGlosses().contains(token.get(OOPWikipediaGlossAnnotation.class))) {
				annotation.getWikipediaGlosses().add(token.get(OOPWikipediaGlossAnnotation.class));
			}
		}
	}
	
	protected void scoreSubAnnotation(Map<String, BigDecimal> annotationScore, Map<String, BigDecimal> existingScoreMap) {
		if (annotationScore != null) {
			for (String subAnnotationScoreName : annotationScore.keySet()) {
				BigDecimal existingScore = existingScoreMap.get(subAnnotationScoreName);
				if (existingScore == null) {
					existingScore = new BigDecimal(0);
				}
				existingScoreMap.put(subAnnotationScoreName, existingScore.add(annotationScore.get(subAnnotationScoreName)));
			}
		}
	}
	
	protected Map<String, BigDecimal> scoreVerbs(CoreLabel token, Map<String, BigDecimal> existingScoreMap) {
		if (token.containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbsAnnotation.class)) {
			Map<String,BigDecimal> annotationScore = token.get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbsAnnotation.class);
			scoreSubAnnotation(annotationScore, existingScoreMap);
		}
		return existingScoreMap;		
	}
	
	protected Map<String, BigDecimal> scoreVerbGroups(CoreLabel token, Map<String, BigDecimal> existingScoreMap) {
		if (token.containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbGroupsAnnotation.class)) {
			Map<String,BigDecimal> annotationScore = token.get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbGroupsAnnotation.class);
			scoreSubAnnotation(annotationScore, existingScoreMap);
		}
		return existingScoreMap;
	}
	
	protected Map<String, BigDecimal> scoreAdverbs(CoreLabel token, Map<String, BigDecimal> existingScoreMap) {
		if (token.containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdverbsAnnotation.class)) {
			Map<String,BigDecimal> annotationScore = token.get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdverbsAnnotation.class);
			scoreSubAnnotation(annotationScore, existingScoreMap);
		}
		return existingScoreMap;
	}
	
	protected Map<String, BigDecimal> scoreAdjectives(CoreLabel token, Map<String, BigDecimal> existingScoreMap) {
		if (token.containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdjectivesAnnotation.class)) {
			Map<String,BigDecimal> annotationScore = token.get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdjectivesAnnotation.class);
			scoreSubAnnotation(annotationScore, existingScoreMap);
		}
		return existingScoreMap;
	}
	
	protected Map<String, BigDecimal> scoreNouns(CoreLabel token, Map<String, BigDecimal> existingScoreMap) {
		if (token.containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounsAnnotation.class)) {
			Map<String,BigDecimal> annotationScore = token.get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounsAnnotation.class);
			scoreSubAnnotation(annotationScore, existingScoreMap);
		}
		return existingScoreMap;
	}
	
	protected Map<String, BigDecimal> scoreNounGroups(CoreLabel token, Map<String, BigDecimal> existingScoreMap) {
		if (token.containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounsAnnotation.class)) {
			Map<String,BigDecimal> annotationScore = token.get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounGroupsAnnotation.class);
			scoreSubAnnotation(annotationScore, existingScoreMap);
		}
		return existingScoreMap;
	}	
	
	protected void scoreDependencies(CoreDocument document, CoreLabel token, ContextualAnnotation annotation) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		scoreDependenciesGov(document, token, annotation);
		scoreDependenciesDep(document, token, annotation);
	}

	
	protected void scoreDependenciesGov(CoreDocument document, CoreLabel token, ContextualAnnotation annotation) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		List<TypedDependency> deps = CoreNlpUtils.getInstance(getParameterStore()).getTypedDependencyGovFromToken(document, token);
		for (TypedDependency dependency : deps) {
			GrammaticalRelation rn = dependency.reln();
			if (dependency.dep().backingLabel().containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounsAnnotation.class)) {
				annotation.getAttributes().put(
					"OOPNounsAnnotation",
					scoreNouns(
							dependency.dep().backingLabel(),
							annotation.getAttribute("OOPNounsAnnotation")
					)
				);
				annotation.getAttributes().put(
						"OOPNounGroupsAnnotation",
						scoreNounGroups(
								dependency.dep().backingLabel(),
								annotation.getAttribute("OOPNounGroupsAnnotation")
						)
					);
			}
			if (dependency.dep().backingLabel().containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdjectivesAnnotation.class)) {
				annotation.getAttributes().put(
					"OOPAdjectivesAnnotation",
					scoreAdjectives(
							dependency.dep().backingLabel(),
							annotation.getAttribute("OOPAdjectivesAnnotation")
					)
				);
			}
		}
	}
	
	
	protected void scoreDependenciesDep(CoreDocument document, CoreLabel token, ContextualAnnotation annotation) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		List<TypedDependency> deps = CoreNlpUtils.getInstance(getParameterStore()).getTypedDependencyDepFromToken(document, token);
		for (TypedDependency dependency : deps) {
			GrammaticalRelation rn = dependency.reln();
			if (rn.getShortName().equals("nsubj")) {
				if (Arrays.asList("VB","VBD","VBG","VBN","VBP","VBZ").contains(dependency.gov().tag())) {
					//Subject of transitive verb. John walks -> John : walk
					if (dependency.gov().backingLabel().containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbsAnnotation.class)) {
						annotation.getAttributes().put(
							"OOPVerbsAnnotation",
							scoreVerbs(
									dependency.gov().backingLabel(),
									annotation.getAttribute("OOPVerbsAnnotation")
							)
						);
					}
					if (dependency.gov().backingLabel().containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbGroupsAnnotation.class)) {
						annotation.getAttributes().put(
							"OOPVerbGroupsAnnotation",
							scoreVerbGroups(
									dependency.gov().backingLabel(),
									annotation.getAttribute("OOPVerbGroupsAnnotation")
							)
						);
					}

					
					List<TypedDependency> transitiveDeps = CoreNlpUtils.getInstance(getParameterStore()).getTypedDependencyGovFromToken(document, CoreNlpUtils.getInstance(getParameterStore()).getTokenFromIndexedWord(document, dependency.gov()));
    				for (TypedDependency transitiveDependency : transitiveDeps) {
    					GrammaticalRelation transitiveRelation = transitiveDependency.reln();
    					if (transitiveRelation.getShortName().equals("advmod")) {
    						//Adverbial description. John walks quickly -> John : quickly
    						if (transitiveDependency.dep().backingLabel().containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdverbsAnnotation.class)) {
    							annotation.getAttributes().put(
    								"OOPAdverbsAnnotation",
    								scoreAdverbs(
    										transitiveDependency.dep().backingLabel(),
    										annotation.getAttribute("OOPAdverbsAnnotation")
    								)
    							);
    						}
    					}
    				}
				}
				else {
					//Object of intransitive verb. John was angry -> John : angry
					if (dependency.gov().backingLabel().containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounsAnnotation.class)) {
						annotation.getAttributes().put(
							"OOPNounsAnnotation",
							scoreNouns(
									dependency.gov().backingLabel(),
									annotation.getAttribute("OOPNounsAnnotation")
							)
						);
						annotation.getAttributes().put(
								"OOPNounGroupsAnnotation",
								scoreNounGroups(
										dependency.gov().backingLabel(),
										annotation.getAttribute("OOPNounGroupsAnnotation")
								)
							);
					}
					//Subject of intransitive verb. John was angry -> John : angry
					if (dependency.gov().backingLabel().containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdjectivesAnnotation.class)) {
						annotation.getAttributes().put(
							"OOPAdjectivesAnnotation",
							scoreAdjectives(
									dependency.gov().backingLabel(),
									annotation.getAttribute("OOPAdjectivesAnnotation")
							)
						);
					}
				}
			}
			
			if (rn.getShortName().equals("nmod:poss")) {
				//Possessive. John's hair -> John : hair.
				if (dependency.gov().backingLabel().containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounsAnnotation.class)) {
					annotation.getAttributes().put(
						"OOPNounsAnnotation",
						scoreNouns(
								dependency.gov().backingLabel(),
								annotation.getAttribute("OOPNounsAnnotation")
						)
					);
					annotation.getAttributes().put(
							"OOPNounGroupsAnnotation",
							scoreNounGroups(
									dependency.gov().backingLabel(),
									annotation.getAttribute("OOPNounGroupsAnnotation")
							)
						);
				};
				List<TypedDependency> transitiveDeps = CoreNlpUtils.getInstance(getParameterStore()).getTypedDependencyDepFromToken(document, CoreNlpUtils.getInstance(getParameterStore()).getTokenFromIndexedWord(document, dependency.gov()));
				for (TypedDependency transitiveDependency : transitiveDeps) {
					GrammaticalRelation transitiveRelation = transitiveDependency.reln();
					if (transitiveRelation.getShortName().equals("amod")) {
						//Possessive modifier.  John's black hair -> John : black
						if (dependency.gov().backingLabel().containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounsAnnotation.class)) {
							annotation.getAttributes().put(
								"OOPAdjectivesAnnotation",
								scoreAdjectives(
										dependency.gov().backingLabel(),
										annotation.getAttribute("OOPAdjectivesAnnotation")
								)
							);
						}
					}
				}
			}
		}
	}

}
