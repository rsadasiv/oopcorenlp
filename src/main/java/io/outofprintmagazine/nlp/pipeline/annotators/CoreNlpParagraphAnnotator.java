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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.util.ParameterStore;

public class CoreNlpParagraphAnnotator extends edu.stanford.nlp.paragraphs.ParagraphAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(CoreNlpParagraphAnnotator.class);
	
	public CoreNlpParagraphAnnotator() {
		super(CoreNlpParagraphAnnotator.getProperties(), false);
	}
	
	protected ParameterStore properties;
	
	@Override
	public void init(ParameterStore properties) {
		this.properties = properties;
	}

	private static Properties getProperties() {
		Properties props = new Properties();
		props.setProperty("paragraphBreak", "two");
		return props;
	}
	
	@Override
	public Class getAnnotationClass() {
		return edu.stanford.nlp.ling.CoreAnnotations.ParagraphIndexAnnotation.class;
	}

	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(
			new ArraySet<>(
				Arrays.asList(
					CoreAnnotations.ParagraphIndexAnnotation.class
				)
			)
		);
	}
	
	@Override
	public void score(CoreDocument document) {
		//pass
	}
	
	@Override
	public void serialize(CoreDocument document, ObjectNode json) {
		Iterator<CoreSentence> coreNlpSentencesIter = document.sentences().iterator();
		Iterator<JsonNode> jsonSentencesIter = ((ArrayNode) json.get("sentences")).iterator();
		while (coreNlpSentencesIter.hasNext() && jsonSentencesIter.hasNext()) {
			CoreSentence sentence = coreNlpSentencesIter.next();
			ObjectNode sentenceNode = (ObjectNode) jsonSentencesIter.next();
			if (sentence.coreMap().containsKey(getAnnotationClass())) {
				sentenceNode.put(CoreAnnotations.ParagraphIndexAnnotation.class.getSimpleName(), sentence.coreMap().get(CoreAnnotations.ParagraphIndexAnnotation.class));
			}
		}
	}

	@Override
	public String getDescription() {
		return "CoreAnnotations.ParagraphIndexAnnotation.class. Recorded per sentence, index starts at 0.";
	}
	
	@Override
	public void serializeAggregateDocument(CoreDocument document, ObjectNode json) {
		//pass
	}
	
}
