package io.outofprintmagazine.nlp.pipeline.annotators;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.CorefChain.CorefMention;
import edu.stanford.nlp.coref.data.Dictionaries;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.CoreQuote;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.CoreNlpUtils;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public class PeopleAnnotator extends AbstractAggregatePosAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(PeopleAnnotator.class);
	private List<String> properNouns = Arrays.asList("NNP","NNPS");

	public PeopleAnnotator() {
		super();
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass(), this.getAggregateClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass(), this.getAggregateClass()));
		this.setTags(Arrays.asList("NNP", "NNPS", "NN", "NNS"));
	}
	
	public PeopleAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPPeopleAnnotation.class;
	}
	
	@Override
	public Class getAggregateClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPPeopleAnnotationAggregate.class;
	}
	
	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(
			new ArraySet<>(
				Arrays.asList(
					CoreAnnotations.TextAnnotation.class, 
					CoreAnnotations.TokensAnnotation.class,
					CoreAnnotations.SentencesAnnotation.class,
					CoreAnnotations.PartOfSpeechAnnotation.class,
					CoreAnnotations.LemmaAnnotation.class,
					CoreAnnotations.NamedEntityTagAnnotation.class, 
					CoreAnnotations.NormalizedNamedEntityTagAnnotation.class,
					CoreAnnotations.CanonicalEntityMentionIndexAnnotation.class,
					CorefCoreAnnotations.CorefChainAnnotation.class
				)
			)
		);
	}
	
	//Do Person entities first
	//Then do coref

	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		annotateNNP(document);
		annotatePerson(document);
		try {
			annotateCoref(document);
		}
		catch (Exception e) {
			logger.error(e);
		}
		annotateNarrator(document);
		score(document);
	}
	
	private List<CoreEntityMention> getAllPersonEntityMentions(CoreDocument document) {
		List<CoreEntityMention> retval = new ArrayList<CoreEntityMention>();
		for (CoreSentence sentence : document.sentences()) {
			List<CoreEntityMention> entityMentions = sentence.entityMentions();
			for (CoreEntityMention mention : entityMentions) {
				if (mention.entityType().equals("PERSON")) {
					boolean isPronoun = false;
					try {
						for (CoreLabel t : CoreNlpUtils.getInstance().getTokensFromCoreEntityMention(document, mention)) {
							if (t.tag().equals("PRP") || t.tag().equals("PRP$")) {
								isPronoun = true;
								break;
							}
						}
					} 
					catch (Exception e) {
						logger.error(e);
					}
					if (!isPronoun) {
						if (mention.entityType().equals("PERSON")) {
							retval.add(mention);
						}
					}
				}
			}
		}
		return retval;
	}
	
	private String getEntityNameFromEntityMention(CoreEntityMention entityMention) {
		String personName = "Kirby";
		if (entityMention.canonicalEntityMention().isPresent()) {
			personName = entityMention.canonicalEntityMention().get().toString();
		}
		else {
			personName = entityMention.entity().toString();
		}
		return personName;
	}
		
	
	private Map<String, String> getEntityNameAliases(List<CoreEntityMention> allEntityMentions) {
		Map<String, String> entityAliases = new HashMap<String,String>();
		for (CoreEntityMention entityMention : allEntityMentions) {
			String personName = getEntityNameFromEntityMention(entityMention);
			if (
					!personName.equalsIgnoreCase("he") 
					&& !personName.equalsIgnoreCase("his") 
					&& !personName.equalsIgnoreCase("she") 
					&& !personName.equalsIgnoreCase("her")
			) {
				entityAliases.put(personName.toLowerCase(), personName);
			}			
			
		}
		for (String entityName : entityAliases.keySet()) {
			if (entityName.contains(" ")) {
				for (String shortEntityName : entityAliases.keySet()) {
					if (entityName.contains(shortEntityName)) {
						entityAliases.put(shortEntityName, entityAliases.get(entityName));
					}
				}
			}
		}

		return entityAliases;
		
	}
	
	private String getNNPNameAlias(String nnpName, Map<String, String> entityAliases) {
		String retval = nnpName;
		for (String entityName : entityAliases.keySet()) {
			if (entityName.contains(nnpName.toLowerCase())) {
				retval = entityAliases.get(entityName);
			}
		}
		return retval;
	}
	
	private void annotatePerson(CoreDocument document) {
		List<CoreEntityMention> allEntityMentions = getAllPersonEntityMentions(document);
		Map<String, String> allEntityAliases = getEntityNameAliases(allEntityMentions);
		for (CoreEntityMention entityMention : allEntityMentions) {
			String personName = getEntityNameFromEntityMention(entityMention);
			if (
					!personName.equalsIgnoreCase("he") 
					&& !personName.equalsIgnoreCase("his") 
					&& !personName.equalsIgnoreCase("she") 
					&& !personName.equalsIgnoreCase("her")
			) {
				Map<String,BigDecimal> scoreMap = new HashMap<String,BigDecimal>();
				String entityName = allEntityAliases.get(personName.toLowerCase());

				for (CoreLabel token : entityMention.tokens()) {
//					logger.debug("Person");
//					logger.debug("token.sentIndex: " + token.sentIndex());
//					logger.debug("token.index: " + token.index());
//					logger.debug("token.lemma: " + token.lemma());
//					logger.debug("personName: " + personName); 
//					logger.debug("entityName: " + entityName); 
//					logger.debug("entityType: " + entityMention.entityType());
//					logger.debug("------------");
					if (!token.containsKey(getAnnotationClass())) {
//						logger.debug("scoreUpdated: " + entityName);
//						logger.debug("------------");
						scoreMap.put(entityName, new BigDecimal(1));
						token.set(getAnnotationClass(), scoreMap);
					}
				}
			}
		}
	}
	
	private void annotateNNP(CoreDocument document) {
		List<CoreEntityMention> allEntityMentions = getAllPersonEntityMentions(document);
		Map<String, String> allEntityAliases = getEntityNameAliases(allEntityMentions);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (scoreTag(token) != null) {
					String personName = null;
					if (token.ner() == null || "O".equals(token.ner()) || "MISC".equals(token.ner())) {
						personName = allEntityAliases.get(token.lemma().toLowerCase());
						if (personName == null && properNouns.contains(token.tag())) {
							personName = getNNPNameAlias(token.lemma(), allEntityAliases);
						}
					}
					if (personName != null) {
//						logger.debug("NNP");
//						logger.debug("token.sentIndex: " + token.sentIndex());
//						logger.debug("token.index: " + token.index());
//						logger.debug("token.lemma: " + token.lemma());
//						logger.debug("personName: " + personName); 
//						logger.debug("------------");
						if (!token.containsKey(getAnnotationClass())) {
//							logger.debug("scoreUpdated: " + personName);
//							logger.debug("------------");
							Map<String,BigDecimal> scoreMap = new HashMap<String,BigDecimal>();
							scoreMap.put(personName, new BigDecimal(1));
							token.set(getAnnotationClass(), scoreMap);
						}
					}

				}
			}
		}
	}
	
	//I must have this method somewhere already
	public List<Map.Entry<String, BigDecimal>> sortMapByValueDescending(Map<String,BigDecimal> hm) {
        List<Map.Entry<String, BigDecimal>> list = new java.util.LinkedList<Map.Entry<String, BigDecimal>>(hm.entrySet()); 
         Collections.sort(list, new Comparator<Map.Entry<String, BigDecimal>>() { 
             @Override
			public int compare(Map.Entry<String, BigDecimal> o1,  
                                Map.Entry<String, BigDecimal> o2) 
             { 
                 return (o2.getValue()).compareTo(o1.getValue()); 
             } 
         });
         return list;
	}
	
	private void annotateCoref(CoreDocument document) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		for (Map.Entry<Integer, CorefChain> corefChainEntry : document.corefChains().entrySet()) {
			CorefChain corefChain = corefChainEntry.getValue();
			Map<String, BigDecimal> corefPeople = new HashMap<String,BigDecimal>();
			for (CorefMention corefMention : corefChain.getMentionsInTextualOrder()) {
				for (CoreEntityMention entityMention : CoreNlpUtils.getInstance().getCoreEntityMentionFromCorefMention(document, corefMention)) {
					for (CoreLabel token : entityMention.tokens()) {
						if (token.containsKey(getAnnotationClass())) {
							Map<String,BigDecimal> personMap = (Map<String, BigDecimal>) token.get(getAnnotationClass());
							for (Map.Entry<String, BigDecimal> personEntry : personMap.entrySet()) {
								BigDecimal existingScore = corefPeople.get(personEntry.getKey());
								if (existingScore == null) {
									existingScore = new BigDecimal(0);
								}
								corefPeople.put(personEntry.getKey(), existingScore.add(personEntry.getValue()));
							}
						}
					}
				}
			}
			if (corefPeople.size() > 0) {
				String corefPersonName = sortMapByValueDescending(corefPeople).get(0).getKey();
				boolean haveSeenProper = false;
				for (CorefMention corefMention : corefChain.getMentionsInTextualOrder()) {
//					if (corefMention.mentionType.compareTo(Dictionaries.MentionType.PROPER) == 0) {
//						if (haveSeenProper) {
//							break;
//						}
//						else {
//							haveSeenProper = true;
//						}
//					}
					//if (corefMention.mentionType.compareTo(Dictionaries.MentionType.PRONOMINAL) == 0) {
						for (CoreEntityMention entityMention : CoreNlpUtils.getInstance().getCoreEntityMentionFromCorefMention(document, corefMention)) {
							for (CoreLabel token : entityMention.tokens()) {
								
								String existingAnnotation = "empty";
								if (token.containsKey(getAnnotationClass())) {
									Map<String,BigDecimal> personMap = (Map<String, BigDecimal>) token.get(getAnnotationClass());
									Iterator<String> personMapKeysIter = personMap.keySet().iterator();
									if (personMapKeysIter.hasNext()) {
										existingAnnotation = personMapKeysIter.next();
									}
								}
//								logger.debug("Coref");
//								logger.debug("corefChainID: " + corefChain.getChainID());
//								logger.debug("corefMentionID: " + corefMention.mentionID);
//								logger.debug("token.sentIndex: " + token.sentIndex());
//								logger.debug("token.index: " + token.index());
//								logger.debug("token.lemma: " + token.lemma());
//								logger.debug("corefPersonName: " + corefPersonName); 
//								logger.debug("existingPersonName: " + existingAnnotation);
//								logger.debug("------------");
								if (!token.containsKey(getAnnotationClass())) {
									Map<String,BigDecimal> scoreMap = new HashMap<String,BigDecimal>();
									scoreMap.put(corefPersonName, new BigDecimal(1));
									token.set(getAnnotationClass(), scoreMap);
//									logger.debug("scoreUpdated: " + corefPersonName);
//									logger.debug("------------");
								}
							}
						}
					//}
				}
			}
		}
	}
	
	public void annotateNarrator(CoreDocument document) {
		for (int sentenceIdx = 0; sentenceIdx < document.sentences().size(); sentenceIdx++) {
			CoreSentence sentence = document.sentences().get(sentenceIdx);
			List<CoreLabel> tokens = sentence.tokens();
			for (int i = 0; i < tokens.size(); i++) {
				CoreLabel token = tokens.get(i);
				if (token.lemma().equals("I")) {
					boolean shouldScore = true;
					for (CoreQuote quote : document.quotes()) {
						if (
							token.beginPosition() >= quote.quoteCharOffsets().first.intValue() 
							&& token.endPosition() <= quote.quoteCharOffsets().second.intValue() 
						) {
							shouldScore = false;
							break;
						}
					}
					if (shouldScore) {
						Map<String, BigDecimal> scoreMap = new HashMap<String, BigDecimal>();
						scoreMap.put(token.lemma(), new BigDecimal(1));
						token.set(getAnnotationClass(), scoreMap);
					}
				}
			}
		}
	}

	@Override
	public String getDescription() {
		return "CoreNLP NER mentions: PERSON minus PRP, PRP$ plus coref";
	}
}
