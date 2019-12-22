package io.outofprintmagazine.nlp.pipeline.annotators;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
import edu.stanford.nlp.pipeline.CoreQuote;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.CoreNlpUtils;
import io.outofprintmagazine.util.ActorAnnotation;

public class ActorsAnnotator extends AbstractPosAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(ActorsAnnotator.class);
	
	public ActorsAnnotator() {
		super();
		this.appendTagsFromFile("io/outofprintmagazine/nlp/models/StativeVerbs.txt");
	}
	
	public ActorsAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
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
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdverbsAnnotation.class
					
				)
			)
		);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPActorsAnnotation.class;
	}
	
	protected Class getPeopleAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPPeopleAnnotation.class;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		Map<String,ActorAnnotation> scoreMap = new HashMap<String,ActorAnnotation>();
		Map<String,BigDecimal> people = (Map<String, BigDecimal>) document.annotation().get(getPeopleAnnotationClass());
	    for (String canonicalEntityName : people.keySet()) {
	    	ActorAnnotation characterAnnotation = new ActorAnnotation();
	    	characterAnnotation.setCanonicalName(canonicalEntityName);
	    	try {
	    		for (CoreSentence sentence: document.sentences()) {
	    			Map<String,BigDecimal> sentencePeople = (Map<String, BigDecimal>) sentence.coreMap().get(getPeopleAnnotationClass());
	    			if (sentencePeople != null && sentencePeople.get(canonicalEntityName) != null) {
	    				scoreSentence(document, sentence, characterAnnotation);
	    			}
	    			for (CoreLabel token : sentence.tokens()) {
		    			Map<String,BigDecimal> tokenPeople = (Map<String, BigDecimal>) token.get(getPeopleAnnotationClass());
		    			if (tokenPeople != null && tokenPeople.get(canonicalEntityName) != null) {
		    				scoreToken(document, token, characterAnnotation);
		    				scoreOOPGender(token, characterAnnotation);
		    				scoreCoreGender(token, characterAnnotation);
		    			}
	    			}
	    		}
		    	scoreQuotes(document, canonicalEntityName, characterAnnotation);
	    	}
	    	catch (Exception e) {
	    		ByteArrayOutputStream os = new ByteArrayOutputStream();
	    		PrintStream ps = new PrintStream(os);
	    		e.printStackTrace(ps);
	    		try {
					logger.error(os.toString("UTF8"));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
	    	}
	    	scoreMap.put(canonicalEntityName, characterAnnotation);
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
			json.set(getAnnotationClass().getName(), mapper.valueToTree(document.annotation().get(getAnnotationClass())));
		}
	}
	
	private void scoreQuotes(CoreDocument document, String canonicalEntityName, ActorAnnotation characterAnnotation) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		for (CoreQuote quote : document.quotes()) {
			if (quote.hasCanonicalSpeaker) {
				if (quote.canonicalSpeaker().get().equals(canonicalEntityName)) {
					characterAnnotation.getQuotes().add(quote.text());
				}
			}
		}
	}
	
	private void scoreCoreGender(CoreLabel token, ActorAnnotation characterAnnotation) {
		if (token.containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.CoreNlpGenderAnnotation.class)) {
			for (String gender : token.get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.CoreNlpGenderAnnotation.class).keySet())
				characterAnnotation.setCoreNlpGender(gender);
		}		
	}
	
	private void scoreOOPGender(CoreLabel token, ActorAnnotation characterAnnotation) {
		if (token.containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPGenderAnnotation.class)) {
			for (String gender : token.get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPGenderAnnotation.class).keySet())
				characterAnnotation.setOOPGender(gender);
		}		
	}
	
