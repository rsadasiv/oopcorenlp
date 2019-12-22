package io.outofprintmagazine.nlp.pipeline.serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;

public class CoreNLPSerializer implements Serializer {

	public CoreNLPSerializer() {
		super();
	}

	@Override
	public void serialize(CoreDocument document, ObjectNode documentNode) {
		ObjectMapper mapper = new ObjectMapper();
		if (document.annotation().containsKey(CoreAnnotations.DocIDAnnotation.class)) {
			documentNode.put(CoreAnnotations.DocIDAnnotation.class.getName(), document.annotation().get(CoreAnnotations.DocIDAnnotation.class));
		}
		if (document.annotation().containsKey(CoreAnnotations.DocTitleAnnotation.class)) {
			documentNode.put(CoreAnnotations.DocTitleAnnotation.class.getName(), document.annotation().get(CoreAnnotations.DocTitleAnnotation.class));
		}
		if (document.annotation().containsKey(CoreAnnotations.DocSourceTypeAnnotation.class)) {
			documentNode.put(CoreAnnotations.DocSourceTypeAnnotation.class.getName(), document.annotation().get(CoreAnnotations.DocSourceTypeAnnotation.class));
		}
		if (document.annotation().containsKey(CoreAnnotations.DocTypeAnnotation.class)) {
			documentNode.put(CoreAnnotations.DocTypeAnnotation.class.getName(), document.annotation().get(CoreAnnotations.DocTypeAnnotation.class));
		}
		if (document.annotation().containsKey(CoreAnnotations.AuthorAnnotation.class)) {
			documentNode.put(CoreAnnotations.AuthorAnnotation.class.getName(), document.annotation().get(CoreAnnotations.AuthorAnnotation.class));
		}
		if (document.annotation().containsKey(CoreAnnotations.DocDateAnnotation.class)) {
			documentNode.put(CoreAnnotations.DocDateAnnotation.class.getName(), document.annotation().get(CoreAnnotations.DocDateAnnotation.class));
		}
		
//crud - paragraphs not going to work easily.
//Just use Tokens, sentences, document
/*		ArrayNode paragraphsList = documentNode.putArray("paragraphs");
		Integer paragraphIndex = new Integer(-1);
		ObjectNode paragraphNode = null;
		ArrayNode sentencesList = null;
		for (CoreSentence sentence : document.sentences()) {

			if (!sentence.coreMap().get(CoreAnnotations.ParagraphIndexAnnotation.class).equals(paragraphIndex)) {
				paragraphIndex = sentence.coreMap().get(CoreAnnotations.ParagraphIndexAnnotation.class);
				paragraphNode = mapper.createObjectNode();
				paragraphNode.put("paragraphIndex", paragraphIndex);
				paragraphsList.add(paragraphNode);
				sentencesList = paragraphNode.putArray("sentences");
			}
*/
		ArrayNode sentencesList = documentNode.putArray("sentences");
		for (CoreSentence sentence : document.sentences()) {
			ObjectNode sentenceNode = mapper.createObjectNode();
			sentencesList.add(sentenceNode);
			sentenceNode.put(CoreAnnotations.SentenceIndexAnnotation.class.getName(), sentence.coreMap().get(CoreAnnotations.SentenceIndexAnnotation.class));
			sentenceNode.put("text", sentence.text());
			ArrayNode tokensList = sentenceNode.putArray("tokens");
			for (CoreLabel token : sentence.tokens()) {
				ObjectNode tokenNode = mapper.createObjectNode();
				tokensList.add(tokenNode);
				tokenNode.put("tokenIndex", token.index());
				ObjectNode coreNlpTokenAnnotationsNode = mapper.createObjectNode();
				tokenNode.set(CoreAnnotations.TokensAnnotation.class.getName(), coreNlpTokenAnnotationsNode);
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

}
