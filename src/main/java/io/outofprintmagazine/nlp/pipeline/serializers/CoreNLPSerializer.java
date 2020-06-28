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
package io.outofprintmagazine.nlp.pipeline.serializers;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.CorefChain.CorefMention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreQuote;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.QuoteAttributionAnnotator;
import io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPThumbnailAnnotation;

/**
 * <p>Creates the base json syntax tree from coreDocument.</p>
 * <ul>
 * 	<li>root (document)
 * 		<ul>
 * 			<li>metadata
 * 				<ul>
 * 					    <li>DocIDAnnotation</li>
 * 					    <li>DocTitleAnnotation</li>
 * 					    <li>DocSourceTypeAnnotation</li>
 * 					    <li>DocTypeAnnotation</li>
 * 					    <li>AuthorAnnotation</li>
 * 					    <li>DocDateAnnotation</li>
 * 						<li>OOPThumbnailAnnotation</li>
 *				</ul>
 *			</li>
 * 			<li>corefs</li>
 * 			<li>quotes</li>
 * 			<li>sentences
 * 				<ul>
 * 					<li>tokens
 * 						<ul>
 * 							<li>tokenIndex</li>
 * 							<li>TokensAnnotation
 * 								<ul>
 * 									<li>word</li>
 * 									<li>originalText</li>
 * 									<li>lemma</li>
 * 									<li>characterOffsetBegin</li>
 * 									<li>characterOffsetEnd</li>
 * 									<li>pos</li>
 * 									<li>ner</li>
 * 									<li>before</li>
 * 									<li>after</li>
 * 								</ul>
 * 							</li>
 * 						</ul>
 * 					</li>
 * 				</ul>
 * 			</li>
 * 		</ul>
 * 	</li>
 * </ul>
 * @author Ram Sadasiv
 *
 */
public class CoreNlpSerializer implements Serializer {

	ObjectMapper mapper = new ObjectMapper();
	
	ObjectMapper getMapper() {
		return mapper;
	}
	
	public CoreNlpSerializer() {
		super();
	}

	protected void serializeMetadata(CoreDocument document, ObjectNode documentNode) {
		ObjectNode metadata = documentNode.putObject("metadata");
		if (document.annotation().containsKey(CoreAnnotations.DocIDAnnotation.class)) {
			metadata.put(CoreAnnotations.DocIDAnnotation.class.getSimpleName(),
					document.annotation().get(CoreAnnotations.DocIDAnnotation.class));
		}
		if (document.annotation().containsKey(CoreAnnotations.DocTitleAnnotation.class)) {
			metadata.put(CoreAnnotations.DocTitleAnnotation.class.getSimpleName(),
					document.annotation().get(CoreAnnotations.DocTitleAnnotation.class));
		}
		if (document.annotation().containsKey(CoreAnnotations.DocSourceTypeAnnotation.class)) {
			metadata.put(CoreAnnotations.DocSourceTypeAnnotation.class.getSimpleName(),
					document.annotation().get(CoreAnnotations.DocSourceTypeAnnotation.class));
		}
		if (document.annotation().containsKey(CoreAnnotations.DocTypeAnnotation.class)) {
			metadata.put(CoreAnnotations.DocTypeAnnotation.class.getSimpleName(),
					document.annotation().get(CoreAnnotations.DocTypeAnnotation.class));
		}
		if (document.annotation().containsKey(CoreAnnotations.AuthorAnnotation.class)) {
			metadata.put(CoreAnnotations.AuthorAnnotation.class.getSimpleName(),
					document.annotation().get(CoreAnnotations.AuthorAnnotation.class));
		}
		if (document.annotation().containsKey(CoreAnnotations.DocDateAnnotation.class)) {
			metadata.put(CoreAnnotations.DocDateAnnotation.class.getSimpleName(),
					document.annotation().get(CoreAnnotations.DocDateAnnotation.class));
		}
		if (document.annotation().containsKey(OOPThumbnailAnnotation.class)) {
			metadata.put(OOPThumbnailAnnotation.class.getSimpleName(),
					document.annotation().get(OOPThumbnailAnnotation.class));
		}
	}
	
	protected void serializeCoref(CoreDocument document, ObjectNode documentNode) {
		ArrayNode corefsNode = documentNode.putArray("corefs");
		Map<Integer, CorefChain> corefChains = document.annotation().get(CorefCoreAnnotations.CorefChainAnnotation.class);
		if (corefChains != null) {
			for (CorefChain chain : corefChains.values()) {
				CorefChain.CorefMention representative = chain.getRepresentativeMention();
				ObjectNode chainNode = getMapper().createObjectNode();
				corefsNode.add(chainNode);
				ArrayNode mentionListNode = chainNode.putArray(Integer.toString(chain.getChainID()));
				for (CorefMention mention : chain.getMentionsInTextualOrder()) {
					ObjectNode mentionNode = mapper.createObjectNode();
					mentionNode.put("id", mention.mentionID);
					mentionNode.put("text", mention.mentionSpan);
					mentionNode.put("type", mention.mentionType.toString());
					mentionNode.put("number", mention.number.toString());
					mentionNode.put("gender", mention.gender.toString());
					mentionNode.put("animacy", mention.animacy.toString());
					mentionNode.put("startIndex", mention.startIndex);
					mentionNode.put("endIndex", mention.endIndex);
					mentionNode.put("headIndex", mention.headIndex);
					mentionNode.put("sentNum", mention.sentNum);
					// mentionWriter.set("position",
					// Arrays.stream(mention.position.elems()).boxed().collect(Collectors.toList()));
					mentionNode.put("isRepresentativeMention", mention == representative);
					mentionListNode.add(mentionNode);
				}
			}
		}		
	}
	
