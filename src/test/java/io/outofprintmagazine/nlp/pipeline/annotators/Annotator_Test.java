package io.outofprintmagazine.nlp.pipeline.annotators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.JSONOutputter;
import io.outofprintmagazine.nlp.Analyzer;
import io.outofprintmagazine.nlp.pipeline.ActorAnnotation;
import io.outofprintmagazine.nlp.pipeline.OOPAnnotations;
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
import io.outofprintmagazine.nlp.pipeline.serializers.CoreNlpSerializer;
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
			p.put("wikipedia_apikey", "OOPCoreNlp/1.0 httpclient/4.5.6");
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


	private void runPrereqs(IOOPAnnotator annotator, CoreDocument document) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		List<String> prereqs = new ArrayList<String>();

		for (Class clazz : annotator.requires()) {
			String className = clazz.getName();
			if (className.startsWith("io.outofprintmagazine.nlp.pipeline.OOPAnnotations$")) {
				String annotatorClassName =
						"io.outofprintmagazine.nlp.pipeline.annotators." +
						className.substring(
								"io.outofprintmagazine.nlp.pipeline.OOPAnnotations$".length(),
								className.length()-"Annotation".length()
						).replaceAll("OOP", "") +
						"Annotator";
				prereqs.add(annotatorClassName);
			}
		}

		for (String ac : Analyzer.customAnnotatorClassNames) {
			if (prereqs.contains(ac)) {
				Object prereq = Class.forName(ac).getConstructor().newInstance();
				if (prereq instanceof IOOPAnnotator) {
					IOOPAnnotator oopAnnotator = (IOOPAnnotator) prereq;
					oopAnnotator.init(parameterStore);
					oopAnnotator.annotate(document.annotation());
					oopAnnotator.score(document);
				}
			}	
		}
	}
	
	@SuppressWarnings("unused")
	private void serializePrereqs(IOOPAnnotator annotator, CoreDocument document, ObjectNode json) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		List<String> prereqs = new ArrayList<String>();

		for (Class clazz : annotator.requires()) {
			String className = clazz.getName();
			if (className.startsWith("io.outofprintmagazine.nlp.pipeline.OOPAnnotations$")) {
				String annotatorClassName =
						"io.outofprintmagazine.nlp.pipeline.annotators." +
						className.substring(
								"io.outofprintmagazine.nlp.pipeline.OOPAnnotations$".length(),
								className.length()-"Annotation".length()
						).replaceAll("OOP", "") +
						"Annotator";
				prereqs.add(annotatorClassName);
			}
		}

		for (String ac : Analyzer.customAnnotatorClassNames) {
			if (prereqs.contains(ac)) {
				Object prereq = Class.forName(ac).getConstructor().newInstance();
				if (prereq instanceof IOOPAnnotator) {
					IOOPAnnotator oopAnnotator = (IOOPAnnotator) prereq;
					oopAnnotator.init(parameterStore);
					oopAnnotator.serialize(document, json);
				}
			}	
		}
	}

	@Test
	public void ActorsAnnotator_Test() throws Throwable {
		try {
			String text = "WE met next day as he had arranged, and inspected the rooms at No. 221B, 5 Baker Street, of which he had spoken at our meeting. They consisted of a couple of comfortable bed-rooms and a single large airy sitting-room, cheerfully furnished, and illuminated by two broad windows. So desirable in every way were the apartments, and so moderate did the terms seem when divided between us, that the bargain was concluded upon the spot, and we at once entered into possession. That very evening I moved my things round from the hotel, and on the following morning Sherlock Holmes followed me with several boxes and portmanteaus. For a day or two we were busily employed in unpacking and laying out our property to the best advantage. That done, we gradually began to settle down and to accommodate ourselves to our new surroundings.\n" + 
					"\n" + 
					"Holmes was certainly not a difficult man to live with. He was quiet in his ways, and his habits were regular. It was rare for him to be up after ten at night, and he had invariably breakfasted and gone out before I rose in the morning. Sometimes he spent his day at the chemical laboratory, sometimes in the dissecting-rooms, and occasionally in long walks, which appeared to take him into the lowest portions of the City. Nothing could exceed his energy when the working fit was upon him; but now and again a reaction would seize him, and for days on end he would lie upon the sofa in the sitting-room, hardly uttering a word or moving a muscle from morning to night. On these occasions I have noticed such a dreamy, vacant expression in his eyes, that I might have suspected him of being addicted to the use of some narcotic, had not the temperance and cleanliness of his whole life forbidden such a notion.\n" + 
					"\n" + 
					"As the weeks went by, my interest in him and my curiosity as to his aims in life, gradually deepened and increased. His very person and appearance were such as to strike the attention of the most casual observer. In height he was rather over six feet, and so excessively lean that he seemed to be considerably taller. His eyes were sharp and piercing, save during those intervals of torpor to which I have alluded; and his thin, hawk-like nose gave his whole expression an air of alertness and decision. His chin, too, had the prominence and squareness which mark the man of determination. His hands were invariably blotted with ink and stained with chemicals, yet he was possessed of extraordinary delicacy of touch, as I frequently had occasion to observe when I watched him manipulating his fragile philosophical instruments.\n" + 
					"\n" + 
					"The reader may set me down as a hopeless busybody, when I confess how much this man stimulated my curiosity, and how often I endeavoured to break through the reticence which he showed on all that concerned himself. Before pronouncing judgment, however, be it remembered, how objectless was my life, and how little there was to engage my attention. My health forbade me from venturing out unless the weather was exceptionally genial, and I had no friends who would call upon me and break the monotony of my daily existence. Under these circumstances, I eagerly hailed the little mystery which hung around my companion, and spent much of my time in endeavouring to unravel it.\n" + 
					"\n" + 
					"He was not studying medicine. He had himself, in reply to a question, confirmed Stamford’s opinion upon that point. Neither did he appear to have pursued any course of reading which might fit him for a degree in science or any other recognized portal which would give him an entrance into the learned world. Yet his zeal for certain studies was remarkable, and within eccentric limits his knowledge was so extraordinarily ample and minute that his observations have fairly astounded me. Surely no man would work so hard or attain such precise information unless he had some definite end in view. Desultory readers are seldom remarkable for the exactness of their learning. No man burdens his mind with small matters unless he has some very good reason for doing so.\n" + 
					"\n" + 
					"His ignorance was as remarkable as his knowledge. Of contemporary literature, philosophy and politics he appeared to know next to nothing. Upon my quoting Thomas Carlyle, he inquired in the naivest way who he might be and what he had done. My surprise reached a climax, however, when I found incidentally that he was ignorant of the Copernican Theory and of the composition of the Solar System. That any civilized human being in this nineteenth century should not be aware that the earth travelled round the sun appeared to be to me such an extraordinary fact that I could hardly realize it.\n" + 
					"\n" + 
					"\"You appear to be astonished,\" he said, smiling at my expression of surprise. \"Now that I do know it I shall do my best to forget it.\"\n" + 
					"\n" + 
					"\"To forget it!\"\n" + 
					"\n" + 
					"\"You see,\" he explained, \"I consider that a man’s brain originally is like a little empty attic, and you have to stock it with such furniture as you choose. A fool takes in all the lumber of every sort that he comes across, so that the knowledge which might be useful to him gets crowded out, or at best is jumbled up with a lot of other things so that he has a difficulty in laying his hands upon it. Now the skilful workman is very careful indeed as to what he takes into his brain-attic. He will have nothing but the tools which may help him in doing his work, but of these he has a large assortment, and all in the most perfect order. It is a mistake to think that that little room has elastic walls and can distend to any extent. Depend upon it there comes a time when for every addition of knowledge you forget something that you knew before. It is of the highest importance, therefore, not to have useless facts elbowing out the useful ones.\"\n" + 
					"\n" + 
					"\"But the Solar System!\" I protested.\n" + 
					"\n" + 
					"\"What the deuce is it to me?\" Holmes interrupted impatiently; \"you say that we go round the sun. If we went round the moon it would not make a pennyworth of difference to me or to my work.\"";
			CoreDocument document = new CoreDocument(text);
			CoreNlpUtils.getInstance(getParameterStore()).getPipeline().annotate(document);
			IOOPAnnotator annotator = new ActorsAnnotator();
			annotator.init(getParameterStore());
			runPrereqs(annotator, document);
			annotator.annotate(document.annotation());
			annotator.score(document);
			
			assertTrue(
					document.annotation().containsKey(annotator.getAnnotationClass()),
					String.format("Document annotation missing")
			);
			
			int targetActorIdx = 1;
			String targetSubscoreName = "Sherlock Holmes";
			ActorAnnotation actor = (
					(List<ActorAnnotation>)document.annotation().get(
							annotator.getAnnotationClass()
							)
					).get(targetActorIdx);			

			assertEquals(
					targetSubscoreName,
					actor.getCanonicalName(),
					String.format("Actor missing: %d %s %s", targetActorIdx, annotator.getAnnotationClass().getName(), targetSubscoreName)
			);
			
			assertTrue(
					actor.getThumbnails().size() > 0,
					String.format("Actor thumbnails missing: %d %s", targetActorIdx, annotator.getAnnotationClass().getName())
			);
					
			assertTrue(
					actor.getAttribute("OOPVerbsAnnotation").containsKey("interrupt"),
					String.format("Actor attributes missing: %s %s %s", "OOPVerbsAnnotation", "interrupt", annotator.getAnnotationClass().getName())
			);
			
			assertTrue(
					actor.getAttribute("OOPVerbGroupsAnnotation").containsKey("communication"),
					String.format("Actor attributes missing: %s %s %s", "OOPVerbGroupsAnnotation", "communication", annotator.getAnnotationClass().getName())
			);
			
			assertTrue(
					actor.getAttribute("OOPAdverbsAnnotation").containsKey("impatiently"),
					String.format("Actor attributes missing: %s %s %s", "OOPAdverbsAnnotation", "impatiently", annotator.getAnnotationClass().getName())
			);
			
			assertTrue(
					actor.getAttribute("OOPNounsAnnotation").containsKey("way"),
					String.format("Actor attributes missing: %s %s %s", "OOPNounsAnnotation", "way", annotator.getAnnotationClass().getName())
			);
			
			assertTrue(
					actor.getAttribute("OOPNounGroupsAnnotation").containsKey("attribute"),
					String.format("Actor attributes missing: %s %s %s", "OOPNounsGroupsAnnotation", "attribute", annotator.getAnnotationClass().getName())
			);
			
			assertTrue(
					actor.getAttribute("OOPAdjectivesAnnotation").containsKey("quiet"),
					String.format("Actor attributes missing: %s %s %s", "OOPAdjectivesAnnotation", "quiet", annotator.getAnnotationClass().getName())
			);			
			
			
		}
		catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}

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
				Integer.valueOf(0),
				(
						(Integer)
						document.sentences().get(0).coreMap().get(
								annotator.getAnnotationClass()
						)
				)
		);

		assertEquals(
				Integer.valueOf(1),
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
	public void FleschKincaidAnnotator_Test() throws IOException {
		String text = "A tree is nice.";	
		CoreDocument document = new CoreDocument(text);
		CoreNlpUtils.getInstance(getParameterStore()).getPipeline().annotate(document);
		IOOPAnnotator prereq = new SyllableCountAnnotator();
		prereq.init(getParameterStore());
		prereq.annotate(document.annotation());
		prereq.score(document);
		IOOPAnnotator annotator = new FleschKincaidAnnotator();
		annotator.init(getParameterStore());
		try {
		annotator.annotate(document.annotation());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		annotator.score(document);
		assertTrue(
				document.annotation().containsKey(annotator.getAnnotationClass()),
				String.format("Document annotation missing")
		);
		BigDecimal score = (BigDecimal) document.annotation().get(annotator.getAnnotationClass());
		assertTrue(
				new BigDecimal(1).compareTo(score) < 0,
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
		try {
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
		//Unreliable
		//assertTrue(
		//		foundSubscore,
		//		String.format("Document annotation subscore missing: %s %s", annotator.getAnnotationClass().getName(), targetSubscoreName)
		//);
		if (!foundSubscore) {
			System.err.println(String.format("WARNING: Document annotation subscore missing: %s %s", annotator.getAnnotationClass().getName(), targetSubscoreName));
		}
		}
		catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}
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
		try {
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
		catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}
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
