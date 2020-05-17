package io.outofprintmagazine.nlp.pipeline.annotators;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public class VerbsAnnotator extends AbstractPosAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(VerbsAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}
	
	private List<String> posTags = Arrays.asList("VB","VBD","VBG","VBN","VBP","VBZ");
	
	public VerbsAnnotator() {
		super();
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass()));
		this.appendTagsFromFile("io/outofprintmagazine/nlp/models/StativeVerbs.txt");
	}
	
	public VerbsAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbsAnnotation.class;
	}

	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (int i=0;i<sentence.tokens().size();i++) {
				CoreLabel token = sentence.tokens().get(i);
				if (posTags.contains(token.tag())) {
					if (!getTags().contains(token.lemma().toLowerCase())) {
						if (!token.lemma().startsWith("'")) {
							Map<String,BigDecimal> scoreMap = new HashMap<String,BigDecimal>();
							addToScoreMap(scoreMap, token.lemma(), new BigDecimal(1));
							token.set(getAnnotationClass(), scoreMap);
						}
					}
				}
			}
		}
		score(document);
	}

	@Override
	public String getDescription() {
		return "Transitive Verbs. VB,VBD,VBG,VBN,VBP,VBZ not in io/outofprintmagazine/nlp/models/StativeVerbs.txt";
	}
}
