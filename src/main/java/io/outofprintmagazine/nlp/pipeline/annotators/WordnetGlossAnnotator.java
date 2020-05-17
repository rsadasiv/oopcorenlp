package io.outofprintmagazine.nlp.pipeline.annotators;

import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import io.outofprintmagazine.nlp.WordnetUtils;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.scorers.StringScorer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;
import io.outofprintmagazine.nlp.pipeline.serializers.StringSerializer;

public class WordnetGlossAnnotator extends AbstractPosAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(WordnetGlossAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}
	
	public WordnetGlossAnnotator() {
		super();
		this.appendTagsFromFile("io/outofprintmagazine/nlp/models/COCA/Dolch.txt");
		this.setScorer((Scorer) new StringScorer(this.getAnnotationClass()));
		this.setSerializer((Serializer) new StringSerializer(this.getAnnotationClass()));
	}
	
	public WordnetGlossAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPWordnetGlossAnnotation.class;
	}
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (isDictionaryWord(token)) {
					if (!getTags().contains(token.lemma().toLowerCase())) {
						try {
							String gloss = WordnetUtils.getInstance().getTokenGloss(token, getContextWords(document, token));
							if (gloss == null) {
								gloss = "iWordless word";
							}
							//scoreMap.put(toAlphaNumeric(token.lemma()), Arrays.asList(gloss));
							token.set(getAnnotationClass(), gloss);
						}
						catch (Exception e) {
							logger.error(e);
						}
					}
				}
			}
		}
		score(document);
	}
	
	@Override
	public String getDescription() {
		return "Wordnet gloss if not in io/outofprintmagazine/nlp/models/COCA/en_20k.txt";
	}
}
