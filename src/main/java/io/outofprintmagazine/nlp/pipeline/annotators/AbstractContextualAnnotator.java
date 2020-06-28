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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.pipeline.ContextualAnnotation;
import io.outofprintmagazine.nlp.utils.BingUtils;
import io.outofprintmagazine.nlp.utils.CoreNlpUtils;

/**
 * <p>Base class for custom annotators that work with dependency trees (Core Nlp depparse).</p>
 * @author Ram Sadasiv
 *
 */
public abstract class AbstractContextualAnnotator extends AbstractPosAnnotator implements Annotator, OOPAnnotator {
	
	private static final Logger logger = LogManager.getLogger(AbstractContextualAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}
	
	public AbstractContextualAnnotator() {
		super();
	}
		
	public abstract Class getEntityAnnotationClass();

	protected abstract ContextualAnnotation getConcreteAnnotation();
	
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
			)
		);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		Map<String,ContextualAnnotation> scoreMap = new HashMap<String,ContextualAnnotation>();
		Map<String,BigDecimal> entities = (Map<String, BigDecimal>) document.annotation().get(getEntityAnnotationClass());
	    for (String canonicalEntityName : entities.keySet()) {
	    	ContextualAnnotation contextualAnnotation = getConcreteAnnotation();
	    	contextualAnnotation.setCanonicalName(canonicalEntityName);
	    	try {
	    		for (CoreSentence sentence: document.sentences()) {
	    			Map<String,BigDecimal> sentencePeople = (Map<String, BigDecimal>) sentence.coreMap().get(getEntityAnnotationClass());
	    			if (sentencePeople != null && sentencePeople.get(canonicalEntityName) != null) {
	    				scoreSentence(document, sentence, contextualAnnotation);
	    			}
	    			for (CoreLabel token : sentence.tokens()) {
		    			Map<String,BigDecimal> tokenPeople = (Map<String, BigDecimal>) token.get(getEntityAnnotationClass());
		    			if (tokenPeople != null && tokenPeople.get(canonicalEntityName) != null) {
		    				scoreToken(document, token, contextualAnnotation);
		    			}
	    			}
	    		}
		    	scoreThumbnails(contextualAnnotation);

	    	}
	    	catch (Exception e) {
	    		ByteArrayOutputStream os = new ByteArrayOutputStream();
	    		PrintStream ps = new PrintStream(os);
	    		e.printStackTrace(ps);
	    		try {
					getLogger().error(os.toString("UTF8"));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
	    	}
	    	if (contextualAnnotation.getImportance().compareTo(new BigDecimal(1)) > 0) {
	    		contextualAnnotation.setVaderSentimentAvg();
	    		contextualAnnotation.setCoreNlpSentimentAvg();
	    		scoreMap.put(toAlphaNumeric(canonicalEntityName), contextualAnnotation);
	    	}
	    }
	    document.annotation().set(getAnnotationClass(), scoreMap);
	}
	
	@Override
	public void score(CoreDocument document) {
		//pass
	}
	
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
	
	
	protected void scoreThumbnails(ContextualAnnotation annotation) throws IOException, URISyntaxException {
		annotation.getThumbnails().addAll(BingUtils.getInstance(getParameterStore()).getImagesByText(annotation.getCanonicalName()));		
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
	
	protected void scoreToken(CoreDocument document, CoreLabel token, ContextualAnnotation annotation) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
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
					};
					if (dependency.gov().backingLabel().containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbGroupsAnnotation.class)) {
						annotation.getAttributes().put(
							"OOPVerbGroupsAnnotation",
							scoreVerbGroups(
									dependency.gov().backingLabel(),
									annotation.getAttribute("OOPVerbGroupsAnnotation")
							)
						);
					};

					
					List<TypedDependency> transitiveDeps = CoreNlpUtils.getInstance(getParameterStore()).getTypedDependencyDepFromToken(document, CoreNlpUtils.getInstance(getParameterStore()).getTokenFromIndexedWord(document, dependency.gov()));
    				for (TypedDependency transitiveDependency : transitiveDeps) {
    					GrammaticalRelation transitiveRelation = transitiveDependency.reln();
    					if (transitiveRelation.getShortName().equals("advmod")) {
    						//Adverbial description. John walks quickly -> John : quickly
    						if (dependency.gov().backingLabel().containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdverbsAnnotation.class)) {
    							annotation.getAttributes().put(
    								"OOPAdverbsAnnotation",
    								scoreAdverbs(
    										dependency.gov().backingLabel(),
    										annotation.getAttribute("OOPAdverbsAnnotation")
    								)
    							);
    						};
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
					};
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
						};
					}
				}
			}
		}
	}

}
