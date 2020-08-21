package io.outofprintmagazine.nlp.pipeline.annotators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import io.outofprintmagazine.nlp.pipeline.PhraseAnnotation;
import io.outofprintmagazine.nlp.pipeline.annotators.conditional.BecauseAnnotator;
import io.outofprintmagazine.nlp.pipeline.annotators.conditional.IfAnnotator;
import io.outofprintmagazine.nlp.pipeline.annotators.count.CharCountAnnotator;
import io.outofprintmagazine.nlp.pipeline.annotators.count.ParagraphCountAnnotator;
import io.outofprintmagazine.nlp.pipeline.annotators.count.SentenceCountAnnotator;
import io.outofprintmagazine.nlp.pipeline.annotators.count.SyllableCountAnnotator;
import io.outofprintmagazine.nlp.pipeline.annotators.count.TokenCountAnnotator;
import io.outofprintmagazine.nlp.pipeline.annotators.count.WordCountAnnotator;
import io.outofprintmagazine.nlp.pipeline.annotators.interrogative.HowAnnotator;
import io.outofprintmagazine.nlp.pipeline.annotators.interrogative.WhatAnnotator;
import io.outofprintmagazine.nlp.pipeline.annotators.interrogative.WhenAnnotator;
import io.outofprintmagazine.nlp.pipeline.annotators.interrogative.WhereAnnotator;
import io.outofprintmagazine.nlp.pipeline.annotators.interrogative.WhoAnnotator;
import io.outofprintmagazine.nlp.pipeline.annotators.interrogative.WhyAnnotator;
import io.outofprintmagazine.nlp.pipeline.annotators.simile.AsAnnotator;
import io.outofprintmagazine.nlp.pipeline.annotators.simile.LikeAnnotator;
import io.outofprintmagazine.nlp.utils.CoreNlpUtils;
import io.outofprintmagazine.util.IParameterStore;
import io.outofprintmagazine.util.ParameterStoreLocal;

public class Annotator_Test {

	public Annotator_Test() {
		super();
	}
	
	/**
	 * 	#http://wordnetcode.princeton.edu/wn3.1.dict.tar.gz
	 *  wordNet_location=dict
	 *  #http://verbs.colorado.edu/verb-index/vn/verbnet-3.3.tar.gz
	 *  verbNet_location=verbnet3.3/
     *
	 *	#https://www.mediawiki.org/wiki/API:Etiquette
	 *	wikipedia_apikey=OOPCoreNlp/0.9.1 httpclient/4.5.6
	*/
	private IParameterStore parameterStore = null;

	public IParameterStore getParameterStore() throws IOException {
		if (parameterStore == null) {
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode p = mapper.createObjectNode();
			p.put("wordNet_location", "../data/dict");
			p.put("verbNet_location", "../data/verbnet3.3");
			p.put("wikipedia_apikey", "OOPCoreNlp/0.9.1 httpclient/4.5.6");
			parameterStore = new ParameterStoreLocal(p);
		}
		return parameterStore;
	}
	
	private CoreDocument annotate(String text, IOOPAnnotator annotator) throws IOException {
		CoreDocument document = new CoreDocument(text);
		CoreNlpUtils.getInstance(getParameterStore()).getPipeline().annotate(document);
		annotator.init(getParameterStore());
		annotator.annotate(document.annotation());
		annotator.score(document);
		return document;
	}
	
