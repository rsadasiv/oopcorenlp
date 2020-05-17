package io.outofprintmagazine.nlp.pipeline.annotators;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.pipeline.scorers.MapSum;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.serializers.MapSerializer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;

public class BiberDimensionsAnnotator extends AbstractPosAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(BiberDimensionsAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}

	/*
	 * Involved vs Informational production
	 */
	public List<String> dimensionOnePositiveNames = Arrays.asList(
		"PRIV",
		"THATD",
		"CONT",
		"VPRT",
		"SPP2",
		"PROD",
		"XX0",
		"DEMP",
		"EMPH",
		"FPP1",
		"PIT",
		"BEMA",
		"CAUS",
		"DPAR",
		"INPR",
		"HDG",
		"AMP",
		"SERE",
		"WHQU",
		"POMD",
		"PHC",
		"WHCL",
		"RB"
	);
	
	public List<String> dimensionOneNegativeNames = Arrays.asList(
		"NN",
		"AWL",
		"TTR",
		"PIN",
		"JJ"
	);

	/*
	 * Narrative vs Non-Narrative concerns
	 */
	public List<String> dimensionTwoPositiveNames = Arrays.asList(
		"VBD",
		"TPP3",
		"PEAS",
		"PUBV",
		"SYNE",
		"PRESP"
	);

	/*
	 * Explicit vs Situational dependent reference
	 */
	public List<String> dimensionThreePositiveNames = Arrays.asList(
		"WHOBJ",
		"PIRE",
		"WHSUB",
		"PHC",
		"NOMZ"
	);
	

	public List<String> dimensionThreeNegativeNames = Arrays.asList(
		"TIME",
		"PLACE",
		"RB"
	);
	
	/*
	 * Overt expression of persuasion
	 */
	public List<String> dimensionFourPositiveNames = Arrays.asList(
		"TO",
		"PRMD",
		"SUAV",
		"COND",
		"NEMD",
		"SPAU"
	);

	/*
	 * Abstract vs Non-abstract information
	 */
	public List<String> dimensionFivePositiveNames = Arrays.asList(
		"CONJ",
		"PASS",
		"PASTP",
		"BYPA",
		"WZPAST",
		"WZPRES",
		"CAUS",
		"CONC",
		"COND",
		"OSUB"
	);

	/*
	 * On-line informational elaboration
	 */
	public List<String> dimensionSixPositiveNames = Arrays.asList(
		"THVC",
		"DEMO",
		"THAC",
		"TOBJ",
		"TSUB"
	);	
	
	public BiberDimensionsAnnotator() {
		super();
		this.setScorer((Scorer)new MapSum(this.getAnnotationClass()));
		this.setSerializer((Serializer)new MapSerializer(this.getAnnotationClass()));		
	}

	public BiberDimensionsAnnotator(Properties properties) {
		this();
		this.properties = properties;
	}

	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPBiberDimensionsAnnotation.class;
	}

	
	@Override
	public void init(Map<String, Object> properties) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public String getDescription() {
		return "Biber: Variation across speech and writing, Dimensions 1-6. https://sites.google.com/site/multidimensionaltagger/MAT%20v%201.2%20-%20Manual.pdf.";
	}

	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(
			new ArraySet<>(
				Arrays.asList(
						io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPBiberAnnotation.class
					)
				)
			);
	}

	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				if (token.containsKey(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPBiberAnnotation.class)) {
					Map<String,BigDecimal> scoreMap = token.get(io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPBiberAnnotation.class);
					Map<String,BigDecimal> dimensionMap = new HashMap<String,BigDecimal>();
					for (String key : scoreMap.keySet()) {
						if (dimensionOnePositiveNames.contains(key)) {
							BigDecimal existingScore = dimensionMap.get("DimensionOnePositive");
							if (existingScore == null) {
								existingScore = new BigDecimal(0);
							}
							dimensionMap.put("DimensionOnePositive", existingScore.add(scoreMap.get(key)));
						}
						if (dimensionOneNegativeNames.contains(key)) {
							BigDecimal existingScore = dimensionMap.get("DimensionOneNegative");
							if (existingScore == null) {
								existingScore = new BigDecimal(0);
							}
							dimensionMap.put("DimensionOneNegative", existingScore.add(scoreMap.get(key)));
						}
						if (dimensionTwoPositiveNames.contains(key)) {
							BigDecimal existingScore = dimensionMap.get("DimensionTwoPositive");
							if (existingScore == null) {
								existingScore = new BigDecimal(0);
							}
							dimensionMap.put("DimensionTwoPositive", existingScore.add(scoreMap.get(key)));
						}
						/*
						if (dimensionTwoNegativeNames.contains(key)) {
							BigDecimal existingScore = dimensionMap.get("DimensionTwo");
							if (existingScore == null) {
								existingScore = new BigDecimal(0);
							}
							dimensionMap.put("DimensionTwo", existingScore.subtract(scoreMap.get(key)));
						}
						*/
						if (dimensionThreePositiveNames.contains(key)) {
							BigDecimal existingScore = dimensionMap.get("DimensionThreePositive");
							if (existingScore == null) {
								existingScore = new BigDecimal(0);
							}
							dimensionMap.put("DimensionThreePositive", existingScore.add(scoreMap.get(key)));
						}
						if (dimensionThreeNegativeNames.contains(key)) {
							BigDecimal existingScore = dimensionMap.get("DimensionThreeNegative");
							if (existingScore == null) {
								existingScore = new BigDecimal(0);
							}
							dimensionMap.put("DimensionThreeNegative", existingScore.add(scoreMap.get(key)));
						}
						if (dimensionFourPositiveNames.contains(key)) {
							BigDecimal existingScore = dimensionMap.get("DimensionFourPositive");
							if (existingScore == null) {
								existingScore = new BigDecimal(0);
							}
							dimensionMap.put("DimensionFourPositive", existingScore.add(scoreMap.get(key)));
						}
						if (dimensionFivePositiveNames.contains(key)) {
							BigDecimal existingScore = dimensionMap.get("DimensionFivePositive");
							if (existingScore == null) {
								existingScore = new BigDecimal(0);
							}
							dimensionMap.put("DimensionFivePositive", existingScore.add(scoreMap.get(key)));
						}
						if (dimensionSixPositiveNames.contains(key)) {
							BigDecimal existingScore = dimensionMap.get("DimensionSixPositive");
							if (existingScore == null) {
								existingScore = new BigDecimal(0);
							}
							dimensionMap.put("DimensionSixPositive", existingScore.add(scoreMap.get(key)));
						}

					}
					token.set(getAnnotationClass(), dimensionMap);
				}
			}
		}
		score(document);
	}
	
}
