package io.outofprintmagazine.nlp.pipeline.annotators;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.mit.jwi.item.ISenseKey;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import io.outofprintmagazine.nlp.WordnetUtils;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public class VerbnetGroupsAnnotator extends AbstractAggregatePosAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(VerbnetGroupsAnnotator.class);
	private List<String> posTags = Arrays.asList("VB","VBD","VBG","VBN","VBP","VBZ");
	
	public VerbnetGroupsAnnotator() {
		super();
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass(), this.getAggregateClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass(), this.getAggregateClass()));
		this.appendTagsFromFile("io/outofprintmagazine/nlp/models/StativeVerbs.txt");
	}
	
	public VerbnetGroupsAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbnetGroupsAnnotation.class;
	}
	
	@Override
	public Class getAggregateClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbnetGroupsAnnotationAggregate.class;
	}

	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (int i=0;i<document.sentences().size();i++) {
			CoreSentence sentence = document.sentences().get(i);			
			for (CoreLabel token : sentence.tokens()) {
				if (posTags.contains(token.tag())) {
					if (!getTags().contains(token.lemma().toLowerCase())) {
						if (!token.lemma().startsWith("'")) {
							try {
								ISenseKey senseKey = WordnetUtils.getInstance().getTargetWordSenseKey(token, getContextWords(document, token));
								if (senseKey != null) {
									//logger.debug("getting verbnet senses: " + senseKey);
									List<String> senses = WordnetUtils.getInstance().getVerbnetSenses(senseKey);
									if (senses != null) {
										//logger.debug("senses length: " + senses.size());
										Map<String,BigDecimal> scoreMap = new HashMap<String,BigDecimal>();
										for (String sense : senses) {
											scoreMap.put(sense.split("-")[0], new BigDecimal(1));
										}
										token.set(getAnnotationClass(), scoreMap);
									}
								}
							} 
							catch (IOException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
								logger.error(e);
							}
						}
					}
				}
			}
		}
		score(document);
	}

	@Override
	public String getDescription() {
		return "Verbnet senses. https://uvi.colorado.edu/";
	}
}
