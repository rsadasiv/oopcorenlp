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
package io.outofprintmagazine.nlp.utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.mit.jverbnet.data.IMember;
import edu.mit.jverbnet.data.IVerbClass;
import edu.mit.jverbnet.data.IWordnetKey;
import edu.mit.jverbnet.index.IVerbIndex;
import edu.mit.jverbnet.index.VerbIndex;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISenseKey;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.stanford.nlp.ling.CoreLabel;
import io.outofprintmagazine.util.IParameterStore;


public class WordnetUtils {

	private static final Logger logger = LogManager.getLogger(WordnetUtils.class);
	
	private IDictionary wordnet = null;
	private HashMap<String, ArrayList<String>> verbnet = new HashMap<String, ArrayList<String>>();
	private IParameterStore parameterStore = null;

	private WordnetUtils(IParameterStore parameterStore) throws IOException {
		wordnet = new Dictionary(
				new URL(
						"file", 
						null, 
						parameterStore.getProperty(
								"wordNet_location"
						)
//						"C:\\Users\\rsada\\eclipse-workspace\\oopcorenlp\\data\\wn3.1.dict\\dict"
				)
		);
		wordnet.open();
		this.parameterStore = parameterStore;
	}
	
	private static Map<IParameterStore, WordnetUtils> instances = new HashMap<IParameterStore, WordnetUtils>();
	
    public static WordnetUtils getInstance(IParameterStore parameterStore) throws IOException { 
        if (instances.get(parameterStore) == null) {
        	WordnetUtils instance = new WordnetUtils(parameterStore);
            instances.put(parameterStore, instance);
        }
        return instances.get(parameterStore); 
    }
    
	private IDictionary getWordnet() {
		return wordnet;
	}
	
