package io.outofprintmagazine.nlp.pipeline.annotators;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.pipeline.PhraseAnnotation;

public class DatesAnnotator extends AbstractTreeAnnotator implements Annotator, OOPAnnotator {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(DatesAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}

	public DatesAnnotator() {
		super();
	}

	public DatesAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}

	@Override
	public void init(Map<String, Object> properties) {
	}

	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPDatesAnnotation.class;
	}

	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(CoreAnnotations.NamedEntityTagAnnotation.class,
				CoreAnnotations.NormalizedNamedEntityTagAnnotation.class,
				CoreAnnotations.CanonicalEntityMentionIndexAnnotation.class)));
	}

	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			List<CoreEntityMention> entityMentions = sentence.entityMentions();
			for (CoreEntityMention mention : entityMentions) {
				if (mention.entityType().equals("DATE") || mention.entityType().equals("TIME")) {
					List<PhraseAnnotation> scoreMap = new ArrayList<PhraseAnnotation>();
					if (mention.canonicalEntityMention().isPresent()) {
						addToScoreList(scoreMap, new PhraseAnnotation(mention.canonicalEntityMention().get().toString().toLowerCase(), new BigDecimal(1)));
						for (CoreLabel token : mention.tokens()) {
							if (!token.containsKey(getAnnotationClass())) {
								token.set(getAnnotationClass(), scoreMap);
								break;
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
		return "CoreNLP NER mentions: DATE or TIME";
	}
}
