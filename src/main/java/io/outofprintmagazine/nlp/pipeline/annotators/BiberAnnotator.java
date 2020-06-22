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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.tokensregex.TokenSequenceMatcher;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;
import io.outofprintmagazine.nlp.utils.ResourceUtils;

public class BiberAnnotator extends AbstractPosAnnotator implements Annotator, OOPAnnotator{
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(BiberAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}
	
	public BiberAnnotator() {
		super();
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass()));		
	}

	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPBiberAnnotation.class;
	}

	
	@Override
	public String getDescription() {
		return "Biber: Variation across speech and writing. https://sites.google.com/site/multidimensionaltagger/MAT%20v%201.2%20-%20Manual.pdf.";
	}


	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(
			new ArraySet<>(
				Arrays.asList(
					CoreAnnotations.TextAnnotation.class, 
					CoreAnnotations.TokensAnnotation.class,
					CoreAnnotations.SentencesAnnotation.class
					)
				)
			);
	}

	@Override
	public void annotate(Annotation annotation) {
		annotate_VBD(annotation);
		annotate_PEAS(annotation);
		annotate_VPRT(annotation);
		annotate_PLACE(annotation);
		annotate_TIME(annotation);
		annotate_FPP1(annotation);
		annotate_SPP2(annotation);
		annotate_TPP3(annotation);
		annotate_PIT(annotation);
		annotate_DEMP(annotation);
		annotate_INPR(annotation);
		annotate_PROD(annotation);
		annotate_WHQU(annotation);
		annotate_NOMZ(annotation);
		annotate_GER(annotation);
		annotate_NN(annotation);
		annotate_PASS(annotation);
		annotate_BYPA(annotation);
		annotate_BEMA(annotation);
		annotate_EX(annotation);
		annotate_THVC(annotation);
		annotate_THAC(annotation);
		annotate_WHCL(annotation);
		annotate_TO(annotation);
		annotate_PRESP(annotation);
		annotate_PASTP(annotation);
		annotate_WZPAST(annotation);
		annotate_WZPRES(annotation);
		annotate_TSUB(annotation);
		annotate_TOBJ(annotation);
		annotate_WHSUB(annotation);
		annotate_WHOBJ(annotation);
		annotate_PIRE(annotation);
		annotate_SERE(annotation);
		annotate_CAUS(annotation);
		annotate_CONC(annotation);
		annotate_COND(annotation);
		annotate_OSUB(annotation);
		annotate_PIN(annotation);
		annotate_JJ(annotation);
		annotate_PRED(annotation);
		annotate_RB(annotation);
		annotate_CONJ(annotation);
		annotate_DWNT(annotation);
		annotate_HDG(annotation);
		annotate_AMP(annotation);
		annotate_EMPH(annotation);
		annotate_DPAR(annotation);
		annotate_DEMO(annotation);
		annotate_POMD(annotation);
		annotate_NEMD(annotation);
		annotate_PRMD(annotation);
		annotate_PUBV(annotation);
		annotate_PRIV(annotation);
		annotate_SUAV(annotation);
		annotate_SEMP(annotation);
		annotate_CONT(annotation);
		annotate_THATD(annotation);
		annotate_STPR(annotation);
		annotate_SPIN(annotation);
		annotate_SPAU(annotation);
		annotate_PHC(annotation);
		annotate_ANDC(annotation);
		annotate_SYNE(annotation);
		annotate_XX0(annotation);
		CoreDocument document = new CoreDocument(annotation);
		score(document);
		annotate_AWL(annotation);
		annotate_TTR(annotation);
	}
	
	private Map<String,BigDecimal> getBiberAnnotation(CoreMap coreMap) {
		Map<String,BigDecimal> retval = null;
		try {
			retval = (Map<String,BigDecimal>) coreMap.get(getAnnotationClass());
		}
		catch (Throwable t) {
			logger.error(t);
		}
		if (retval == null) {
			retval = new HashMap<String, BigDecimal>();
		}
		return retval;	
	}
	
	public void annotate_VBD(Annotation annotation) {
		List<String> posTags = Arrays.asList("VBD");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (posTags.contains(token.tag())) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "VBD", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	

	public void annotate_PEAS(Annotation annotation) {
/*
This is calculated by counting how many times a form of HAVE is followed by: a VBD or
VBN tag (a past or participle form of any verb). These are also counted when an adverb (RB)
or negation (XX0) occurs between the two. The interrogative version is counted too. This is
achieved by counting how many times a form of HAVE is followed by a nominal form
(noun, NN, proper noun, NP or personal pronoun, PRP) and then followed by a VBD or VBN
tag. As for the affirmative version, the latter algorithm also accounts for intervening adverbs
or negations. 
 */
		
		List<String> posTags = Arrays.asList("VBD", "VBN");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (int i=0;i<sentence.tokens().size();i++) {
				if (i<sentence.tokens().size()-1) {
					if (sentence.tokens().get(i).lemma().equals("have") && posTags.contains(sentence.tokens().get(i+1).tag())) {
						Map<String, BigDecimal> scoreMap = getBiberAnnotation(sentence.tokens().get(i));
						addToScoreMap(scoreMap, "PEAS", new BigDecimal(1));
						sentence.tokens().get(i).set(getAnnotationClass(), scoreMap);
					}					
				}
				if (i<sentence.tokens().size()-2) {
					if (sentence.tokens().get(i).lemma().equals("have") && (sentence.tokens().get(i+1).tag().equals("RB") || sentence.tokens().get(i+1).lemma().equals("not")) && posTags.contains(sentence.tokens().get(i+2).tag())) {
						Map<String, BigDecimal> scoreMap = getBiberAnnotation(sentence.tokens().get(i));
						addToScoreMap(scoreMap, "PEAS", new BigDecimal(1));
						sentence.tokens().get(i).set(getAnnotationClass(), scoreMap);
					}					
				}
			}
		}
	}
	
	public void annotate_VPRT(Annotation annotation) {
		List<String> posTags = Arrays.asList("VBP", "VBZ");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (posTags.contains(token.tag())) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "VPRT", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	
	public void annotate_PLACE(Annotation annotation) {
		try {
			List<String> posTags = Arrays.asList("NNP");
			List<String> tags = ResourceUtils.getInstance(getParameterStore()).getList("io/outofprintmagazine/nlp/models/Biber/PLACE.txt");
			CoreDocument document = new CoreDocument(annotation);
			for (CoreSentence sentence : document.sentences()) {
				for (CoreLabel token : sentence.tokens()) {
					if (!posTags.contains(token.tag())) {
						if (tags.contains(token.lemma())) {
							Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
							addToScoreMap(scoreMap, "PLACE", new BigDecimal(1));
							token.set(getAnnotationClass(), scoreMap);
						}
					}
				}
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
	}
	
	
	public void annotate_TIME(Annotation annotation) {
		try {
			List<String> tags = ResourceUtils.getInstance(getParameterStore()).getList("io/outofprintmagazine/nlp/models/Biber/TIME.txt");
			CoreDocument document = new CoreDocument(annotation);
			for (CoreSentence sentence : document.sentences()) {
				boolean soonAs = false;
				for (CoreLabel token : sentence.tokens()) {
					if (tags.contains(token.lemma()) || (soonAs && token.lemma().equals("as"))) {
						Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
						addToScoreMap(scoreMap, "TIME", new BigDecimal(1));
						token.set(getAnnotationClass(), scoreMap);
					}
					if (token.lemma().equals("soon")) {
						soonAs = true;
					}
					else {
						soonAs = false;
					}
				}
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
	}
	
	public void annotate_FPP1(Annotation annotation) {
		List<String> tags = Arrays.asList("i", "me", "us", "my", "we", "our", "myself", "ourselves");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (tags.contains(token.lemma())) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "FPP1", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	
	public void annotate_SPP2(Annotation annotation) {
		List<String> tags = Arrays.asList("you", "your", "yourself", "yourselves", "thy", "thee", "thyself", "thou");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (tags.contains(token.lemma())) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "SPP2", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	
	public void annotate_TPP3(Annotation annotation) {
		List<String> tags = Arrays.asList("she", "he", "they", "her", "him", "them", "his", "their", "himself", "herself", "themselves");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (tags.contains(token.lemma())) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "TPP3", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	
	public void annotate_PIT(Annotation annotation) {
		List<String> tags = Arrays.asList("it", "its", "itself");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (tags.contains(token.lemma())) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "PIT", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	
	//TODO
	public void annotate_DEMP(Annotation annotation) {
/*
The program tags as demonstrative pronouns the words those, this, these when they are
followed by a verb (any tag starting with V) or auxiliary verb (modal verbs in the form of 
MD tags or forms of DO or forms of HAVE or forms of BE) or a punctuation mark or a WH
pronoun or the word and. The word that is tagged as a demonstrative pronoun when it
follows the said pattern or when it is followed by ‘s or is and, at the same time, it has not
been already tagged as a TOBJ, TSUB, THAC or THVC.  
 */
	}
	
	public void annotate_INPR(Annotation annotation) {
		List<String> tags = Arrays.asList("anybody", "anyone", "anything", "everybody",	"everyone",	"everything", "nobody",	"none",	"nothing", "nowhere", "somebody", "someone", "something");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (tags.contains(token.lemma())) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "INPR", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	

	public void annotate_PROD(Annotation annotation) {
/*
Any form of DO that is used as main verb and, therefore, excluding DO when used as
auxiliary verb. The tagger tags as PROD any DO that is NOT in neither of the following
patterns: (a) DO followed by a verb (any tag starting with V) or followed by adverbs (RB),
negations and then a verb (V); (b) DO preceded by a punctuation mark or a WH pronoun (the
list of WH pronouns is in Biber (1988)).  
 */
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (int i=0;i<sentence.tokens().size();i++) {
				if (i<sentence.tokens().size()-1) {
					if (sentence.tokens().get(i).lemma().equals("do") && !sentence.tokens().get(i+1).tag().startsWith("V") && (!sentence.tokens().get(i+1).tag().equals("RB"))) {
						if (i>0 && !sentence.tokens().get(i-1).tag().equals("WP") && !isPunctuationMark(sentence.tokens().get(i-1))) {
							Map<String, BigDecimal> scoreMap = getBiberAnnotation(sentence.tokens().get(i));
							addToScoreMap(scoreMap, "PROD", new BigDecimal(1));
							sentence.tokens().get(i).set(getAnnotationClass(), scoreMap);
						}
					}					
				}
			}
		}
	}
	

	public void annotate_WHQU(Annotation annotation) {
/*
Any punctuation followed by a WH word (what, where, when, how, whether, why, whoever,
whomever, whichever, wherever, whenever, whatever, however) and followed by any
auxiliary verb (modal verbs in the form of MD tags or forms of DO or forms of HAVE or
forms of BE). This algorithm was slightly changed by allowing an intervening word between
the punctuation mark and the WH word. This allows WH-questions containing discourse
markers such as ‘so’ or ‘anyways’ to be recognised. Furthermore, Biber’s algorithm was
improved by excluding WH words such as however or whatever that do not introduce WHquestions.  
 */

		List<String> posTags = Arrays.asList("WP", "WP$", "WDT");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (posTags.contains(token.tag())) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "WHQU", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}

	}
	
	
	public void annotate_NOMZ(Annotation annotation) {
		List<String> tags = Arrays.asList("tion", "ment", "ness", "ity", "tions", "ments", "nesses", "ities");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				for (String ending : tags) {
					if (token.lemma().endsWith(ending) && (token.lemma().length() > (ending.length()+2))) {
						Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
						addToScoreMap(scoreMap, "NOMZ", new BigDecimal(1));
						token.set(getAnnotationClass(), scoreMap);
					}
				}
			}
		}
	}
	

	public void annotate_GER(Annotation annotation) {
		//List<String> tags = Arrays.asList("ing", "ings");
		List<String> posTags = Arrays.asList("VBG");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				//for (String ending : tags) {
					//if (token.lemma().endsWith(ending) && (token.lemma().length() > 10)) {
				if (posTags.contains(token.tag())) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "GER", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	
	//DONE - Must run after GER and NOMZ
	public void annotate_NN(Annotation annotation) {
		List<String> posTags = Arrays.asList("NN", "NNS", "NNP", "NNPS");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (posTags.contains(token.tag())) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					if (scoreMap.get("GER") == null && scoreMap.get("NOMZ") == null) {
						addToScoreMap(scoreMap, "NN", new BigDecimal(1));
						token.set(getAnnotationClass(), scoreMap);
					}
				}
			}
		}
	}
	

	public void annotate_PASS(Annotation annotation) {
/*
This tag is assigned when one of the two following patterns is found: (a) any form of BE
followed by a participle (VBN or VBD) plus one or two optional intervening adverbs (RB) or
negations; (b) any form of BE followed by a nominal form (a noun, NN, NNP or personal
pronoun, PRP) and a participle (VBN or VBD). This algorithm was slightly changed from
Biber’s version in the present tagger. It was felt necessary to implement the possibility of an
intervening negation in the pattern (b). This tag is therefore assigned also in the cases in
which a negation precedes the nominal form of pattern (b).  
 */
		List<String> posTags = Arrays.asList("VBN", "VBD");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			boolean wasBe = false;
			for (CoreLabel token : sentence.tokens()) {
				if (posTags.contains(token.tag()) && wasBe) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "PASS", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
				if (token.lemma().equals("be")) {
					wasBe = true;
				}
				else {
					wasBe = false;
				}
			}
		}
	}
	
	//DONE - Must run after PASS
	public void annotate_BYPA(Annotation annotation) {
		List<String> tags = Arrays.asList("by");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			boolean wasPASS = false;
			for (CoreLabel token : sentence.tokens()) {
				if (tags.contains(token.lemma()) && wasPASS) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "BYPA", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
				if (getBiberAnnotation(token).get("PASS") != null) {
					wasPASS = true;
				}
				else {
					wasPASS = false;
				}
			}
		}
	}
	

	public void annotate_BEMA(Annotation annotation) {
/*
BE is tagged as being a main verb in the following pattern: BE followed by a determiner
(DT), or a possessive pronoun (PRP$) or a preposition (PIN) or an adjective (JJ). This
algorithm was improved in the present tagger by taking into account that adverbs or negations
can appear between the verb BE and the rest of the pattern. Furthermore, the algorithm was
slightly modified and improved: (a) the problem of a double-coding of any Existential there
followed by a form of BE as a BEMA was solved by imposing the condition that there should
not appear before the pattern; (b) the cardinal numbers (CD) tag and the personal pronoun
(PRP) tag were added to the list of items that can follow the form of BE. 
 */
		List<String> posTags = Arrays.asList("DT", "PRP$", "IN", "JJ");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (int i=0;i<sentence.tokens().size();i++) {
				if (i<sentence.tokens().size()-1) {
					if (sentence.tokens().get(i).lemma().equals("be") && !posTags.contains(sentence.tokens().get(i+1).tag())) {
						Map<String, BigDecimal> scoreMap = getBiberAnnotation(sentence.tokens().get(i));
						addToScoreMap(scoreMap, "BEMA", new BigDecimal(1));
						sentence.tokens().get(i).set(getAnnotationClass(), scoreMap);
					}					
				}
			}
		}
	}
	
	public void annotate_EX(Annotation annotation) {
		List<String> posTags = Arrays.asList("EX");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (posTags.contains(token.tag())) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "EX", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	

	public void annotate_THVC(Annotation annotation) {
/*
This tag is assigned when the word that is: (1) preceded by and, nor, but, or, also or any
punctuation mark and followed by a determiner (DT, QUAN, CD), a pronoun (PRP), there, a
plural noun (NNS) or a proper noun (NNP); (2) preceded by a public, private or suasive verb
or a form of seem or appear and followed by any word that is NOT a verb (V), auxiliary verb
(MD, form of DO, form of HAVE, form of BE), a punctuation or the word and; (3) preceded
by a public, private or suasive verb or a form of seem or appear and a preposition and up to
four words that are not nouns (N). 
 */
		TokenSequencePattern pattern= TokenSequencePattern.compile("[and | nor | but | or | also] [that] ");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			List<CoreLabel> tokens = sentence.tokens();

			TokenSequenceMatcher matcher = pattern.getMatcher(tokens);
			while (matcher.find()) {
				List<CoreMap> matchedTokens = matcher.groupNodes();
				CoreMap token = null;
				for (CoreMap t : matchedTokens) {
					token = t;
				}
				if (token != null) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "THVC", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}

	
	public void annotate_THAC(Annotation annotation) {

		List<String> tags = Arrays.asList("that");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			boolean wasJJPRED = false;
			for (CoreLabel token : sentence.tokens()) {
				if (tags.contains(token.lemma()) && wasJJPRED) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "THAC", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
				if (token.tag().equals("JJ") || token.tag().equals("PRED")) {
					wasJJPRED = true;
				}
				else {
					wasJJPRED = false;
				}
			}
		}
	}


	public void annotate_WHCL(Annotation annotation) {
/*
(e.g. I believed what he told me)
This tag is assigned when the following pattern is found: any public, private or suasive verb
followed by any WH word, followed by a word that is NOT an auxiliary (tag MD for modal
verbs, or a form of DO, or a form of HAVE, or a form of BE). 
 */
		TokenSequencePattern pattern= TokenSequencePattern.compile("[{pos:WP}] [{pos:PRP} | {pos:NNP} | {pos:NNPS} | {pos:NN} | {pos:NNS}] [{pos:VB } | {pos:VBD} | {pos:VBG} | {pos:VBN} | {pos:VBP} | {pos:VBZ} ]");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			List<CoreLabel> tokens = sentence.tokens();

			TokenSequenceMatcher matcher = pattern.getMatcher(tokens);
			while (matcher.find()) {
				List<CoreMap> matchedTokens = matcher.groupNodes();
				CoreMap token = null;
				for (CoreMap t : matchedTokens) {
					token = t;
				}
				if (token != null) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "WHCL", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}



	public void annotate_TO(Annotation annotation) {
/*
The tag for infinitives is the Stanford Tagger Treebank tag TO. The Stanford Tagger does not
distinguish when the word to is used as an infinitive marker or a preposition. Therefore, an
algorithm was implemented to identify instances of to as preposition. This algorithm finds
any occurrence of to followed by a subordinator (IN), a cardinal number (CD), a determiner
(DT), an adjective (JJ), a possessive pronoun (PRP$), WH words (WP$, WDT, WP, WRB), a
pre-determiner (PDT), a noun (N, NNS, NP, NPs), or a pronoun (PRP) and tags it as a
preposition. The remaining instances of to are considered as being infinitive markers and are
therefore identifying occurrences of infinitive clauses. 
 */
		TokenSequencePattern pattern= TokenSequencePattern.compile("[{pos:TO}] [{pos:VB}]");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			List<CoreLabel> tokens = sentence.tokens();

			TokenSequenceMatcher matcher = pattern.getMatcher(tokens);
			while (matcher.find()) {
				List<CoreMap> matchedTokens = matcher.groupNodes();
				CoreMap token = null;
				for (CoreMap t : matchedTokens) {
					token = t;
				}
				if (token != null) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "TO", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	

	public void annotate_PRESP(Annotation annotation) {
/*
(e.g. Stuffing his mouth with cookies, Joe ran out the door)
This tag is assigned when the following pattern is found: a punctuation mark is followed by a
present participial form of a verb (VBG) followed by a preposition (PIN), a determiner (DT,
QUAN, CD), a WH pronoun, a WH possessive pronoun (WP$), any WH word, any pronoun
(PRP) or any adverb (RB). 
 */
		List<String> posTags = Arrays.asList("VBG");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (posTags.contains(token.tag())) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "PRESP", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}

	
	public void annotate_PASTP(Annotation annotation) {
/*
(e.g. Built in a single week, the house would stand for fifty years)
This tag is assigned when the following pattern is found: a punctuation mark followed by a
past participial form of a verb (VBN) followed by a preposition (PIN) or an adverb (RB). 
 */
		List<String> posTags = Arrays.asList("VBN");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (posTags.contains(token.tag())) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "PASTP", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	

	public void annotate_WZPAST(Annotation annotation) {
/*
(e.g. The solution produced by this process)
This tag is assigned when the following pattern is found: a noun (N) or quantifier pronoun
(QUPR) followed by a past participial form of a verb (VBN) followed by a preposition (PIN)
or an adverb (RB) or a form of BE. 
 */
		TokenSequencePattern pattern= TokenSequencePattern.compile("[{pos:NN} | {pos:NNS}] [{pos:VBN}]");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			List<CoreLabel> tokens = sentence.tokens();

			TokenSequenceMatcher matcher = pattern.getMatcher(tokens);
			while (matcher.find()) {
				List<CoreMap> matchedTokens = matcher.groupNodes();
				CoreMap token = null;
				for (CoreMap t : matchedTokens) {
					token = t;
				}
				if (token != null) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "WZPAST", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	

	public void annotate_WZPRES(Annotation annotation) {
/*
(e.g. the event causing this decline is….)
This tag is assigned a present participial form of a verb (VBG) is preceded by a noun (NN).
 */
		TokenSequencePattern pattern= TokenSequencePattern.compile("[{pos:NN} | {pos:NNS}] [{pos:VBG}]");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			List<CoreLabel> tokens = sentence.tokens();
			TokenSequenceMatcher matcher = pattern.getMatcher(tokens);
			while (matcher.find()) {
				List<CoreMap> matchedTokens = matcher.groupNodes();
				CoreMap token = null;
				for (CoreMap t : matchedTokens) {
					token = t;
				}
				if (token != null) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "WZPRES", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	

	public void annotate_TSUB(Annotation annotation) {
/*
(e.g. the dog that bit me)
These are occurrences of that preceded by a noun (N) and followed by an auxiliary verb or a
verb (V), with the possibility of an intervening adverb (RB) or negation (XX0). 
 */
		TokenSequencePattern pattern= TokenSequencePattern.compile("[that] [{pos:VB } | {pos:VBD} | {pos:VBG} | {pos:VBN} | {pos:VBP} | {pos:VBZ}]");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			List<CoreLabel> tokens = sentence.tokens();

			TokenSequenceMatcher matcher = pattern.getMatcher(tokens);
			while (matcher.find()) {
				List<CoreMap> matchedTokens = matcher.groupNodes();
				CoreMap token = null;
				for (CoreMap t : matchedTokens) {
					token = t;
				}
				if (token != null) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "TSUB", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	

	public void annotate_TOBJ(Annotation annotation) {
/*
(e.g. the dog that I saw)
These are occurrences of that preceded by a noun and followed by a determiner (DT, QUAN,
CD), a subject form of a personal pronoun, a possessive pronoun (PRP$), the pronoun it, an
adjective (JJ), a plural noun (NNS), a proper noun (NNP) or a possessive noun (a noun (N)
followed by a genitive marker (POS)). As Biber specifies, however, this algorithm does not
distinguish between simple complements to nouns and true relative clauses. 
 */
		TokenSequencePattern pattern= TokenSequencePattern.compile("[that] [{pos:NN } | {pos:NNP} | {pos:PRP}]");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			List<CoreLabel> tokens = sentence.tokens();

			TokenSequenceMatcher matcher = pattern.getMatcher(tokens);
			while (matcher.find()) {
				List<CoreMap> matchedTokens = matcher.groupNodes();
				CoreMap token = null;
				for (CoreMap t : matchedTokens) {
					token = t;
				}
				if (token != null) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "TOBJ", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	

	public void annotate_WHSUB(Annotation annotation) {
/*
(e.g. the man who likes popcorn)
This tag is assigned when the following pattern is found: any word that is NOT a form of the
words ASK or TELL followed by a noun (N), then a WH pronoun, then by any verb or
auxiliary verb (V), with the possibility of an intervening adverb (RB) or negation (XX0)
between the WH pronoun and the verb. 
 */
		TokenSequencePattern pattern= TokenSequencePattern.compile("[{pos:WP}] [{pos:VB } | {pos:VBD} | {pos:VBG} | {pos:VBN} | {pos:VBP} | {pos:VBZ} ]");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			List<CoreLabel> tokens = sentence.tokens();

			TokenSequenceMatcher matcher = pattern.getMatcher(tokens);
			while (matcher.find()) {
				List<CoreMap> matchedTokens = matcher.groupNodes();
				CoreMap token = null;
				for (CoreMap t : matchedTokens) {
					token = t;
				}
				if (token != null) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "WHSUB", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	

	public void annotate_WHOBJ(Annotation annotation) {
/*
(e.g. the man who Sally likes)
This tag is assigned when the following pattern is found: any word that is NOT a form of the
words ASK or TELL followed by any word, followed by a noun (N), followed by any word
that is NOT an adverb (RB), a negation (XX0) , a verb or an auxiliary verb (MD, forms of
HAVE, BE or DO). 
 */
		TokenSequencePattern pattern= TokenSequencePattern.compile("[{pos:WP}] [{pos:NN } | {pos:NNP} | {pos:PRP}]");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			List<CoreLabel> tokens = sentence.tokens();

			TokenSequenceMatcher matcher = pattern.getMatcher(tokens);
			while (matcher.find()) {
				List<CoreMap> matchedTokens = matcher.groupNodes();
				CoreMap token = null;
				for (CoreMap t : matchedTokens) {
					token = t;
				}
				if (token != null) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "WHOBJ", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}

	public void annotate_PIRE(Annotation annotation) {
		try {
			List<String> posTags = Arrays.asList("who", "whom", "whose", "which");
			List<String> tags = ResourceUtils.getInstance(getParameterStore()).getList("io/outofprintmagazine/nlp/models/Biber/PIN.txt");
			CoreDocument document = new CoreDocument(annotation);
			for (CoreSentence sentence : document.sentences()) {
				boolean wasPIN = false;
				for (CoreLabel token : sentence.tokens()) {
					if (posTags.contains(token.lemma()) && wasPIN) {
						Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
						addToScoreMap(scoreMap, "PIRE", new BigDecimal(1));
						token.set(getAnnotationClass(), scoreMap);
					}
					if (tags.contains(token.lemma())) {
						wasPIN = true;
					}
					else {
						wasPIN = false;
					}
				}
			}
		}
		catch (Throwable t) {
			logger.error(t);
		}
	}
	
	public void annotate_SERE(Annotation annotation) {
		List<String> tags = Arrays.asList("which");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			boolean wasPunct = false;
			for (CoreLabel token : sentence.tokens()) {
				if (tags.contains(token.lemma()) && wasPunct) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "SERE", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
				if (isPunctuationMark(token)) {
					wasPunct = true;
				}
				else {
					wasPunct = false;
				}
			}
		}
	}
	
	public void annotate_CAUS(Annotation annotation) {
		List<String> tags = Arrays.asList("because");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (tags.contains(token.lemma())) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "CAUS", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	
	public void annotate_CONC(Annotation annotation) {
		List<String> tags = Arrays.asList("although", "though", "tho");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (tags.contains(token.lemma())) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "CONC", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	
	public void annotate_COND(Annotation annotation) {
		List<String> tags = Arrays.asList("if", "unless");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (tags.contains(token.lemma())) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "COND", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	

	public void annotate_OSUB(Annotation annotation) {
/*
This tag identifies any occurrence of the words: since, while, whilst, whereupon, whereas,
whereby, such that, so that (followed by a word that is neither a noun nor an adjective), such
that (followed by a word that is neither a noun nor an adjective), inasmuch as, forasmuch as,
insofar as, insomuch as, as long as, as soon as. In cases of multi-word units such as as long
as, only the first word is tagged as OSUB and the other words are tagged with the tag NULL.
 */
		List<String> tags = Arrays.asList("since", "while", "whilst", "whereupon", "whereas", "whereby", "inasmuch", "forasmuch", "insofar", "insomuch");
		List<String> posTags = Arrays.asList("NN", "NNS", "NNP","NNPS", "JJ", "JJR", "JJS");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (int i=0;i<sentence.tokens().size();i++) {
				if (tags.contains(sentence.tokens().get(i).lemma())) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(sentence.tokens().get(i));
					addToScoreMap(scoreMap, "OSUB", new BigDecimal(1));
					sentence.tokens().get(i).set(getAnnotationClass(), scoreMap);
				}
				if (i<sentence.tokens().size()-2) {
					if ((sentence.tokens().get(i).lemma().equals("such") || sentence.tokens().get(i).lemma().equals("so")) && sentence.tokens().get(i+1).lemma().equals("that") && !posTags.contains(sentence.tokens().get(i+2).tag())) {
						Map<String, BigDecimal> scoreMap = getBiberAnnotation(sentence.tokens().get(i));
						addToScoreMap(scoreMap, "OSUB", new BigDecimal(1));
						sentence.tokens().get(i).set(getAnnotationClass(), scoreMap);
					}					
				}
			}
		}
	}
	
	public void annotate_PIN(Annotation annotation) {
		try {
			List<String> tags = ResourceUtils.getInstance(getParameterStore()).getList("io/outofprintmagazine/nlp/models/Biber/PIN.txt");
			CoreDocument document = new CoreDocument(annotation);
			for (CoreSentence sentence : document.sentences()) {
				for (CoreLabel token : sentence.tokens()) {
					if (tags.contains(token.lemma())) {
						Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
						addToScoreMap(scoreMap, "PIN", new BigDecimal(1));
						token.set(getAnnotationClass(), scoreMap);
					}
				}
			}
		}
		catch (Throwable t) {
			logger.error(t);
		}
	}
	
	public void annotate_JJ(Annotation annotation) {
		List<String> posTags = Arrays.asList("JJ", "JJS", "JJR");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (posTags.contains(token.tag())) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "JJ", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	

	public void annotate_PRED(Annotation annotation) {
/*
(e.g. the horse is big)
The tagger tags as PRED the adjectives that are found in the following pattern: any form of
BE followed by an adjective (JJ) followed by a word that is NOT another adjective, an
adverb (RB) or a noun (N). If any adverb or negation is intervening between the adjective and
the word after it, the tag is still assigned. A modification to Biber’s algorithm was
implemented in the present tagger to improve its accuracy. An adjective is tagged as
predicative if it is preceded by another predicative adjective followed by a phrasal
coordinator (see below). This pattern accounts for cases such as: the horse is big and fast. 
 */
		List<String> posTags = Arrays.asList("JJ");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			boolean wasBe = false;
			for (CoreLabel token : sentence.tokens()) {
				if (posTags.contains(token.tag()) && wasBe) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "PRED", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
				if (token.lemma().equals("be")) {
					wasBe = true;
				}
				else {
					wasBe = false;
				}
			}
		}
	}
	
	public void annotate_RB(Annotation annotation) {
		List<String> posTags = Arrays.asList("RB", "RBS", "RBR", "WRB");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (posTags.contains(token.tag())) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "RB", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	
	public void annotate_AWL(Annotation annotation) {
		int words = 0;
		int characters = 0;
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (!isPunctuationMark(token)) {
					words++;
					characters += token.originalText().length();
				}
			}
		}
		try {
			if (characters > 0 && words > 0) {
				Map<String, BigDecimal> scoreMap = getBiberAnnotation(annotation);
				addToScoreMap(scoreMap, "AWL", new BigDecimal(characters/words));
				annotation.set(getAnnotationClass(), scoreMap);
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
	}
	
	public void annotate_TTR(Annotation annotation) {
		int words = 0;
		List<String> typeCount = new ArrayList<String>();
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			if (words > 400) {
				break;
			}
			for (CoreLabel token : sentence.tokens()) {
				if (!isPunctuationMark(token)) {
					words++;
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(annotation);
					for (String key : scoreMap.keySet()) {
						if (!typeCount.contains(key)) {
							typeCount.add(key);
						}
						
					}
				}
			}
		}
		try {
			if (words > 0) {
				Map<String, BigDecimal> scoreMap = getBiberAnnotation(annotation);
				addToScoreMap(scoreMap, "TTR", new BigDecimal(typeCount.size()/words));
				annotation.set(getAnnotationClass(), scoreMap);
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
	}
	

	public void annotate_CONJ(Annotation annotation) {
/*
This tag finds any of the items in this list: punctuation+else, punctuation+altogether,
punctuation+rather, alternatively, consequently, conversely, e.g., furthermore, hence,
however, i.e., instead, likewise, moreover, namely, nevertheless, nonetheless,
notwithstanding, otherwise, similarly, therefore, thus, viz., in comparison, in contrast, in
particular, in addition, in conclusion, in consequence, in sum, in summary, for example, for 
instance, instead of, by contrast, by comparison, in any event, in any case, in other words, as
a result, as a consequence, on the contrary, on the other hand.
Some minor inconsistencies in the said list were fixed. For example, Biber lists the word
rather two times in this list, making the second mentions redundant. Rather was counted only
when it appeared after a punctuation mark. The same applies for altogether. In cases of multiword units such as on the other hand, only the first word is tagged as OSUB and the other
words are tagged with the tag NULL. 
 */
		try {
			List<String> tags = ResourceUtils.getInstance(getParameterStore()).getList("io/outofprintmagazine/nlp/models/Biber/CONJ.txt");;
			CoreDocument document = new CoreDocument(annotation);
			for (CoreSentence sentence : document.sentences()) {
				for (CoreLabel token : sentence.tokens()) {
					if (tags.contains(token.lemma())) {
						Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
						addToScoreMap(scoreMap, "CONJ", new BigDecimal(1));
						token.set(getAnnotationClass(), scoreMap);
					}
				}
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
	}
	
	public void annotate_DWNT(Annotation annotation) {
		List<String> tags = Arrays.asList("almost", "barely", "hardly", "merely", "mildly", "nearly", "only", "partially", "partly", "practically", "scarcely", "slightly", "somewhat");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (tags.contains(token.lemma())) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "DWNT", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	

	public void annotate_HDG(Annotation annotation) {
/*
This tag finds any of the items in this list: maybe, at about, something like, more or less, sort
of, kind of (these two items must be preceded by a determiner (DT), a quantifier (QUAN), a
cardinal number (CD), an adjective (JJ or PRED), a possessive pronouns (PRP$) or WH word
(see entry on WH-questions)). In cases of multi-word units such as more or less, only the first
word is tagged as HDG and the other words are tagged with the tag NULL. 
 */
		List<String> posTags = Arrays.asList("NN", "NNS", "NNP", "NNPS");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (int i=0;i<sentence.tokens().size();i++) {
				if (sentence.tokens().get(i).lemma().equals("maybe")) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(sentence.tokens().get(i));
					addToScoreMap(scoreMap, "HDG", new BigDecimal(1));
					sentence.tokens().get(i).set(getAnnotationClass(), scoreMap);
				}
				if (i<sentence.tokens().size()-1) {
					if (sentence.tokens().get(i).lemma().equals("at") && sentence.tokens().get(i+1).lemma().equals("about")) {
						Map<String, BigDecimal> scoreMap = getBiberAnnotation(sentence.tokens().get(i));
						addToScoreMap(scoreMap, "HDG", new BigDecimal(1));
						sentence.tokens().get(i).set(getAnnotationClass(), scoreMap);
					}
					if (sentence.tokens().get(i).lemma().equals("something") && sentence.tokens().get(i+1).lemma().equals("like")) {
						Map<String, BigDecimal> scoreMap = getBiberAnnotation(sentence.tokens().get(i));
						addToScoreMap(scoreMap, "HDG", new BigDecimal(1));
						sentence.tokens().get(i).set(getAnnotationClass(), scoreMap);
					}
				}
				if (i<sentence.tokens().size()-2) {				
					if (sentence.tokens().get(i).lemma().equals("more") && sentence.tokens().get(i+1).lemma().equals("or") && sentence.tokens().get(i+2).lemma().equals("less")) {
						Map<String, BigDecimal> scoreMap = getBiberAnnotation(sentence.tokens().get(i));
						addToScoreMap(scoreMap, "HDG", new BigDecimal(1));
						sentence.tokens().get(i).set(getAnnotationClass(), scoreMap);
					}
					if ((sentence.tokens().get(i).lemma().equals("sort") || sentence.tokens().get(i).lemma().equals("kind")) && sentence.tokens().get(i+1).lemma().equals("of") && !posTags.contains(sentence.tokens().get(i+2).tag())) {
						Map<String, BigDecimal> scoreMap = getBiberAnnotation(sentence.tokens().get(i));
						addToScoreMap(scoreMap, "HDG", new BigDecimal(1));
						sentence.tokens().get(i).set(getAnnotationClass(), scoreMap);
					}
				}
			}
		}
	}
	
	public void annotate_AMP(Annotation annotation) {
		List<String> tags = Arrays.asList("absolutely", "altogether", "completely", "enormously", "entirely", "extremely", "fully", "greatly", "highly", "intensely", "perfectly", "strongly", "thoroughly", "totally",	"utterly", "very");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (tags.contains(token.lemma())) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "AMP", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	

	public void annotate_EMPH(Annotation annotation) {
/*
This tag finds any of the items in this list: just, really, most, more, real+adjective,
so+adjective, any form of DO followed by a verb, for sure, a lot, such a. In cases of multiword units such as a lot, only the first word is tagged as OSUB and the other words are
tagged with the tag NULL. 
 */
		List<String> tags = Arrays.asList("just", "really", "most", "more");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (int i=0;i<sentence.tokens().size();i++) {
				if (tags.contains(sentence.tokens().get(i).lemma())) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(sentence.tokens().get(i));
					addToScoreMap(scoreMap, "EMPH", new BigDecimal(1));
					sentence.tokens().get(i).set(getAnnotationClass(), scoreMap);
				}
				if (i<sentence.tokens().size()-1) {
					if (
							(sentence.tokens().get(i).lemma().equals("for") && sentence.tokens().get(i+1).lemma().equals("sure"))
							||
							(sentence.tokens().get(i).lemma().equals("a") && sentence.tokens().get(i+1).lemma().equals("lot"))
							||
							(sentence.tokens().get(i).lemma().equals("such") && sentence.tokens().get(i+1).lemma().equals("a"))
							||
							(sentence.tokens().get(i).lemma().equals("real") && sentence.tokens().get(i+1).tag().equals("JJ"))
							||
							(sentence.tokens().get(i).lemma().equals("so") && sentence.tokens().get(i+1).lemma().equals("JJ"))
							||
							(sentence.tokens().get(i).lemma().equals("do") && sentence.tokens().get(i+1).tag().startsWith("V"))
						) {
						Map<String, BigDecimal> scoreMap = getBiberAnnotation(sentence.tokens().get(i));
						addToScoreMap(scoreMap, "EMPH", new BigDecimal(1));
						sentence.tokens().get(i).set(getAnnotationClass(), scoreMap);
					}
				}
			}
		}

	}
	
	public void annotate_DPAR(Annotation annotation) {
		List<String> tags = Arrays.asList("well", "now", "anyhow", "anyways");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			boolean wasPunct = false;
			for (CoreLabel token : sentence.tokens()) {
				if (tags.contains(token.lemma()) && wasPunct) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "DPAR", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
				if (isPunctuationMark(token)) {
					wasPunct = true;
				}
				else {
					wasPunct = false;
				}
			}
		}
	}
	
	//DONE - Must run after DEMP, TOBJ, TSUB, THAC, THVC
	public void annotate_DEMO(Annotation annotation) {
		List<String> tags = Arrays.asList("this", "that", "these", "those");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (tags.contains(token.lemma())) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					if (scoreMap.get("DEMP") == null && scoreMap.get("TOBJ") == null && scoreMap.get("TSUB") == null && scoreMap.get("THAC") == null && scoreMap.get("THVC") == null) {
						addToScoreMap(scoreMap, "DEMO", new BigDecimal(1));
						token.set(getAnnotationClass(), scoreMap);
					}
				}
			}
		}
	}
	
	public void annotate_POMD(Annotation annotation) {
		List<String> tags = Arrays.asList("can", "may", "might", "could");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (tags.contains(token.lemma())) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "POMD", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	
	public void annotate_NEMD(Annotation annotation) {
		List<String> tags = Arrays.asList("ought", "should", "must");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (tags.contains(token.lemma())) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "NEMD", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	
	public void annotate_PRMD(Annotation annotation) {
		List<String> tags = Arrays.asList("will", "would", "shall");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (tags.contains(token.lemma())) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "PRMD", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	
	public void annotate_PUBV(Annotation annotation) {
		try {
			List<String> posTags = Arrays.asList("VB", "VBD", "VBG", "VBN", "VBP", "VBZ");
			List<String> tags = ResourceUtils.getInstance(getParameterStore()).getList("io/outofprintmagazine/nlp/models/Biber/PUBV.txt");
			CoreDocument document = new CoreDocument(annotation);
			for (CoreSentence sentence : document.sentences()) {
				for (CoreLabel token : sentence.tokens()) {
					if (posTags.contains(token.tag()) && tags.contains(token.lemma())) {
						Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
						addToScoreMap(scoreMap, "PUBV", new BigDecimal(1));
						token.set(getAnnotationClass(), scoreMap);
					}
				}
			}
		}
		catch (Throwable t) {
			logger.error(t);
		}
	}
	
	public void annotate_PRIV(Annotation annotation) {
		try {
			List<String> posTags = Arrays.asList("VB", "VBD", "VBG", "VBN", "VBP", "VBZ");
			List<String> tags = ResourceUtils.getInstance(getParameterStore()).getList("io/outofprintmagazine/nlp/models/Biber/PRIV.txt");
			CoreDocument document = new CoreDocument(annotation);
			for (CoreSentence sentence : document.sentences()) {
				for (CoreLabel token : sentence.tokens()) {
					if (posTags.contains(token.tag()) && tags.contains(token.lemma())) {
						Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
						addToScoreMap(scoreMap, "PRIV", new BigDecimal(1));
						token.set(getAnnotationClass(), scoreMap);
					}
				}
			}
		}
		catch (Throwable t) {
			logger.error(t);
		}
	}
	
	public void annotate_SUAV(Annotation annotation) {
		try {
			List<String> posTags = Arrays.asList("VB", "VBD", "VBG", "VBN", "VBP", "VBZ");
			List<String> tags = ResourceUtils.getInstance(getParameterStore()).getList("io/outofprintmagazine/nlp/models/Biber/SUAV.txt");
			CoreDocument document = new CoreDocument(annotation);
			for (CoreSentence sentence : document.sentences()) {
				for (CoreLabel token : sentence.tokens()) {
					if (posTags.contains(token.tag()) && tags.contains(token.lemma())) {
						Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
						addToScoreMap(scoreMap, "SUAV", new BigDecimal(1));
						token.set(getAnnotationClass(), scoreMap);
					}
				}
			}
		}
		catch (Throwable t) {
			logger.error(t);
		}
	}
	
	
	public void annotate_SEMP(Annotation annotation) {
		try {
			List<String> posTags = Arrays.asList("VB", "VBD", "VBG", "VBN", "VBP", "VBZ");
			List<String> tags = Arrays.asList("seem", "appear");
			CoreDocument document = new CoreDocument(annotation);
			for (CoreSentence sentence : document.sentences()) {
				for (CoreLabel token : sentence.tokens()) {
					if (posTags.contains(token.tag()) && tags.contains(token.lemma())) {
						Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
						addToScoreMap(scoreMap, "SEMP", new BigDecimal(1));
						token.set(getAnnotationClass(), scoreMap);
					}
				}
			}
		}
		catch (Throwable t) {
			logger.error(t);
		}
	}
	

	public void annotate_CONT(Annotation annotation) {
/*
The contractions were tagged by identifying any instance of apostrophe followed by a tagged
word OR any instance of the item n’t.
 */
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (token.lemma().equals("not") && token.originalText().equals("n\'t")) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "CONT", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	
	
	//TODO
	public void annotate_THATD(Annotation annotation) {
/*
The tag THATD is added when one of the following patterns is found: (1) a public, private or
suasive verb followed by a demonstrative pronoun (DEMP) or a subject form of a personal
pronoun; (2) a public, private or suasive verb is followed by a pronoun (PRP) or a noun (N)
and then by a verb (V) or auxiliary verb; (3) a public, private or suasive verb is followed by
an adjective (JJ or PRED), an adverb (RB), a determiner (DT, QUAN, CD) or a possessive
pronoun (PRP$) and then a noun (N) and then a verb or auxiliary verb, with the possibility of
an intervening adjective (JJ or PRED) between the noun and its preceding word.
 */
	}
	
	public void annotate_STPR(Annotation annotation) {
		List<String> posTags = Arrays.asList("IN");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			boolean wasPIN = false;
			for (CoreLabel token : sentence.tokens()) {
				if (wasPIN && isPunctuationMark(token)) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "STPR", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
				if (posTags.contains(token.tag())) {
					wasPIN = true;
				}
				else {
					wasPIN = false;
				}
			}
		}
	}
	

	public void annotate_SPIN(Annotation annotation) {
/*
(e.g. he wants to convincingly prove that…)
Split infinitives are identified every time an infinitive marker to is followed by one or two
adverbs and a verb base form. 
 */
		TokenSequencePattern pattern= TokenSequencePattern.compile("[{pos:TO}] [{pos:RB}] [{pos:VB}]");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			List<CoreLabel> tokens = sentence.tokens();

			TokenSequenceMatcher matcher = pattern.getMatcher(tokens);
			while (matcher.find()) {
				List<CoreMap> matchedTokens = matcher.groupNodes();
				CoreMap token = null;
				for (CoreMap t : matchedTokens) {
					token = t;
				}
				if (token != null) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "SPIN", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	

	public void annotate_SPAU(Annotation annotation) {
/*
(e.g. they are objectively shown that…)
Split auxiliaries are identified every time an auxiliary (any modal verb MD, or any form of
DO, or any form of BE, or any form of HAVE) is followed by one or two adverbs and a verb
base form. 
 */
		
		TokenSequencePattern pattern= TokenSequencePattern.compile("[{pos:MD}] [{pos:RB}] [{pos:VB}]");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			List<CoreLabel> tokens = sentence.tokens();

			TokenSequenceMatcher matcher = pattern.getMatcher(tokens);
			while (matcher.find()) {
				List<CoreMap> matchedTokens = matcher.groupNodes();
				CoreMap token = null;
				for (CoreMap t : matchedTokens) {
					token = t;
				}
				if (token != null) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "SPAU", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}
	

	public void annotate_PHC(Annotation annotation) {
		List<String> posTags = Arrays.asList("NN", "NNS", "NNP", "NNPS", "JJ", "JJR", "JJS", "RB", "RBR", "RBS", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (int i=0;i<sentence.tokens().size();i++) {
				if (i>0 && i<sentence.tokens().size()-1) {
					if (sentence.tokens().get(i).lemma().equals("and") && posTags.contains(sentence.tokens().get(i-1).tag()) && sentence.tokens().get(i-1).tag().equals(sentence.tokens().get(i+1).tag())) {
						Map<String, BigDecimal> scoreMap = getBiberAnnotation(sentence.tokens().get(i));
						addToScoreMap(scoreMap, "PHC", new BigDecimal(1));
						sentence.tokens().get(i).set(getAnnotationClass(), scoreMap);
					}					
				}
			}
		}
	}
	
	//TODO
	public void annotate_ANDC(Annotation annotation) {
/*
This tag is assigned to the word and when it is found in one of the following patterns: (1)
preceded by a comma and followed by it, so, then, you, there + BE, or a demonstrative 
pronoun (DEMP) or the subject forms of a personal pronouns; (2) preceded by any
punctuation; (3) followed by a WH pronoun or any WH word, an adverbial subordinator
(CAUS, CONC, COND, OSUB) or a discourse particle (DPAR) or a conjunct (CONJ). 
 */
	}
	
	public void annotate_SYNE(Annotation annotation) {
		List<String> posTags = Arrays.asList("JJ", "PRED", "NN", "NNS", "NNP", "NNPS");
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			boolean wasNot = false;
			for (CoreLabel token : sentence.tokens()) {
				if (wasNot && posTags.contains(token.tag())) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "SYNE", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
				if (token.lemma().equals("no") || token.lemma().equals("not") || token.lemma().equals("neither")) {
					wasNot = true;
				}
				else {
					wasNot = false;
				}
			}
		}
	}
	
	public void annotate_XX0(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (token.lemma().equals("not")) {
					Map<String, BigDecimal> scoreMap = getBiberAnnotation(token);
					addToScoreMap(scoreMap, "XX0", new BigDecimal(1));
					token.set(getAnnotationClass(), scoreMap);
				}
			}
		}
	}

	
}