	public void serializeQuotes(CoreDocument document, ObjectNode documentNode) {
		ArrayNode quotesNode = documentNode.putArray("quotes");
		for (CoreQuote quote : document.quotes()) {
			ObjectNode quoteNode = mapper.createObjectNode();
			quoteNode.put("id", quote.coreMap().get(CoreAnnotations.QuotationIndexAnnotation.class));
			quoteNode.put("text", quote.coreMap().get(CoreAnnotations.TextAnnotation.class));
			quoteNode.put("beginIndex", quote.coreMap().get(CoreAnnotations.CharacterOffsetBeginAnnotation.class));
			quoteNode.put("endIndex", quote.coreMap().get(CoreAnnotations.CharacterOffsetEndAnnotation.class));
			quoteNode.put("beginToken", quote.coreMap().get(CoreAnnotations.TokenBeginAnnotation.class));
			quoteNode.put("endToken", quote.coreMap().get(CoreAnnotations.TokenEndAnnotation.class));
			quoteNode.put("beginSentence", quote.coreMap().get(CoreAnnotations.SentenceBeginAnnotation.class));
			quoteNode.put("endSentence", quote.coreMap().get(CoreAnnotations.SentenceEndAnnotation.class));
			quoteNode.put("speaker",
					quote.coreMap().get(QuoteAttributionAnnotator.SpeakerAnnotation.class) != null
							? quote.coreMap().get(QuoteAttributionAnnotator.SpeakerAnnotation.class)
							: "Unknown");
			quoteNode.put("canonicalSpeaker",
					quote.coreMap().get(QuoteAttributionAnnotator.CanonicalMentionAnnotation.class) != null
							? quote.coreMap().get(QuoteAttributionAnnotator.CanonicalMentionAnnotation.class)
							: "Unknown");
			quotesNode.add(quoteNode);
		}
	}
	
	@Override
	public void serialize(CoreDocument document, ObjectNode documentNode) {
		

		serializeMetadata(document, documentNode);
		serializeCoref(document, documentNode);
		serializeQuotes(document, documentNode);


		//crud - paragraphs not going to work easily.
		//Just use Tokens, sentences, document
		/*
		 * ArrayNode paragraphsList = documentNode.putArray("paragraphs"); Integer
		 * paragraphIndex = new Integer(-1); ObjectNode paragraphNode = null; ArrayNode
		 * sentencesList = null; for (CoreSentence sentence : document.sentences()) {
		 * 
		 * if (!sentence.coreMap().get(CoreAnnotations.ParagraphIndexAnnotation.class).
		 * equals(paragraphIndex)) { paragraphIndex =
		 * sentence.coreMap().get(CoreAnnotations.ParagraphIndexAnnotation.class);
		 * paragraphNode = mapper.createObjectNode();
		 * paragraphNode.put("paragraphIndex", paragraphIndex);
		 * paragraphsList.add(paragraphNode); sentencesList =
		 * paragraphNode.putArray("sentences"); }
		 */
		ArrayNode sentencesList = documentNode.putArray("sentences");
		for (CoreSentence sentence : document.sentences()) {
			ObjectNode sentenceNode = mapper.createObjectNode();
			sentencesList.add(sentenceNode);
			sentenceNode.put(CoreAnnotations.SentenceIndexAnnotation.class.getSimpleName(),
					sentence.coreMap().get(CoreAnnotations.SentenceIndexAnnotation.class));
			sentenceNode.put("text", sentence.text());
			ArrayNode tokensList = sentenceNode.putArray("tokens");
			for (CoreLabel token : sentence.tokens()) {
				ObjectNode tokenNode = mapper.createObjectNode();
				tokensList.add(tokenNode);
				tokenNode.put("tokenIndex", token.index());
				ObjectNode coreNlpTokenAnnotationsNode = mapper.createObjectNode();
				tokenNode.set(CoreAnnotations.TokensAnnotation.class.getSimpleName(), coreNlpTokenAnnotationsNode);
				coreNlpTokenAnnotationsNode.put("word", token.word());
				coreNlpTokenAnnotationsNode.put("originalText", token.originalText());
				coreNlpTokenAnnotationsNode.put("lemma", token.lemma());
				coreNlpTokenAnnotationsNode.put("characterOffsetBegin", token.beginPosition());
				coreNlpTokenAnnotationsNode.put("characterOffsetEnd", token.endPosition());
				coreNlpTokenAnnotationsNode.put("pos", token.tag());
				coreNlpTokenAnnotationsNode.put("ner", token.ner());
				coreNlpTokenAnnotationsNode.put("before", token.get(CoreAnnotations.BeforeAnnotation.class));
				coreNlpTokenAnnotationsNode.put("after", token.get(CoreAnnotations.AfterAnnotation.class));
			}
		}
	}


	public static void main(String[] argv) {
		System.out.println(CoreAnnotations.DocIDAnnotation.class.getSimpleName());
	}

	//aggregate must be the document itself?
	@Override
	public void serializeAggregate(Object aggregate, ObjectNode json) {
		serializeMetadata((CoreDocument)aggregate, json);
	}

}
