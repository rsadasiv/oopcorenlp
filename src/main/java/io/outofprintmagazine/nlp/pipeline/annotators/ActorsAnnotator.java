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
import io.outofprintmagazine.nlp.BingUtils;
import io.outofprintmagazine.nlp.CoreNlpUtils;
import io.outofprintmagazine.nlp.pipeline.ActorAnnotation;
import io.outofprintmagazine.nlp.pipeline.ContextualAnnotation;

public class ActorsAnnotator extends AbstractContextualAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(ActorsAnnotator.class);
	
	@Override
	public Logger getLogger() {
		return logger;
	}

	@Override
	protected ContextualAnnotation getConcreteAnnotation() {
		return new ActorAnnotation();
	}
	
	public ActorsAnnotator() {
		super();
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

	
	@SuppressWarnings("unchecked")
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		Map<String,ActorAnnotation> scoreMap = new HashMap<String,ActorAnnotation>();
		Map<String,BigDecimal> entities = (Map<String, BigDecimal>) document.annotation().get(getEntityAnnotationClass());
	    for (String canonicalEntityName : entities.keySet()) {
	    	ActorAnnotation characterAnnotation = new ActorAnnotation();
	    	characterAnnotation.setCanonicalName(canonicalEntityName);
	    	try {
	    		for (CoreSentence sentence: document.sentences()) {
	    			Map<String,BigDecimal> sentencePeople = (Map<String, BigDecimal>) sentence.coreMap().get(getEntityAnnotationClass());
	    			if (sentencePeople != null && sentencePeople.get(canonicalEntityName) != null) {
	    				scoreSentence(document, sentence, characterAnnotation);
	    			}
	    			for (CoreLabel token : sentence.tokens()) {
		    			Map<String,BigDecimal> tokenPeople = (Map<String, BigDecimal>) token.get(getEntityAnnotationClass());
		    			if (tokenPeople != null && tokenPeople.get(canonicalEntityName) != null) {
		    				scoreToken(document, token, characterAnnotation);
		    				scoreOOPGender(token, characterAnnotation);
		    				scoreCoreGender(token, characterAnnotation);
		    			}
	    			}
	    		}
		    	scoreQuotes(document, canonicalEntityName, characterAnnotation);
		    	scoreThumbnails(characterAnnotation);

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
	    	if (characterAnnotation.getImportance().compareTo(new BigDecimal(1)) > 0) {
	    		characterAnnotation.setVaderSentimentAvg();
	    		characterAnnotation.setCoreNlpSentimentAvg();
	    		scoreMap.put(toAlphaNumeric(canonicalEntityName), characterAnnotation);
	    	}
	    }
	    document.annotation().set(getAnnotationClass(), scoreMap);
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
	protected void scoreThumbnails(ContextualAnnotation actor) throws IOException, URISyntaxException {
		String tmpCharacterName = actor.getCanonicalName();
		if (!tmpCharacterName.equalsIgnoreCase("I")) {

			String[] names = tmpCharacterName.split(" ");
			if (names.length > 1) {
				if (names[0].startsWith("Mr") || names[0].startsWith("Mrs") || names[0].startsWith("Dr")) {
					tmpCharacterName = names[1];
				}
				else {
					tmpCharacterName = names[0];
				}
			}
			actor.getThumbnails().addAll(BingUtils.getInstance().getImagesByText(tmpCharacterName));
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
				BigDecimal existingScore = characterAnnotation.getExtrovert();
				characterAnnotation.setExtrovert(existingScore.add(annotationScore.get("extrovert")));
			}
			if (annotationScore.containsKey("introvert")) {
				BigDecimal existingScore = characterAnnotation.getIntrovert();
				characterAnnotation.setIntrovert(existingScore.add(annotationScore.get("introvert")));
			}
			if (annotationScore.containsKey("sensing")) {
				BigDecimal existingScore = characterAnnotation.getSensing();
				characterAnnotation.setSensing(existingScore.add(annotationScore.get("sensing")));
			}
			if (annotationScore.containsKey("intuitive")) {
				BigDecimal existingScore = characterAnnotation.getIntuitive();
				characterAnnotation.setIntuitive(existingScore.add(annotationScore.get("intuitive")));
			}
			if (annotationScore.containsKey("thinking")) {
				BigDecimal existingScore = characterAnnotation.getThinking();
				characterAnnotation.setThinking(existingScore.add(annotationScore.get("thinking")));
			}
			if (annotationScore.containsKey("feeling")) {
				BigDecimal existingScore = characterAnnotation.getFeeling();
				characterAnnotation.setFeeling(existingScore.add(annotationScore.get("feeling")));
			}
			if (annotationScore.containsKey("judging")) {
				BigDecimal existingScore = characterAnnotation.getJudging();
				characterAnnotation.setJudging(existingScore.add(annotationScore.get("judging")));
			}
			if (annotationScore.containsKey("perceiving")) {
				BigDecimal existingScore = characterAnnotation.getPerceiving();
				characterAnnotation.setJudging(existingScore.add(annotationScore.get("perceiving")));
			}
		}
	}
	

	@Override
	public String getDescription() {
		return "OOPPeopleAnnotation with sentiment, quotes, coref, and universal dependencies nsubj, nmod:poss, advmod, amod (http://universaldependencies.org/docsv1/en/dep/index.html)." ;
	}

}