	public synchronized List<String> getVerbnetSenses(CoreLabel token, List<CoreLabel> contextWords) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		List<String> retval = new ArrayList<String>(); 
		ISenseKey senseKey = getTargetWordSenseKey(token, contextWords);
		if (senseKey != null) {
			List<String> senses = getVerbnetSenses(senseKey);
			if (senses != null) {
				for (String sense : senses) {
					retval.add(sense.split("-")[0]);
				}
			}
		}
		return retval;
	}
	
	private ArrayList<String> getVerbnetSenses(ISenseKey senseKey) throws IOException {
		if (senseKey != null && senseKey.toString().length() > 2) {
			return getVerbnetSenses(senseKey.toString().substring(0, senseKey.toString().length()-2));
		}
		return new ArrayList<String>();
	}
	
	private ArrayList<String> getVerbnetSenses(String senseKey) throws IOException {
		//logger.debug("checking: " + senseKey);
		ArrayList<String> retval = getVerbnet().get(senseKey);
		return retval;
	}
	
	private HashMap<String, ArrayList<String>> getVerbnet() throws IOException {
		if (verbnet.size() == 0) {
			initVerbnet();
		}
		return verbnet;
	}
	
	private void initVerbnet() throws IOException {
		IVerbIndex index = new VerbIndex(
				new URL(
						"file", 
						null, 
						parameterStore.getProperty(
								"verbNet_location"
						)
//						"C:\\Users\\rsada\\eclipse-workspace\\oopcorenlp\\data\\new_vn\\"
				)
		);
		index.open();
		Iterator<IVerbClass> verbClassIter = index.iteratorRoots();
		while (verbClassIter.hasNext()) {
			IVerbClass verb = verbClassIter.next();
			for (IMember member : verb.getMembers()) {
				for (IWordnetKey senseKey : member.getWordnetTypes().keySet()) {
					ArrayList<String> senseKeyList = verbnet.get(senseKey.toString());
					if (senseKeyList == null) {
						senseKeyList = new ArrayList<String>();
					}
					senseKeyList.add(verb.getID());
					verbnet.put(senseKey.toString(), senseKeyList);
					//logger.debug("loaded " + senseKey.toString() + " size " + senseKeyList.size());
				}
			}
			
		}	
	}
	
	private POS tagToPOS(CoreLabel token) {
		POS wnPos = null;
		if (token.tag().startsWith("N")) {
			wnPos = POS.NOUN;
		}
		else if (token.tag().startsWith("V")) {
			wnPos = POS.VERB;
		}
		else if (token.tag().startsWith("J")) {
			wnPos = POS.ADJECTIVE;
		}
		else if (token.tag().equals("DT")) {
			wnPos = POS.ADJECTIVE;
		}
		else if (token.tag().startsWith("R")) {
			wnPos = POS.ADVERB;
		}
		return wnPos;
	}
	

	private String getLexicalFileName(CoreLabel token) {
		String score = null;
		POS pos = tagToPOS(token);
		if (pos == null) {
			return score;
		}
				
			try {
				IIndexWord idxWord = getWordnet().getIndexWord(token.lemma(), tagToPOS(token));
				if (idxWord != null && idxWord.getWordIDs().size() > 0) {
					IWordID wordID = idxWord.getWordIDs().get(0);
					IWord word = getWordnet().getWord(wordID);
					score = word.getSynset().getLexicalFile().getName();
				}
			}
			catch (Exception e) {
				logger.error(token.toString(), e);
			}


		return score;
	}
	
	public synchronized String getLexicalFileName(CoreLabel token, List<CoreLabel> contextWords) {
		String score = null;
		POS pos = tagToPOS(token);
		if (pos == null) {
			return score;
		}
	
			try {
				IIndexWord idxWord = getWordnet().getIndexWord(token.lemma(), tagToPOS(token));
				if (idxWord != null && idxWord.getWordIDs().size() > 0) {
					ISenseKey senseKey = getTargetWordSenseKey(token, contextWords);
					for (IWordID wordID : idxWord.getWordIDs()) {
						IWord word = getWordnet().getWord(wordID);
						if (word.getSenseKey().equals(senseKey)) {
							score = word.getSynset().getLexicalFile().getName();
							break;
						}
					}
				}
			}
			catch (Exception e) {
				logger.error(token.toString(), e);
			}

		
		return score;
	}
	
	public synchronized boolean isIndexWord(CoreLabel token) {
		return getIndexWord(token) == null;
	}
	
	private IIndexWord getIndexWord(CoreLabel token) {
		IIndexWord retval = null;

			if (tagToPOS(token) != null) {
				retval = getWordnet().getIndexWord(token.lemma(), tagToPOS(token));
			}
			if ((token.tag().equals("JJR") || token.tag().equals("RBR")) && token.lemma().endsWith("er") && token.lemma().length() > 2) {
				retval = getWordnet().getIndexWord(token.lemma().substring(0, token.lemma().length()-2 ), tagToPOS(token));
			}
			if ((token.tag().equals("JJS") || token.tag().equals("RBS")) && token.lemma().endsWith("est") && token.lemma().length() > 3) {
				retval = getWordnet().getIndexWord(token.lemma().substring(0, token.lemma().length()-3 ), tagToPOS(token));
			}
			if (retval == null) {
				retval = getWordnet().getIndexWord(token.lemma(), POS.NOUN);
			}
			if (retval == null) {
				retval = getWordnet().getIndexWord(token.lemma(), POS.VERB);
			}
			if (retval == null) {
				retval = getWordnet().getIndexWord(token.lemma(), POS.ADJECTIVE);
				
			}
			if (retval == null) {
				retval = getWordnet().getIndexWord(token.lemma(), POS.ADVERB);
			}

		return retval;
	}
	
	public synchronized Map<String,BigDecimal> scoreTokenHypernym(CoreLabel token, List<CoreLabel> contextWords, Map<String,BigDecimal> tokensToMatch) {
		Map<String,BigDecimal> retval = new HashMap<String,BigDecimal>();
		POS pos = tagToPOS(token);
		if (pos == null) {
			return retval;
		}
				
		try {
			IIndexWord idxWord = getIndexWord(token);
			if (idxWord != null && idxWord.getWordIDs().size() > 0) {
				ISenseKey senseKey = getTargetWordSenseKey(token, contextWords);
				for (IWordID wordID : idxWord.getWordIDs()) {
					IWord word = getWordnet().getWord(wordID);
					if (word.getSenseKey().equals(senseKey)) {
						List<ISynsetID> hypernyms = word.getSynset().getRelatedSynsets(Pointer.HYPERNYM);
						IWord bestHypernymWord = null;
						BigDecimal bestHypernymScore = new BigDecimal(-1);
						for (int i=0;i<hypernyms.size();i++) {
							for (IWord hypernymWord : getWordnet().getSynset(hypernyms.get(i)).getWords()) {
								if (i==0) {
									bestHypernymWord = hypernymWord;
								}
								try {
									//what does this mean?
									//retval.put(hypernymWord.getLemma(), new BigDecimal(hypernyms.size()-i));
									BigDecimal hypernymScore = new BigDecimal(0);
									List<ISynsetID> hyponyms = hypernymWord.getSynset().getRelatedSynsets(Pointer.HYPONYM);
									for (int j=0;j<hyponyms.size();j++) {
										//logger.debug("hyponyms");
										for (IWord hyponymWord : getWordnet().getSynset(hyponyms.get(j)).getWords()) {
											//logger.debug("hyponym synset lemma:" + hyponymWord.getLemma());
											if (tokensToMatch.containsKey(hyponymWord.getLemma())) {
												hypernymScore.add(tokensToMatch.get(hyponymWord.getLemma()));
											}
										}
									}
									if (hypernymScore.compareTo(bestHypernymScore) > 0) {
										bestHypernymWord = hypernymWord;
									}
								}
								catch (Throwable t) {
									logger.debug(t);
								}
							}
						}
						if (bestHypernymWord != null) {
							retval.put(bestHypernymWord.getLemma(), new BigDecimal(1));
						}
						break;
					}
				}
			}
		}
		catch (Exception e) {
			logger.error(token.toString(), e);
		}

		return retval;
	}
	
	public synchronized String getTokenGloss(CoreLabel token, List<CoreLabel> contextWords) {
		String retval = null;
		POS pos = tagToPOS(token);
		if (pos == null) {
			return retval;
		}
					
			try {
				IIndexWord idxWord = getIndexWord(token);
				if (idxWord != null && idxWord.getWordIDs().size() > 0) {
					ISenseKey senseKey = getTargetWordSenseKey(token, contextWords);
					for (IWordID wordID : idxWord.getWordIDs()) {
						IWord word = getWordnet().getWord(wordID);
						if (word.getSenseKey().equals(senseKey)) {
							retval = word.getSynset().getGloss();
							break;
						}
					}
				}
			}
			catch (Exception e) {
				logger.error(token.toString(), e);
			}

		return retval;
	}
	
	private ISenseKey getTargetWordSenseKey(CoreLabel targetWord, List<CoreLabel> contextWords) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		POS pos = tagToPOS(targetWord);
		if (pos == null) {
			return null;
		}
		ISenseKey retval = getTargetWordSenseKeyPOS(pos, targetWord, contextWords);
		if (retval == null && pos.compareTo(POS.ADJECTIVE) == 0) {
			retval = getTargetWordSenseKeyPOS(POS.VERB, targetWord, contextWords);
		}
		return retval;
	}

	private ISenseKey getTargetWordSenseKeyPOS(POS pos, CoreLabel targetWord, List<CoreLabel> contextWords) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
	
			//look up the target word
			IIndexWord idxWord = getWordnet().getIndexWord(targetWord.lemma(), pos);	
			if (idxWord != null) {
				IWord word0 = getWordnet().getWord(idxWord.getWordIDs().get(0));
				//if the target word only has one sense, return it
				if (idxWord.getWordIDs().size() == 1) {
					//logger.debug("returning only sense: " + targetWord);
					return word0.getSenseKey();
				}
				//if the top ranked sense is more than 2 times as likely as the second ranked sense, return the top ranked sense
				IWord word1 = getWordnet().getWord(idxWord.getWordIDs().get(1));
				if (getWordnet().getSenseEntry(word1.getSenseKey()).getTagCount() < (getWordnet().getSenseEntry(word0.getSenseKey()).getTagCount()/2)) {
					//logger.debug("returning dominant sense: " + targetWord);
					return word0.getSenseKey();
				}
				else {
					ISenseKey k = simplifiedLesk(targetWord, contextWords);
					//logger.debug("ran simplifiedLesk: " + targetWord + "sense " + k);
					return k;
					//logger.debug("simplifiedLesk doesn't add value - throwing it away");
				}
			}
			//logger.debug("no sense key: " + targetWord);

		return null;
	}
	
	private List<String> getGlossWords(IWord word) {
		String gloss = word.getSynset().getGloss();
		gloss = gloss.toLowerCase();
		gloss = gloss.replace(";", "");
		gloss = gloss.replace("\"", "");
		gloss = gloss.replace(",", "");
		gloss = gloss.replace("!", "");
		gloss = gloss.replace("(", "");
		gloss = gloss.replace(")", "");
		gloss = gloss.replace("?", "");
		return Arrays.asList(gloss.split("\\s+"));
	}
	
	private List<String> getSynsetWords(IWord word) {
		ArrayList<String> retval = new ArrayList<String>();
		
			ISynset synset = word.getSynset();
	        for (IWord w : synset.getWords()) {
	            retval.add(w.getLemma());
	        }
	        for (ISynsetID relatedSynsetId : synset.getRelatedSynsets()) {
	        	ISynset relatedSynset = getWordnet().getSynset(relatedSynsetId);
	        	for (IWord relatedSynsetWord : relatedSynset.getWords()) {
	        		retval.add(relatedSynsetWord.getLemma());
	        	}
	        }
	        for (IWordID relatedWordId : word.getRelatedWords()) {
	        	IWord relatedWord = getWordnet().getWord(relatedWordId);
	        	retval.add(relatedWord.getLemma());
	        	
	        }

        return retval;
	}
	
	private List<String> removeCommonWords(List<String> list) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {

		Map<String,String> dict = ResourceUtils.getInstance(parameterStore).getDictionary(
				"io/outofprintmagazine/nlp/models/COCA/en_100.txt"
		);
		Iterator<String> listIter = list.iterator();
		while (listIter.hasNext()) {
			if (dict.get(listIter.next()) != null) {
				listIter.remove();
			}
		}
		return list;
	}
	
	//the word itself, all words in the glosses or synset, remove common words
	private List<String> getSenseContextWords(IWord word) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		ArrayList<String> retval = new ArrayList<String>();
		retval.add(word.getLemma());
		retval.addAll(getGlossWords(word));
		retval.addAll(getSynsetWords(word));
		return retval;
	}
	
	private List<String> getLemmasAndTokens(List<CoreLabel> words) {
		List<String> retval = new ArrayList<String>();
		for (CoreLabel word : words ) {
			retval.add(word.lemma());
			retval.add(word.originalText().toLowerCase());
		}
		return retval;
	}
	
	 private <T> List<T> removeDuplicates(List<T> list) 
	    { 
	  
	        // Create a new LinkedHashSet 
	        Set<T> set = new LinkedHashSet<>(); 
	  
	        // Add the elements to set 
	        set.addAll(list); 
	  
	        // Clear the list 
	        list.clear(); 
	  
	        // add the elements of set 
	        // with no duplicates to the list 
	        list.addAll(set); 
	  
	        // return the list 
	        return list; 
	    } 
	
	private int computeWordOverlap(List<String> list, List<String> otherList) {
		//logger.debug("--------Context---------------");
		//for (String x : list) {
			//logger.debug(x);
		//}
		//logger.debug("--------Gloss+Related---------------");
		//for (String x : otherList) {
			//logger.debug(x);
		//}
		int overlap = 0;
		for (String x : list) {
			if (otherList.contains(x)) {
				//String wordFrequency = getTa().getDictionary(dictionaryFileName).get(x);
				//double numerator = 100;
				//if (wordFrequency == null) {
					overlap++;
				//}
				//else {
				//	overlap+=(double) numerator/new Integer(wordFrequency).doubleValue();
				//}
			}
		}
		return overlap;
	}
		
	/*
	 * function SIMPLIFIED LESK(word,sentence) returns best sense of word
	best-sense <- most frequent sense for word
	max-overlap <- 0
	context <- set of words in sentence
	for each sense in senses of word do
	signature <- set of words in the gloss and examples of sense
	overlap <- COMPUTEOVERLAP (signature,context)
	if overlap > max-overlap then
	max-overlap <- overlap
	best-sense <- sense
	end return (best-sense)		
	 */
	private ISenseKey simplifiedLesk(CoreLabel targetWord, List<CoreLabel> contextWords) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		ISenseKey retval = null;
		POS pos = tagToPOS(targetWord);
		if (pos == null) {
			return retval;
		}
		List<String> expandedContextWords = removeCommonWords(removeDuplicates(getLemmasAndTokens(contextWords)));
		IIndexWord idxWord = getWordnet().getIndexWord(targetWord.lemma(), tagToPOS(targetWord));
		if (idxWord != null) {
			//logger.debug("checking word: " + idxWord.getLemma());
			int maxOverlap = 0;
			int bestMatchIdx = 0;
			for (int i=0; i<idxWord.getWordIDs().size();i++) {
				IWord word = getWordnet().getWord(idxWord.getWordIDs().get(i));
				//logger.debug("checking sense: " + i);
				//logger.debug("current best match: " + bestMatchIdx);
				int overlap = computeWordOverlap(expandedContextWords, removeCommonWords(removeDuplicates(getSenseContextWords(word))));
				if (overlap > maxOverlap) {
					maxOverlap = overlap;
					bestMatchIdx = i;
				}
			}
			//logger.debug("bestMatch was: " + bestMatchIdx);
			retval = getWordnet().getWord(idxWord.getWordIDs().get(bestMatchIdx)).getSenseKey();
			//logger.debug("senseKey: " + retval);
		}
		return retval;
	}			
}