	@SuppressWarnings("unused")
	private void printContents(Map<String,BigDecimal> score) {
		if (score != null) {
			for (Entry<String,BigDecimal> s : score.entrySet()) {
				System.out.println(String.format("%s : %s", s.getKey(), s.getValue()));
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void printContents(List<CoreLabel> tokens, IOOPAnnotator annotator) {
		if (tokens != null) {
			int i=0;
			for (CoreLabel token : tokens) {
				System.out.println(i++);
				printContents((Map<String,BigDecimal>)token.get(annotator.getAnnotationClass()));
			}
		}
		System.out.println("----------------------------------------");
	}
	
	@Test
	public void ActionlessVerbsAnnotator_Test() throws IOException {
		String text = "These are the times that try men's souls.";
		IOOPAnnotator annotator = new ActionlessVerbsAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		targetTokenIdx = 1;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "be";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	//NOT IN OSS
	//@Test
	//public void ActorsAnnotator_Test() throws IOException {
	//	assertEquals("Implemented", "todo");
	//}
	
	@Test
	public void AdjectiveCategoriesAnnotator_Test() throws IOException {
		String text = "The hungry fox was eaten by the hungrier wolf.";
		IOOPAnnotator annotator = new AdjectiveCategoriesAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);
		
		targetTokenIdx = 1;		
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "descriptive";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
		
		targetTokenIdx = 7;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "comparative";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));

	}
	
	@Test
	public void AdjectivesAnnotator_Test() throws IOException {
		String text = "The quick brown fox jumped over the lazy white dog.";
		IOOPAnnotator annotator = new AdjectivesAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);
		
		targetTokenIdx = 1;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "quick";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
		
		targetTokenIdx = 2;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "brown";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));

		targetTokenIdx = 7;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "lazy";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));

		
		targetTokenIdx = 8;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "white";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void AdverbCategoriesAnnotator_Test() throws IOException {
		String text = "The fox ran quickly, but the wolf ran quicker.";
		IOOPAnnotator annotator = new AdverbCategoriesAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		targetTokenIdx = 3;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "descriptive";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));

		
		targetTokenIdx = 9;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "comparative";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void AdverbsAnnotator_Test() throws IOException {
		String text = "The fox ran quickly, but the wolf ran quicker.";
		IOOPAnnotator annotator = new AdverbsAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		targetTokenIdx = 3;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "quickly";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
		
		targetTokenIdx = 9;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "quicker";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void AmericanizeAnnotator_Test() throws IOException {
		String text = "I agonised over this decision.";
		IOOPAnnotator annotator = new AmericanizeAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		targetTokenIdx = 1;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "agonized";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void AngliciseAnnotator_Test() throws IOException {
		String text = "I agonized over this decision.";
		IOOPAnnotator annotator = new AngliciseAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		targetTokenIdx = 1;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "agonised";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}	

	@Test
	public void BiberAnnotatorAMP_Test() throws IOException {
		String text = "I thoroughly enjoyed our conversation.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 1;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "AMP";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorAWL_Test() throws IOException {
		String text = "Four letter words are fun.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		score = (Map<String,BigDecimal>) document.annotation().get(annotator.getAnnotationClass());
		targetSubscoreName = "AWL";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(4), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorBEMA_Test() throws IOException {
		String text = "Rain is wet.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		targetTokenIdx = 1;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "BEMA";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorBYPA_Test() throws IOException {
		String text = "The crime was committed by person or persons unknown.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 4;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "BYPA";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorCAUS_Test() throws IOException {
		String text = "Because I said so.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 0;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "CAUS";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorCONC_Test() throws IOException {
		String text = "He did it, although we'll never be able to prove it.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 4;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "CONC";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorCOND_Test() throws IOException {
		String text = "Unless someone rats him out.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 0;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "COND";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorCONJ_Test() throws IOException {
		String text = "And furthermore, I don't like your trousers. Or your appalling taste in women.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 1;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "CONJ";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorCONT_Test() throws IOException {
		String text = "And futhermore, I don't like your trousers. Or your appalling taste in women.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 5;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "CONT";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorDEMO_Test() throws IOException {
		String text = "Dis, dat, dese, and those?";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 7;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "DEMO";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorDPAR_Test() throws IOException {
		String text = "That's how we feel about it around here, anyways.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 10;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "DPAR";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorDWNT_Test() throws IOException {
		String text = "Hardly seems worth it.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 0;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "DWNT";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorEMPH_Test() throws IOException {
		String text = "It's just a damn shame.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 2;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "EMPH";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorEX_Test() throws IOException {
		String text = "There is a place where I can go.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 0;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "EX";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorFPP1_Test() throws IOException {
		String text = "I'm the man who loves you.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 0;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "FPP1";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorGER_Test() throws IOException {
		String text = "Running away to get away ha ha ha ha you're wearing out your shoes.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 0;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "GER";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorHDG_Test() throws IOException {
		String text = "Maybe you're going to be the one that saves me.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 0;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "HDG";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorINPR_Test() throws IOException {
		String text = "Anyone who tells you different is a liar.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 0;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "INPR";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorJJ_Test() throws IOException {
		String text = "The quick brown fox jumped over the lazy white dog.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 1;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "JJ";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorNEMD_Test() throws IOException {
		String text = "We should go.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 1;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "NEMD";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorNN_Test() throws IOException {
		String text = "Snow is cold.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 0;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "NN";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorNOMZ_Test() throws IOException {
		String text = "Come on baby, do the locomotion with me.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 6;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "NOMZ";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorOSUB_Test() throws IOException {
		String text = "Since you've been gone, all I have left is this band of gold.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 0;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "OSUB";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorPASS_Test() throws IOException {
		String text = "The crime was committed by person or persons unknown.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 3;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "PASS";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorPASTP_Test() throws IOException {
		String text = "Rome wasn't built in a day.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		targetTokenIdx = 3;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "PASTP";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorPEAS_Test() throws IOException {
		String text = "I have heard rumours of such things.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 1;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "PEAS";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorPHC_Test() throws IOException {
		String text = "Jack and Jill went up the hill to fetch a pail of water.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 1;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "PHC";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorPIN_Test() throws IOException {
		String text = "Life flows on within you and without you.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 3;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "PIN";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorPIT_Test() throws IOException {
		String text = "It's in the way that you use it.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 0;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "PIT";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorPLACE_Test() throws IOException {
		String text = "Up, up, and away.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 5;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "PLACE";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorPOMD_Test() throws IOException {
		String text = "Winnipeg can get cold in the winter.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 1;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "POMD";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorPRED_Test() throws IOException {
		String text = "The horse is big.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 3;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "PRED";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorPRESP_Test() throws IOException {
		String text = "Stuffing his mouth with cookies, Joe ran out the door.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 0;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "PRESP";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorPRIV_Test() throws IOException {
		String text = "I accept your judgement.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 1;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "PRIV";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorPRMD_Test() throws IOException {
		String text = "Letting I dare not wait upon I would, like the poor cat in the adage?";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		targetTokenIdx = 7;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "PRMD";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorPROD_Test() throws IOException {
		String text = "I do the absolute minimum to get by.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 1;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "PROD";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorPUBV_Test() throws IOException {
		String text = "I pronounce you husband and wife.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 1;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "PUBV";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorRB_Test() throws IOException {
		String text = "I wrote quickly";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 2;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "RB";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorSEMP_Test() throws IOException {
		String text = "I seem to have misplaced my glasses.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 1;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "SEMP";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorSERE_Test() throws IOException {
		String text = "I didn't hear the conclusion, which may have been the point.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 7;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "SERE";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorSPAU_Test() throws IOException {
		String text = "No one believes it until they are objectively shown that it is true.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		targetTokenIdx = 8;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "SPAU";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorSPIN_Test() throws IOException {
		String text = "He wants to convincingly prove that he is right.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 4;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "SPIN";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorSPP2_Test() throws IOException {
		String text = "You don't know what it's like.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 0;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "SPP2";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorSTPR_Test() throws IOException {
		String text = "The candidate that I was thinking of.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 6;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "STPR";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorSUAV_Test() throws IOException {
		String text = "I recommend a different course.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 1;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "SUAV";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorSYNE_Test() throws IOException {
		String text = "He was no true friend.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 4;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "SYNE";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorTHAC_Test() throws IOException {
		String text = "She was so beautiful that it made me cry";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		targetTokenIdx = 4;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "THAC";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorTHVC_Test() throws IOException {
		String text = "And that is how the cookie crumbles.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		targetTokenIdx = 1;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "THVC";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorTIME_Test() throws IOException {
		String text = "And they lived happily ever afterwards. The end.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 5;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "TIME";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorTO_Test() throws IOException {
		String text = "To be or not to be, that is the question.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		targetTokenIdx = 1;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "TO";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorTOBJ_Test() throws IOException {
		String text = "The dog that I saw was yellow.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 2;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "TOBJ";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	
	@Test
	public void BiberAnnotatorTPP3_Test() throws IOException {
		String text = "She was the best damn woman that I'd ever seen.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 0;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "TPP3";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorTSUB_Test() throws IOException {
		String text = "The dog that bit me was rabid.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 2;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "TSUB";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorVBD_Test() throws IOException {
		String text = "Jesus wept.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 1;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "VBD";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorVPRT_Test() throws IOException {
		String text = "Jesus weeps.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 1;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "VPRT";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorWHCL_Test() throws IOException {
		String text = "I believed what he told me.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 4;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "WHCL";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorWHOBJ_Test() throws IOException {
		String text = "The man who Sally likes.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 3;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "WHOBJ";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorWHQU_Test() throws IOException {
		String text = "Who do you love?";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 0;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "WHQU";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorWHSUB_Test() throws IOException {
		String text = "I'm the man who loves you.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 4;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "WHSUB";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorWZPAST_Test() throws IOException {
		String text = "The solution produced by this process is toxic.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 2;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "WZPAST";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorWZPRES_Test() throws IOException {
		String text = "The event causing this decline is unknown at this time.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 2;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "WZPRES";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BiberAnnotatorXX0_Test() throws IOException {
		String text = "I don't know why I didn't come.";
		IOOPAnnotator annotator = new BiberAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		targetTokenIdx = 2;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "XX0";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	//TODO
	//@Test
	//public void BiberDimensionsAnnotator_Test() throws IOException {
	//	assertEquals("Implemented", "TODO");
	//}
	
	@Test
	public void ColorsAnnotator_Test() throws IOException {
		String text = "The quick brown fox jumped over the lazy white dog.";
		IOOPAnnotator annotator = new ColorsAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		
		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);
		
		targetTokenIdx = 2;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "brown";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
		
		targetTokenIdx = 8;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "white";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void CommonWordsAnnotator_Test() throws IOException {
		String text = "These are the times that try men's souls.";
		IOOPAnnotator annotator = new CommonWordsAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		targetTokenIdx = 2;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "the";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void CoreNlpGenderAnnotator_Test() throws IOException {
		String text = "Suzanne takes you down to her place near the river.";
		IOOPAnnotator annotator = new CoreNlpGenderAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		targetTokenIdx = 0;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "FEMALE";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void CoreNlpParagraphAnnotator_Test() throws IOException {
		String text = "Suzanne takes you down to her place near the river.\n\n" + 
				"You can hear the boats go by, you can spend the night forever.";
		IOOPAnnotator annotator = new CoreNlpParagraphAnnotator();
		CoreDocument document = annotate(text, annotator);

		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);
		
		assertEquals(
				new Integer(0),
				(
						(Integer)
						document.sentences().get(0).coreMap().get(
								annotator.getAnnotationClass()
						)
				)
		);
		
		assertEquals(
				new Integer(1),
				(
						(Integer)
						document.sentences().get(1).coreMap().get(
								annotator.getAnnotationClass()
						)
				)
		);
	}
	
	@Test
	public void CoreNlpSentimentAnnotator_Test() throws IOException {
		String text = "I love you Suzanne.";
		IOOPAnnotator annotator = new CoreNlpSentimentAnnotator();
		CoreDocument document = annotate(text, annotator);

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);
		
		assertTrue(
				(
						(
								(BigDecimal)
								document.sentences().get(0).coreMap().get(
										annotator.getAnnotationClass()
								)
						).compareTo(new BigDecimal(.5))
						> 0
				),
				String.format(
						"Sentence annotation incorrect: %s %s", 
						annotator.getAnnotationClass().getName(), 
						document.sentences().get(0).coreMap().get(
								annotator.getAnnotationClass()
						)
				)
		);
	}
	
	@Test
	public void DatesAnnotator_Test() throws IOException {
		String text = "From January 4, 1966, Kawara made a long series of \"Date paintings\" (the Today series), which consist entirely of the date on which the painting was executed in simple white lettering set against a solid background.";
		IOOPAnnotator annotator =  new DatesAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		List<PhraseAnnotation> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		score = (List<PhraseAnnotation>) document.sentences().get(0).coreMap().get(annotator.getAnnotationClass());
		targetSubscoreName = "today";
		boolean foundSubscore = false;
		for (PhraseAnnotation subscore : score) {
			if (subscore.getName().equals(targetSubscoreName)) {
				foundSubscore = true;
				break;
			}
		}
		assertTrue(
				foundSubscore,
				String.format("Sentence annotation subscore missing: %s %s", annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
	}
	
	@Test
	public void FlavorsAnnotator_Test() throws IOException {
		String text = "It won't do to dream of caramel, to think of cinnamon, and long for you.";
		IOOPAnnotator annotator =  new FlavorsAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		score = (Map<String,BigDecimal>) document.sentences().get(0).coreMap().get(annotator.getAnnotationClass());
		targetSubscoreName = "caramel";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Sentence annotation subscore missing: %s %s", annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
		targetSubscoreName = "cinnamon";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Sentence annotation subscore missing: %s %s", annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void FleschKincaidAnnotator_Text() throws IOException {
		String text = "But I had seen first one and then another of the rooms in which I had slept during my life, "
				+ "and in the end I would revisit them all in the long course of my waking dream: rooms in winter, "
				+ "where on going to bed I would at once bury my head in a nest, built up out of the most diverse materials, "
				+ "the corner of my pillow, the top of my blankets, a piece of a shawl, the edge of my bed, "
				+ "and a copy of an evening paper, all of which things I would contrive, "
				+ "with the infinite patience of birds building their nests, to cement into one whole; "
				+ "rooms where, in a keen frost, I would feel the satisfaction of being shut in from the outer world "
				+ "(like the sea-swallow which builds at the end of a dark tunnel and is kept warm by the surrounding earth), "
				+ "and where, the fire keeping in all night, I would sleep wrapped up, as it were, in a great cloak of snug and savoury air, "
				+ "shot with the glow of the logs which would break out again in flame: in a sort of alcove without walls, "
				+ "a cave of warmth dug out of the heart of the room itself, a zone of heat whose boundaries were constantly "
				+ "shifting and altering in temperature as gusts of air ran across them to strike freshly upon my face, "
				+ "from the corners of the room, or from parts near the window or far from the fireplace which had therefore "
				+ "remained cold—or rooms in summer, where I would delight to feel myself a part of the warm evening, "
				+ "where the moonlight striking upon the half-opened shutters would throw down to the foot of my bed its enchanted ladder; "
				+ "where I would fall asleep, as it might be in the open air, like a titmouse which the breeze keeps poised in the "
				+ "focus of a sunbeam—or sometimes the Louis XVI room, so cheerful that I could never feel really unhappy, "
				+ "even on my first night in it: that room where the slender columns which lightly supported its ceiling would part, "
				+ "ever so gracefully, to indicate where the bed was and to keep it separate; sometimes again that little room "
				+ "with the high ceiling, hollowed in the form of a pyramid out of two separate storeys, and partly "
				+ "walled with mahogany, in which from the first moment my mind was drugged by the unfamiliar scent of flowering grasses, "
				+ "convinced of the hostility of the violet curtains and of the insolent indifference of a clock that chattered on at the "
				+ "top of its voice as though I were not there; while a strange and pitiless mirror with square feet, which "
				+ "stood across one corner of the room, cleared for itself a site I had not looked to find tenanted in the quiet "
				+ "surroundings of my normal field of vision: that room in which my mind, forcing itself for hours on end to "
				+ "leave its moorings, to elongate itself upwards so as to take on the exact shape of the room, and to reach to the "
				+ "summit of that monstrous funnel, had passed so many anxious nights while my body lay stretched out in bed, "
				+ "my eyes staring upwards, my ears straining, my nostrils sniffing uneasily, and my heart beating; "
				+ "until custom had changed the colour of the curtains, made the clock keep quiet, brought an expression of "
				+ "pity to the cruel, slanting face of the glass, disguised or even completely dispelled the scent of flowering grasses, "
				+ "and distinctly reduced the apparent loftiness of the ceiling.";
		
		CoreDocument document = new CoreDocument(text);
		CoreNlpUtils.getInstance(getParameterStore()).getPipeline().annotate(document);
		IOOPAnnotator prereq = new SyllableCountAnnotator();
		prereq.init(getParameterStore());
		prereq.annotate(document.annotation());
		prereq.score(document);
		IOOPAnnotator annotator = new FleschKincaidAnnotator();
		annotator.init(getParameterStore());
		annotator.annotate(document.annotation());
		annotator.score(document);
		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		BigDecimal score = (BigDecimal) document.annotation().get(annotator.getAnnotationClass());
		assertTrue(
				new BigDecimal(0).compareTo(score) > 0,
				String.format("Document annotation incorrect: %s", score.toPlainString())
		);
	}
	
	@Test
	public void FunctionWordsAnnotator_Test() throws IOException {
		String text = "These are the times that try men's souls.";
		IOOPAnnotator annotator = new FunctionWordsAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		targetTokenIdx = 2;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "the";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void GenderAnnotator_Test() throws IOException {
		String text = "Anjali is a punk rocker.";
		IOOPAnnotator annotator = new GenderAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		targetTokenIdx = 0;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "FEMALE";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void LocationsAnnotator_Test() throws IOException {
		String text = "She feeds you tea and oranges that come all the way from China.";
		IOOPAnnotator annotator = new LocationsAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		List<PhraseAnnotation> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		targetTokenIdx = 12;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (List<PhraseAnnotation>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());	
		targetSubscoreName = "China";
		PhraseAnnotation foundScore = null;
		for (PhraseAnnotation subscore : score) {
			if (subscore.getName().equals(targetSubscoreName)) {
				foundScore = subscore;
				break;
			}
		}
		assertNotNull(
				foundScore,
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), foundScore.getValue());
	}
	
	@Test
	public void NonAffirmativeAnnotator_Test() throws IOException {
		String text = "I would not eat green eggs and ham.";
		IOOPAnnotator annotator = new NonAffirmativeAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		targetTokenIdx = 2;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "not";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	

	
	@Test
	public void NounGroupsAnnotator_Test() throws IOException {
		String text = "I would not eat green eggs and ham.";
		IOOPAnnotator annotator = new NounGroupsAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		targetTokenIdx = 7;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "food";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void NounHypernymsAnnotator_Test() throws IOException {
		String text = "I would not eat green eggs and ham.";
		CoreDocument document = new CoreDocument(text);
		CoreNlpUtils.getInstance(getParameterStore()).getPipeline().annotate(document);
		IOOPAnnotator prereq = new NounsAnnotator();
		prereq.init(getParameterStore());
		prereq.annotate(document.annotation());
		prereq.score(document);
		IOOPAnnotator annotator = new NounHypernymsAnnotator();
		annotator.init(getParameterStore());
		annotator.annotate(document.annotation());
		annotator.score(document);
		
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);
		
		targetTokenIdx = 7;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "cut_of_pork";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));

	}
	
	@Test
	public void NounsAnnotator_Test() throws IOException {
		String text = "I would not eat green eggs and ham.";
		IOOPAnnotator annotator = new NounsAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		score = (Map<String,BigDecimal>) document.annotation().get(annotator.getAnnotationClass());
		targetSubscoreName = "egg";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Document annotation subscore missing: %s %s", annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
		
		targetSubscoreName = "ham";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %s %s", annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	

	@Test
	public void PeopleAnnotator_Test() throws IOException {
		String text = "Barack Obama was born in Hawaii. He is the president. Obama was elected in 2008.";
		IOOPAnnotator annotator = new PeopleAnnotator();
		CoreDocument document = annotate(text, annotator);
		
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		List<PhraseAnnotation> score = null;
		int targetSentenceIdx = 0;
		
		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(targetSentenceIdx).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		targetSentenceIdx = 0;
		targetTokenIdx = 0;
		assertTrue(
				document.sentences().get(targetSentenceIdx).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (List<PhraseAnnotation>) document.sentences().get(targetSentenceIdx).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "Barack Obama";
		PhraseAnnotation targetSubscore = null;
		for (PhraseAnnotation subscore : score) {
			if (subscore.getName().equals(targetSubscoreName)) {
				targetSubscore = subscore;
			}
		}
		assertNotNull(
				targetSubscore,
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		
		targetSentenceIdx = 1;
		targetTokenIdx = 0;
		assertTrue(
				document.sentences().get(targetSentenceIdx).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (List<PhraseAnnotation>) document.sentences().get(targetSentenceIdx).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "Barack Obama";
		targetSubscore = null;
		for (PhraseAnnotation subscore : score) {
			if (subscore.getName().equals(targetSubscoreName)) {
				targetSubscore = subscore;
			}
		}
		assertNotNull(
				targetSubscore,
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		
		targetSentenceIdx = 2;
		targetTokenIdx = 0;
		assertTrue(
				document.sentences().get(targetSentenceIdx).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (List<PhraseAnnotation>) document.sentences().get(targetSentenceIdx).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "Barack Obama";
		targetSubscore = null;
		for (PhraseAnnotation subscore : score) {
			if (subscore.getName().equals(targetSubscoreName)) {
				targetSubscore = subscore;
			}
		}
		assertNotNull(
				targetSubscore,
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);

	}
	
	//NOT IN OSS VERSION
	//@Test
	//public void PerfecttenseAnnotator_Test() throws IOException {
	//	assertEquals("Implemented", "todo");
	//}
	
	@Test
	public void PointlessAdjectivesAnnotator_Test() throws IOException {
		String text = "I would not eat such green eggs and ham.";
		IOOPAnnotator annotator = new PointlessAdjectivesAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		targetTokenIdx = 4;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "such";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void PointlessAdverbsAnnotator_Test() throws IOException {
		String text = "I would rather not eat green eggs and ham.";
		IOOPAnnotator annotator = new PointlessAdverbsAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		targetTokenIdx = 2;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "rather";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void PossessivesAnnotator_Test() throws IOException {
		String text = "Suzanne takes you down to her place near the river.";
		IOOPAnnotator annotator = new PossessivesAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		targetTokenIdx = 5;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());	
		targetSubscoreName = "her";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void PrepositionCategoriesAnnotator_Test() throws IOException {
		String text = "She feeds you tea and oranges that come all the way from China.";
		IOOPAnnotator annotator = new PrepositionCategoriesAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		targetTokenIdx = 4;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());	
		targetSubscoreName = "coordinating";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
		
		targetTokenIdx = 11;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());	
		targetSubscoreName = "subordinating";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void PrepositionsAnnotator_Test() throws IOException {
		String text = "She feeds you tea and oranges that come all the way from China.";
		IOOPAnnotator annotator = new PrepositionsAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		targetTokenIdx = 4;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());	
		targetSubscoreName = "and";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
		
		targetTokenIdx = 11;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());	
		targetSubscoreName = "from";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void PronounAnnotator_Test() throws IOException {
		String text = "She feeds you tea and oranges that come all the way from China.";
		IOOPAnnotator annotator = new PronounAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		targetTokenIdx = 0;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());	
		targetSubscoreName = "she";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
		
		targetTokenIdx = 2;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());	
		targetSubscoreName = "you";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void PunctuationMarkAnnotator_Test() throws IOException {
		String text = "The panda eats, shoots, and leaves.";
		IOOPAnnotator annotator = new PunctuationMarkAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		targetTokenIdx = 3;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());	
		targetSubscoreName = "Comma";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
		
		targetTokenIdx = 5;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());	
		targetSubscoreName = "Comma";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
		
		targetTokenIdx = 8;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());	
		targetSubscoreName = "Period";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));		
	}
	
	@Test
	public void QuotesAnnotator_Test() throws IOException {
		String text = "She said \"I know what it's like to be dead\".";
		IOOPAnnotator annotator = new QuotesAnnotator();
		CoreDocument document = annotate(text, annotator);
		List<PhraseAnnotation> score = null;
		String targetSubscoreName = "";
		score = (List<PhraseAnnotation>) document.annotation().get(annotator.getAnnotationClass());	
		targetSubscoreName = "I know what";
		assertTrue(
				score.get(0).getName().startsWith(targetSubscoreName),
				String.format("Document annotation subscore missing: %s %s", annotator.getAnnotationClass().getName(), targetSubscoreName)
		);

	}
	
	//NOT IN OSS VERSION
	//@Test
	//public void SettingsAnnotator_Test() throws IOException {
	//	assertEquals("Implemented", "todo");
	//}
	

	@Test
	public void SVOAnnotator_Test() throws IOException {
		//String text = "She gave me a raise.";
		String text = "You should read the story to me.";
		IOOPAnnotator annotator = new SVOAnnotator();
		CoreDocument document = annotate(text, annotator);
		Map<String,BigDecimal> score = null;
		int targetTokenIdx = 0;
		String targetSubscoreName = "";

		score = (Map<String,BigDecimal>) document.annotation().get(annotator.getAnnotationClass());
		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		targetTokenIdx = 0;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());	
		targetSubscoreName = "subject";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
		
		targetTokenIdx = 1;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());	
		targetSubscoreName = "auxVerb";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
		
		targetTokenIdx = 2;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());	
		targetSubscoreName = "verb";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
		
		targetTokenIdx = 4;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());	
		targetSubscoreName = "object";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
		
		targetTokenIdx = 6;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());	
		targetSubscoreName = "indirectObject";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	//NOT IN OSS VERSION
	//@Test
	//public void TemporalNGramsAnnotator_Test() throws IOException {
	//	assertEquals("Implemented", "todo");
	//}
	
	@Test
	public void TopicsAnnotator_Test() throws IOException {
		String text = "She feeds you tea and oranges that come all the way from China.";
		IOOPAnnotator annotator = new TopicsAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		targetTokenIdx = 12;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());	
		targetSubscoreName = "China";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void UncommonWordsAnnotator_Test() throws IOException {
		String text = "It thus differs from a nonce word, which may never be recorded.";
		IOOPAnnotator annotator = new UncommonWordsAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		targetTokenIdx = 5;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());	
		targetSubscoreName = "nonce";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	
	@Test
	public void VaderSentimentAnnotator_Test() throws IOException {
		String text = "I love you Suzanne.";
		IOOPAnnotator annotator = new VaderSentimentAnnotator();
		CoreDocument document = annotate(text, annotator);

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);
		
		assertTrue(
				(
						(
								(BigDecimal)
								document.sentences().get(0).coreMap().get(
										annotator.getAnnotationClass()
								)
						).compareTo(new BigDecimal(.5))
						> 0
				),
				String.format(
						"Sentence annotation incorrect: %s %s", 
						annotator.getAnnotationClass().getName(), 
						document.sentences().get(0).coreMap().get(
								annotator.getAnnotationClass()
						)
				)
		);
	}
	
	@Test
	public void VerbGroupsAnnotator_Test() throws IOException {
		String text = "I'm just sitting here watching the wheels go round and round.";
		IOOPAnnotator annotator = new VerbGroupsAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		targetTokenIdx = 5;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());

		targetSubscoreName = "social";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void VerbHypernymsAnnotator_Test() throws IOException {
		String text = "I'm just sitting here watching the wheels go round and round.";
		CoreDocument document = new CoreDocument(text);
		CoreNlpUtils.getInstance(getParameterStore()).getPipeline().annotate(document);
		IOOPAnnotator prereq = new VerbsAnnotator();
		prereq.init(getParameterStore());
		prereq.annotate(document.annotation());
		prereq.score(document);
		IOOPAnnotator annotator = new VerbHypernymsAnnotator();
		annotator.init(getParameterStore());
		annotator.annotate(document.annotation());
		annotator.score(document);
		
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);
		
		targetTokenIdx = 5;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "check_into";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void VerblessSentencesAnnotator_Test() throws IOException {
		String text = "sadasf ads asdf dsaf.";
		IOOPAnnotator annotator = new VerblessSentencesAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		BigDecimal score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		score = (BigDecimal) document.sentences().get(0).coreMap().get(annotator.getAnnotationClass());
		assertEquals(new BigDecimal(1), score);
	}
	
	@Test
	public void VerbnetGroupsAnnotator_Test() throws IOException {
		String text = "I'm just sitting here watching the wheels go round and round.";
		IOOPAnnotator annotator = new VerbnetGroupsAnnotator();
		CoreDocument document = annotate(text, annotator);
		
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);
		
		targetTokenIdx = 5;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "assessment";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));

	}
	
	@Test
	public void VerbsAnnotator_Test() throws IOException {
		String text = "I would not eat green eggs and ham.";
		IOOPAnnotator annotator = new VerbsAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		score = (Map<String,BigDecimal>) document.annotation().get(annotator.getAnnotationClass());
		targetSubscoreName = "eat";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Document annotation subscore missing: %s %s", annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void VerbTenseAnnotator_Test() throws IOException {
		String text = "I would not eat green eggs and ham.";
		IOOPAnnotator annotator = new VerbTenseAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		score = (Map<String,BigDecimal>) document.annotation().get(annotator.getAnnotationClass());
		targetSubscoreName = "present";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Document annotation subscore missing: %s %s", annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));	
	}
	
	@Test
	public void WikipediaCategoriesAnnotator_Test() throws IOException {
		String text = "Visit Winnipeg in the summer.";
		CoreDocument document = new CoreDocument(text);
		CoreNlpUtils.getInstance(getParameterStore()).getPipeline().annotate(document);
		IOOPAnnotator prereq = new TopicsAnnotator();
		prereq.init(getParameterStore());
		prereq.annotate(document.annotation());
		prereq.score(document);
		IOOPAnnotator annotator = new WikipediaCategoriesAnnotator();
		annotator.init(getParameterStore());
		annotator.annotate(document.annotation());
		annotator.score(document);
		String targetSubscoreName = "";
		List<PhraseAnnotation> score = null;
		score = (List<PhraseAnnotation>) document.annotation().get(annotator.getAnnotationClass());
		targetSubscoreName = "Cities in Manitoba";
		boolean foundSubscore = false;
		for (PhraseAnnotation subscore : score) {
			//System.out.println(subscore.getName());
			if (subscore.getName().startsWith(targetSubscoreName)) {
				foundSubscore = true;
				break;
			}
		}
		assertTrue(
				foundSubscore,
				String.format("Document annotation subscore missing: %s %s", annotator.getAnnotationClass().getName(), targetSubscoreName)
		);		
	}
	
	@Test
	public void WikipediaGlossAnnotator_Test() throws IOException {
		String text = "Visit Winnipeg in the summer.";
		IOOPAnnotator annotator = new WikipediaGlossAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;

		targetTokenIdx = 1;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
	}
	

	@Test
	public void WikipediaPageviewTopicsAnnotator_Test() throws IOException {
		String text = "Visit Winnipeg in the summer.";
		CoreDocument document = new CoreDocument(text);
		CoreNlpUtils.getInstance(getParameterStore()).getPipeline().annotate(document);
		IOOPAnnotator prereq = new TopicsAnnotator();
		prereq.init(getParameterStore());
		prereq.annotate(document.annotation());
		prereq.score(document);
		IOOPAnnotator annotator = new WikipediaPageviewTopicsAnnotator();
		annotator.init(getParameterStore());
		annotator.annotate(document.annotation());
		annotator.score(document);
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;
		score = (Map<String,BigDecimal>) document.annotation().get(annotator.getAnnotationClass());
		targetSubscoreName = "Winnipeg";
		boolean foundSubscore = false;
		for (String subscore : score.keySet()) {
			if (subscore.startsWith(targetSubscoreName)) {
				foundSubscore = true;
				break;
			}
		}
		assertTrue(
				foundSubscore,
				String.format("Document annotation subscore missing: %s %s", annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
	}
	
	@Test
	public void WordlessWordsAnnotator_Test() throws IOException {
		String text = "asdfa sadf asdf sadf.";
		IOOPAnnotator annotator = new WordlessWordsAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		targetTokenIdx = 0;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
	}
	
	@Test
	public void WordnetGlossAnnotator_Test() throws IOException {
		String text = "Visit Winnipeg in the summer.";
		IOOPAnnotator annotator = new WordnetGlossAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;

		targetTokenIdx = 0;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
	}
	
	@Test
	public void WordsAnnotator_Test() throws IOException {
		String text = "These are the times that try men's souls.";
		IOOPAnnotator annotator = new WordsAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		Map<String,BigDecimal> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		targetTokenIdx = 2;
		assertTrue(
				document.sentences().get(0).tokens().get(targetTokenIdx).containsKey(annotator.getAnnotationClass()),
				String.format("Token annotation missing")
		);
		score = (Map<String,BigDecimal>) document.sentences().get(0).tokens().get(targetTokenIdx).get(annotator.getAnnotationClass());
		targetSubscoreName = "the";
		assertTrue(
				score.containsKey(targetSubscoreName),
				String.format("Token annotation subscore missing: %d %s %s", targetTokenIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
		assertEquals(new BigDecimal(1), score.get(targetSubscoreName));
	}
	
	@Test
	public void BecauseAnnotator_Test() throws IOException {
		String text = "Because the world is round it turns me on.";
		IOOPAnnotator annotator =  new BecauseAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		List<PhraseAnnotation> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		score = (List<PhraseAnnotation>) document.sentences().get(0).coreMap().get(annotator.getAnnotationClass());
		targetSubscoreName = "because";
		boolean foundSubscore = false;
		for (PhraseAnnotation subscore : score) {
			if (subscore.getName().startsWith(targetSubscoreName)) {
				foundSubscore = true;
				break;
			}
		}
		assertTrue(
				foundSubscore,
				String.format("Sentence annotation subscore missing: %s %s", annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
	}
	
	@Test
	public void IfAnnotator_Test() throws IOException {
		String text = "If a picture paints a thousand words, then why can't I paint you?";
		IOOPAnnotator annotator =  new IfAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		List<PhraseAnnotation> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		score = (List<PhraseAnnotation>) document.sentences().get(0).coreMap().get(annotator.getAnnotationClass());
		targetSubscoreName = "if";
		boolean foundSubscore = false;
		for (PhraseAnnotation subscore : score) {
			if (subscore.getName().startsWith(targetSubscoreName)) {
				foundSubscore = true;
				break;
			}
		}
		assertTrue(
				foundSubscore,
				String.format("Sentence annotation subscore missing: %s %s", annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
	}
	
	@Test
	public void CharCountAnnotator_Test() throws IOException {
		String text = "Now is the time for all good men to come to the aid of their party.";
		IOOPAnnotator annotator =  new CharCountAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		BigDecimal score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);

		score = (BigDecimal) document.annotation().get(annotator.getAnnotationClass());

		assertEquals(
				new BigDecimal(67),
				score
		);
	}
	
	@Test
	public void ParagraphCountAnnotator_Test() throws IOException {
		String text = "Now is the time for all good men to come to the aid of their party.";
		IOOPAnnotator annotator =  new ParagraphCountAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		BigDecimal score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);

		score = (BigDecimal) document.annotation().get(annotator.getAnnotationClass());

		assertEquals(
				new BigDecimal(1),
				score
		);
	}
	
	@Test
	public void SentenceCountAnnotator_Test() throws IOException {
		String text = "Now is the time for all good men to come to the aid of their party. Donate now.";
		IOOPAnnotator annotator =  new SentenceCountAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		BigDecimal score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);

		score = (BigDecimal) document.annotation().get(annotator.getAnnotationClass());

		assertEquals(
				new BigDecimal(2),
				score
		);
	}
	
	@Test
	public void SyllableCountAnnotator_Test() throws IOException {
		String text = "Now is the time for all good men to come to the aid of their party.";
		IOOPAnnotator annotator =  new SyllableCountAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		BigDecimal score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);

		score = (BigDecimal) document.annotation().get(annotator.getAnnotationClass());

		assertEquals(
				new BigDecimal(18),
				score
		);
	}
	
	@Test
	public void TokenCountAnnotator_Test() throws IOException {
		String text = "Now is the time for all good men to come to the aid of their party.";
		IOOPAnnotator annotator =  new TokenCountAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		BigDecimal score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);

		score = (BigDecimal) document.annotation().get(annotator.getAnnotationClass());

		assertEquals(
				new BigDecimal(17),
				score
		);
	}
	
	@Test
	public void WordCountAnnotator_Test() throws IOException {
		String text = "Now is the time for all good men to come to the aid of their party.";
		IOOPAnnotator annotator =  new WordCountAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		BigDecimal score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);

		score = (BigDecimal) document.annotation().get(annotator.getAnnotationClass());

		assertEquals(
				new BigDecimal(16),
				score
		);
	}
	
	@Test
	public void HowAnnotator_Test() throws IOException {
		String text = "How can I go forward when I don't know which way I'm facing?";
		IOOPAnnotator annotator =  new HowAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		List<PhraseAnnotation> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		score = (List<PhraseAnnotation>) document.sentences().get(0).coreMap().get(annotator.getAnnotationClass());
		targetSubscoreName = "how";
		boolean foundSubscore = false;
		for (PhraseAnnotation subscore : score) {
			if (subscore.getName().startsWith(targetSubscoreName)) {
				foundSubscore = true;
				break;
			}
		}
		assertTrue(
				foundSubscore,
				String.format("Sentence annotation subscore missing: %s %s", annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
	}
	
	@Test
	public void WhatAnnotator_Test() throws IOException {
		String text = "What do you want me to do, to do for you, to see you through?";
		IOOPAnnotator annotator =  new WhatAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		List<PhraseAnnotation> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		score = (List<PhraseAnnotation>) document.sentences().get(0).coreMap().get(annotator.getAnnotationClass());
		targetSubscoreName = "what";
		boolean foundSubscore = false;
		for (PhraseAnnotation subscore : score) {
			if (subscore.getName().startsWith(targetSubscoreName)) {
				foundSubscore = true;
				break;
			}
		}
		assertTrue(
				foundSubscore,
				String.format("Sentence annotation subscore missing: %s %s", annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
	}
	
	@Test
	public void WhenAnnotator_Test() throws IOException {
		String text = "When can I see you again?";
		IOOPAnnotator annotator =  new WhenAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		List<PhraseAnnotation> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		score = (List<PhraseAnnotation>) document.sentences().get(0).coreMap().get(annotator.getAnnotationClass());
		targetSubscoreName = "when";
		boolean foundSubscore = false;
		for (PhraseAnnotation subscore : score) {
			if (subscore.getName().startsWith(targetSubscoreName)) {
				foundSubscore = true;
				break;
			}
		}
		assertTrue(
				foundSubscore,
				String.format("Sentence annotation subscore missing: %s %s", annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
	}
	
	@Test
	public void WhereAnnotator_Test() throws IOException {
		String text = "Where are you going?";
		IOOPAnnotator annotator =  new WhereAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		List<PhraseAnnotation> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		score = (List<PhraseAnnotation>) document.sentences().get(0).coreMap().get(annotator.getAnnotationClass());
		targetSubscoreName = "where";
		boolean foundSubscore = false;
		for (PhraseAnnotation subscore : score) {
			if (subscore.getName().startsWith(targetSubscoreName)) {
				foundSubscore = true;
				break;
			}
		}
		assertTrue(
				foundSubscore,
				String.format("Sentence annotation subscore missing: %s %s", annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
	}
	
	@Test
	public void WhoAnnotator_Test() throws IOException {
		String text = "Who let the dogs out?";
		IOOPAnnotator annotator =  new WhoAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		List<PhraseAnnotation> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		score = (List<PhraseAnnotation>) document.sentences().get(0).coreMap().get(annotator.getAnnotationClass());
		targetSubscoreName = "who";
		boolean foundSubscore = false;
		for (PhraseAnnotation subscore : score) {
			if (subscore.getName().startsWith(targetSubscoreName)) {
				foundSubscore = true;
				break;
			}
		}
		assertTrue(
				foundSubscore,
				String.format("Sentence annotation subscore missing: %s %s", annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
	}
	
	@Test
	public void WhyAnnotator_Test() throws IOException {
		String text = "Why you wanna treat me so bad?";
		IOOPAnnotator annotator =  new WhyAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		List<PhraseAnnotation> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		score = (List<PhraseAnnotation>) document.sentences().get(0).coreMap().get(annotator.getAnnotationClass());
		targetSubscoreName = "why";
		boolean foundSubscore = false;
		for (PhraseAnnotation subscore : score) {
			if (subscore.getName().startsWith(targetSubscoreName)) {
				foundSubscore = true;
				break;
			}
		}
		assertTrue(
				foundSubscore,
				String.format("Sentence annotation subscore missing: %s %s", annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
	}
	
	@Test
	public void AsAnnotator_Test() throws IOException {
		String text = "We are such stuff as dreams are made on, and our little life is rounded with a sleep.";
		IOOPAnnotator annotator =  new AsAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		List<PhraseAnnotation> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		score = (List<PhraseAnnotation>) document.sentences().get(0).coreMap().get(annotator.getAnnotationClass());
		targetSubscoreName = "as";
		boolean foundSubscore = false;
		for (PhraseAnnotation subscore : score) {
			if (subscore.getName().startsWith(targetSubscoreName)) {
				foundSubscore = true;
				break;
			}
		}
		assertTrue(
				foundSubscore,
				String.format("Sentence annotation subscore missing: %s %s", annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
	}
	
	@Test
	public void LikeAnnotator_Test() throws IOException {
		String text = "Happy like a cabbie in a rainstorm.";
		IOOPAnnotator annotator =  new LikeAnnotator();
		CoreDocument document = annotate(text, annotator);
		int targetTokenIdx = 0;
		String targetSubscoreName = "";
		List<PhraseAnnotation> score = null;

		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		assertTrue(
				document.sentences().get(0).coreMap().containsKey(annotator.getAnnotationClass()),
				String.format("Sentence annotation missing")
		);

		score = (List<PhraseAnnotation>) document.sentences().get(0).coreMap().get(annotator.getAnnotationClass());
		targetSubscoreName = "like";
		boolean foundSubscore = false;
		for (PhraseAnnotation subscore : score) {
			if (subscore.getName().startsWith(targetSubscoreName)) {
				foundSubscore = true;
				break;
			}
		}
		assertTrue(
				foundSubscore,
				String.format("Sentence annotation subscore missing: %s %s", annotator.getAnnotationClass().getName(), targetSubscoreName)
		);
	}
	
	public static void main(String[] args) throws IOException {
		Annotator_Test t = new Annotator_Test();
		t.ActionlessVerbsAnnotator_Test();

	}

}
