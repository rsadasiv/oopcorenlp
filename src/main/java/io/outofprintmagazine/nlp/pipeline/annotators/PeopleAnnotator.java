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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.CorefChain.CorefMention;
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
import io.outofprintmagazine.nlp.pipeline.PhraseAnnotation;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.PhraseScorer;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.PhraseSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;
import io.outofprintmagazine.nlp.utils.CoreNlpUtils;

public class PeopleAnnotator extends AbstractPosAnnotator implements OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(PeopleAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}
	
	
	private List<String> properNouns = Arrays.asList("NNP","NNPS");

	public PeopleAnnotator() {
		super();
		this.setScorer((Scorer) new PhraseScorer(this.getAnnotationClass()));
		this.setSerializer((Serializer) new PhraseSerializer(this.getAnnotationClass()));
		this.setTags(Arrays.asList("NNP", "NNPS", "NN", "NNS"));
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPPeopleAnnotation.class;
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
	}
	
	private List<CoreEntityMention> getAllPersonEntityMentions(CoreDocument document) {
		List<CoreEntityMention> retval = new ArrayList<CoreEntityMention>();
		for (CoreSentence sentence : document.sentences()) {
			List<CoreEntityMention> entityMentions = sentence.entityMentions();
			for (CoreEntityMention mention : entityMentions) {
				if (mention.entityType().equals("PERSON")) {
					boolean isPronoun = false;
					try {
						for (CoreLabel t : CoreNlpUtils.getInstance(getParameterStore()).getTokensFromCoreEntityMention(document, mention)) {
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
						List<PhraseAnnotation> scoreList = new ArrayList<PhraseAnnotation>();
						addToScoreList(scoreList, new PhraseAnnotation(entityName, new BigDecimal(1)));
						token.set(getAnnotationClass(), scoreList);
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
							List<PhraseAnnotation> scoreList = new ArrayList<PhraseAnnotation>();
							addToScoreList(scoreList, new PhraseAnnotation(personName, new BigDecimal(1)));
							token.set(getAnnotationClass(), scoreList);
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
				for (CoreEntityMention entityMention : CoreNlpUtils.getInstance(getParameterStore()).getCoreEntityMentionFromCorefMention(document, corefMention)) {
					for (CoreLabel token : entityMention.tokens()) {	
						if (token.containsKey(getAnnotationClass())) {
							List<PhraseAnnotation> existingScores = (List<PhraseAnnotation>) token.get(getAnnotationClass());
							//Map<String,BigDecimal> personMap = (Map<String, BigDecimal>) token.get(getAnnotationClass());
							//for (Map.Entry<String, BigDecimal> personEntry : personMap.entrySet()) {
							for (PhraseAnnotation personScore : existingScores) {
								BigDecimal existingScore = corefPeople.get(personScore.getName());
								if (existingScore == null) {
									existingScore = new BigDecimal(0);
								}
								corefPeople.put(personScore.getName(), existingScore.add(personScore.getValue()));
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
						for (CoreEntityMention entityMention : CoreNlpUtils.getInstance(getParameterStore()).getCoreEntityMentionFromCorefMention(document, corefMention)) {
							for (CoreLabel token : entityMention.tokens()) {
								if (!token.containsKey(getAnnotationClass())) {
									List<PhraseAnnotation> scoreList = new ArrayList<PhraseAnnotation>();
									addToScoreList(scoreList, new PhraseAnnotation(corefPersonName, new BigDecimal(1)));
									token.set(getAnnotationClass(), scoreList);
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
						List<PhraseAnnotation> scoreList = new ArrayList<PhraseAnnotation>();
						addToScoreList(scoreList, new PhraseAnnotation(token.lemma(), new BigDecimal(1)));
						token.set(getAnnotationClass(), scoreList);
					}
				}
			}
		}
	}

	@Override
	public String getDescription() {
		return "CoreNLP NER mentions: I, PERSON minus PRP, PRP$, stray NNP/NNPS; coref";
	}
}
