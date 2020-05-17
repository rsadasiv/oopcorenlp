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
import io.outofprintmagazine.nlp.pipeline.OOPAnnotations;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public class GenderAnnotator extends edu.stanford.nlp.pipeline.GenderAnnotator implements Annotator, OOPAnnotator {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(GenderAnnotator.class);

	protected Scorer scorer;
	protected Serializer serializer;

	private static Properties getProperties() {
		Properties props = new Properties();
		props.setProperty("gender.maleNamesFile",
				"io/outofprintmagazine/nlp/models/MaleNames.txt");
		props.setProperty("gender.femaleNamesFile",
				"io/outofprintmagazine/nlp/models/FemaleNames.txt");
		return props;
	}

	public GenderAnnotator(String annotatorName, Properties props) {
		this();
	}
	
	public GenderAnnotator() {
		super("GenderAnnotator", GenderAnnotator.getProperties());
		this.setScorer((Scorer) new MapSum(this.getAnnotationClass()));
		this.setSerializer((Serializer) new MapSerializer(this.getAnnotationClass()));
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
		score(new CoreDocument(annotation));
	}

	@Override
	public void annotateEntityMention(CoreMap entityMention, String gender) {
		// annotate the entity mention
		entityMention.set(CoreAnnotations.GenderAnnotation.class, gender);
		// annotate each token of the entity mention
		for (CoreLabel token : entityMention.get(CoreAnnotations.TokensAnnotation.class)) {
			Map<String, BigDecimal> scoreMap = new HashMap<String, BigDecimal>();
			scoreMap.put(gender, new BigDecimal(1));
			token.set(getAnnotationClass(), scoreMap);
		}
	}


	@Override
	public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
		return Collections.singleton(OOPAnnotations.OOPGenderAnnotation.class);
	}
	

	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPGenderAnnotation.class;
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
	public void init(Map<String, Object> properties) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDescription() {
		return "io/outofprintmagazine/nlp/models/MaleNames.txt io/outofprintmagazine/nlp/models/FemaleNames.txt";
	}

	@Override
	public void serializeAggregateDocument(CoreDocument document, ObjectNode json) {
		getSerializer().serializeAggregate(getScorer().aggregateDocument(document), json);
	}
}