/*	private void scoreCoref(CoreDocument document, String canonicalEntityName, ActorAnnotation characterAnnotation) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
	    for (CorefChain corefChain : document.annotation().get(CorefCoreAnnotations.CorefChainAnnotation.class).values()) {
	    	if (CoreNlpUtils.getInstance().isCorefChainForCanonicalEntityName(document, corefChain, canonicalEntityName)) {
	    		for (CorefMention corefMention : corefChain.getMentionsInTextualOrder() ) {
	    			CoreSentence sentence = CoreNlpUtils.getInstance().getSentenceFromCorefMention(document, corefMention);
	    			scoreSentence(document, sentence, characterAnnotation);
	    			
	    			List<CoreLabel> corefTokens = CoreNlpUtils.getInstance().getTokensFromCorefMention(document, corefMention);
	    			for (CoreLabel token : corefTokens) {
	    				scoreToken(document, token, characterAnnotation);
	    			}
	    		}
	    	}
	    }		
	}
	
	private void scoreEntities(CoreDocument document, String canonicalEntityName, ActorAnnotation characterAnnotation) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		for (int sentenceIdx=0;sentenceIdx<document.sentences().size();sentenceIdx++) {
			CoreSentence sentence = document.sentences().get(sentenceIdx);
			List<CoreEntityMention> entityMentions = sentence.entityMentions();
			for (CoreEntityMention entityMention : entityMentions) {
		    	if (CoreNlpUtils.getInstance().isCoreEntityMentionForCanonicalEntityName(document, entityMention, canonicalEntityName)) {
		    		if (entityMention.entityType().equals("PERSON") && !CoreNlpUtils.getInstance().isCoreEntityMentionInAnyCorefChain(document, entityMention)) {
			    		characterAnnotation.setCoreNlpGender(CoreNlpUtils.getInstance().getCoreNlpGenderNameFromCoreEntityMention(document, entityMention));
			    		characterAnnotation.setOOPGender(CoreNlpUtils.getInstance().getOOPGenderNameFromCoreEntityMention(document, entityMention));
			    		scoreSentence(document, sentence, characterAnnotation);
		    			List<CoreLabel> coreEntityTokens = CoreNlpUtils.getInstance().getTokensFromCoreEntityMention(document, entityMention);
		    			for (CoreLabel token : coreEntityTokens) {
			    			scoreToken(document, token, characterAnnotation);
			    		}
			    	}
				}
			}
		}
				
	}*/
	
	private void scoreSentence(CoreDocument document, CoreSentence sentence, ActorAnnotation characterAnnotation) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		characterAnnotation.addImportance(1);
		
		BigDecimal sentenceId = new BigDecimal(CoreNlpUtils.getInstance().getSentenceIdFromSentence(document, sentence));
		if (characterAnnotation.getFirstAppearance() < 0 || sentenceId.intValue() < characterAnnotation.getFirstAppearance()) {
			characterAnnotation.setFirstAppearance(sentenceId.intValue());
		}
		if (characterAnnotation.getLastAppearance() < sentenceId.intValue()) {
			characterAnnotation.setLastAppearance(sentenceId.intValue());
		}
		
		characterAnnotation.addVaderSentiment(sentence.coreMap().get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.VaderSentimentAnnotation.class));
		characterAnnotation.addCoreNlpSentiment(sentence.coreMap().get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.CoreNlpSentimentAnnotation.class));
	}
	
	private void scoreSubAnnotation(Map<String, BigDecimal> annotationScore, Map<String, BigDecimal> existingScoreMap) {
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
	
	private Map<String, BigDecimal> scoreVerbs(CoreLabel token, Map<String, BigDecimal> existingScoreMap) {
		if (token.containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbsAnnotation.class)) {
			Map<String,BigDecimal> annotationScore = token.get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbsAnnotation.class);
			scoreSubAnnotation(annotationScore, existingScoreMap);
		}
		return existingScoreMap;		
	}
	
	private Map<String, BigDecimal> scoreVerbGroups(CoreLabel token, Map<String, BigDecimal> existingScoreMap) {
		if (token.containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbGroupsAnnotation.class)) {
			Map<String,BigDecimal> annotationScore = token.get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbGroupsAnnotation.class);
			scoreSubAnnotation(annotationScore, existingScoreMap);
		}
		return existingScoreMap;
	}
	
	private Map<String, BigDecimal> scoreAdverbs(CoreLabel token, Map<String, BigDecimal> existingScoreMap) {
		if (token.containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdverbsAnnotation.class)) {
			Map<String,BigDecimal> annotationScore = token.get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdverbsAnnotation.class);
			scoreSubAnnotation(annotationScore, existingScoreMap);
		}
		return existingScoreMap;
	}
	
	private Map<String, BigDecimal> scoreAdjectives(CoreLabel token, Map<String, BigDecimal> existingScoreMap) {
		if (token.containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdjectivesAnnotation.class)) {
			Map<String,BigDecimal> annotationScore = token.get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdjectivesAnnotation.class);
			scoreSubAnnotation(annotationScore, existingScoreMap);
		}
		return existingScoreMap;
	}
	
	private Map<String, BigDecimal> scoreNouns(CoreLabel token, Map<String, BigDecimal> existingScoreMap) {
		if (token.containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounsAnnotation.class)) {
			Map<String,BigDecimal> annotationScore = token.get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounsAnnotation.class);
			scoreSubAnnotation(annotationScore, existingScoreMap);
		}
		return existingScoreMap;
	}
	
	private void scoreToken(CoreDocument document, CoreLabel token, ActorAnnotation characterAnnotation) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		List<TypedDependency> deps = CoreNlpUtils.getInstance().getTypedDependencyDepFromToken(document, token);
		for (TypedDependency dependency : deps) {
			GrammaticalRelation rn = dependency.reln();
			if (rn.getShortName().equals("nsubj")) {
				if (Arrays.asList("VB","VBD","VBG","VBN","VBP","VBZ").contains(dependency.gov().tag())) {
					//Subject of transitive verb. John walks -> John : walk
					if (dependency.gov().backingLabel().containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbsAnnotation.class)) {
						characterAnnotation.getAttributes().put(
							"io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbsAnnotation",
							scoreVerbs(
									dependency.gov().backingLabel(),
									characterAnnotation.getAttribute("io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbsAnnotation")
							)
						);
					};
					if (dependency.gov().backingLabel().containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbGroupsAnnotation.class)) {
						characterAnnotation.getAttributes().put(
							"io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbGroupsAnnotation",
							scoreVerbGroups(
									dependency.gov().backingLabel(),
									characterAnnotation.getAttribute("io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbGroupsAnnotation")
							)
						);
					};

					
					List<TypedDependency> transitiveDeps = CoreNlpUtils.getInstance().getTypedDependencyDepFromToken(document, CoreNlpUtils.getInstance().getTokenFromIndexedWord(document, dependency.gov()));
    				for (TypedDependency transitiveDependency : transitiveDeps) {
    					GrammaticalRelation transitiveRelation = transitiveDependency.reln();
    					if (transitiveRelation.getShortName().equals("advmod")) {
    						//Adverbial description. John walks quickly -> John : quickly
    						if (dependency.gov().backingLabel().containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdverbsAnnotation.class)) {
    							characterAnnotation.getAttributes().put(
    								"io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdverbsAnnotation",
    								scoreAdverbs(
    										dependency.gov().backingLabel(),
    										characterAnnotation.getAttribute("io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdverbsAnnotation")
    								)
    							);
    						};
    					}
    				}
				}
				else {
					//Object of intransitive verb. John was angry -> John : angry
					if (dependency.gov().backingLabel().containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounsAnnotation.class)) {
						characterAnnotation.getAttributes().put(
							"io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounsAnnotation",
							scoreNouns(
									dependency.gov().backingLabel(),
									characterAnnotation.getAttribute("io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounsAnnotation")
							)
						);
					};
				}
			}
			if (rn.getShortName().equals("nmod:poss")) {
				//Possessive. John's hair -> John : hair.
				if (dependency.gov().backingLabel().containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounsAnnotation.class)) {
					characterAnnotation.getAttributes().put(
						"io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounsAnnotation",
						scoreNouns(
								dependency.gov().backingLabel(),
								characterAnnotation.getAttribute("io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounsAnnotation")
						)
					);
				};
				List<TypedDependency> transitiveDeps = CoreNlpUtils.getInstance().getTypedDependencyDepFromToken(document, CoreNlpUtils.getInstance().getTokenFromIndexedWord(document, dependency.gov()));
				for (TypedDependency transitiveDependency : transitiveDeps) {
					GrammaticalRelation transitiveRelation = transitiveDependency.reln();
					if (transitiveRelation.getShortName().equals("amod")) {
						//Possessive modifier.  John's black hair -> John : black
						if (dependency.gov().backingLabel().containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounsAnnotation.class)) {
							characterAnnotation.getAttributes().put(
								"io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdjectivesAnnotation",
								scoreAdjectives(
										dependency.gov().backingLabel(),
										characterAnnotation.getAttribute("io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdjectivesAnnotation")
								)
							);
						};
					}
				}
			}
		}
	}

	@Override
	public String getDescription() {
		return "OOPPeopleAnnotation with sentiment, quotes, coref, and universal dependencies nsubj, nmod:poss, advmod, amod (http://universaldependencies.org/docsv1/en/dep/index.html)." ;
	}
}
