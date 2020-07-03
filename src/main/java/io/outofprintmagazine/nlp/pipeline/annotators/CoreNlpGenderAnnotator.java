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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.util.CoreMap;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;
import io.outofprintmagazine.util.ParameterStore;

public class CoreNlpGenderAnnotator extends edu.stanford.nlp.pipeline.GenderAnnotator implements Annotator, OOPAnnotator {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(CoreNlpGenderAnnotator.class);
	
	protected Scorer scorer;
	protected Serializer serializer;
	protected ParameterStore properties;
	
	@Override
	public void init(ParameterStore properties) {
		this.properties = properties;
	}
		
	private static Properties getDefaultProperties() {
		Properties props = new Properties();
		props.setProperty("gender.maleNamesFile", "edu/stanford/nlp/models/gender/male_first_names.txt");
		props.setProperty("gender.femaleNamesFile", "edu/stanford/nlp/models/gender/female_first_names.txt");
		return props;
	}
	
	public CoreNlpGenderAnnotator() {
		super("gender", CoreNlpGenderAnnotator.getDefaultProperties());
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass()));		
	}
	
	public CoreNlpGenderAnnotator(String annotatorName, Properties props) {
		super(annotatorName, props);
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass()));	
	}
	
	protected Serializer getSerializer() {
		return serializer;
	}

	protected void setSerializer(Serializer serializer) {
		this.serializer = serializer;
	}
	
	protected Scorer getScorer() {
		return scorer;
	}

	protected void setScorer(Scorer scorer) {
		this.scorer = scorer;
	}

	@Override
	public void annotate(Annotation annotation) {
		super.annotate(annotation);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void annotateEntityMention(CoreMap entityMention, String gender) {
		entityMention.set(CoreAnnotations.GenderAnnotation.class, gender);
		// annotate each token of the entity mention
		for (CoreLabel token : entityMention.get(CoreAnnotations.TokensAnnotation.class)) {
			Map<String, BigDecimal> scoreMap = new HashMap<String, BigDecimal>();
			scoreMap.put(gender, new BigDecimal(1));
			token.set(getAnnotationClass(), scoreMap);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
		return Collections.singleton(getAnnotationClass());
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.CoreNlpGenderAnnotation.class;
	}
	
	@Override
	public void score(CoreDocument document) {
		getScorer().score(document);
	}
	
	@Override
	public void serialize(CoreDocument document, ObjectNode json) {
		getSerializer().serialize(document, json);
	}

	@Override
	public String getDescription() {
		return "edu/stanford/nlp/models/gender/male_first_names.txt edu/stanford/nlp/models/gender/female_first_names.txt";
	}

	@Override
	public void serializeAggregateDocument(CoreDocument document, ObjectNode json) {
		getSerializer().serializeAggregate(getScorer().aggregateDocument(document), json);
	}
}
