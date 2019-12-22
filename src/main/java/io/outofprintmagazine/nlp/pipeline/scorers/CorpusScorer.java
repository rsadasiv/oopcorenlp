package io.outofprintmagazine.nlp.pipeline.scorers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.outofprintmagazine.util.CorpusDocumentAggregateScore;
import io.outofprintmagazine.util.CorpusScalarAggregateScore;
import io.outofprintmagazine.util.CorpusScore;
import io.outofprintmagazine.util.DocumentAggregateScore;

public class CorpusScorer {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(CorpusScorer.class);
	
	private Map<String, Map<String, DocumentAggregateScore>> annotationDocumentAnnotationScores = new HashMap<String, Map<String, DocumentAggregateScore>>();
	private Map<String, Map<String, BigDecimal>> annotationDocumentScalarAnnotationScores = new HashMap<String, Map<String, BigDecimal>>();
	//io.outofprintmagazine.nlp.pipeline.OOPAnnotations$OOPDocumentLengthAnnotation
	private List<String> scalarAnnotations = Arrays.asList(
			"io.outofprintmagazine.nlp.pipeline.OOPAnnotations$OOPFleschKincaidAnnotation",
			"io.outofprintmagazine.nlp.pipeline.OOPAnnotations$VaderSentimentAnnotation",
			"io.outofprintmagazine.nlp.pipeline.OOPAnnotations$CoreNlpSentimentAnnotation"
	);
	
	public CorpusScorer() {
		super();
	}
	
	public CorpusScorer(String name) {
		this();
		setName(name);
	}
	
	private String name = "Submissions";
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addDocumentJson(ObjectNode documentNode) {
		ObjectMapper mapper = new ObjectMapper();
		Iterator<String> annotationScoresIter = documentNode.fieldNames();
		while (annotationScoresIter.hasNext()) {
			String annotationName = annotationScoresIter.next();
			if (scalarAnnotations.contains(annotationName)) {
				BigDecimal annotationScore = documentNode.get(annotationName).decimalValue();
				Map<String, BigDecimal> annotationDocumentAnnotationScore = annotationDocumentScalarAnnotationScores.get(annotationName);
				if (annotationDocumentAnnotationScore == null) {
					annotationDocumentAnnotationScore = new HashMap<String, BigDecimal>();
				}
				annotationDocumentAnnotationScore.put(
						documentNode.get("edu.stanford.nlp.ling.CoreAnnotations$DocIDAnnotation").asText(),
						annotationScore
				);
				annotationDocumentScalarAnnotationScores.put(annotationName, annotationDocumentAnnotationScore);
			}
			else if (annotationName.endsWith("Aggregate")) {
				//assume MapAggregateScore - need to deal with Scalar separately?
				//OOPLocationsAggregate
				DocumentAggregateScore annotationScore = mapper.convertValue(documentNode.get(annotationName), DocumentAggregateScore.class);

				Map<String, DocumentAggregateScore> annotationDocumentAnnotationScore = annotationDocumentAnnotationScores.get(annotationName);
				if (annotationDocumentAnnotationScore == null) {
					annotationDocumentAnnotationScore = new HashMap<String, DocumentAggregateScore>();
				}
				annotationDocumentAnnotationScore.put(
						documentNode.get("edu.stanford.nlp.ling.CoreAnnotations$DocIDAnnotation").asText(),
						annotationScore
				);
				annotationDocumentAnnotationScores.put(annotationName, annotationDocumentAnnotationScore);
			}
		}
	}
	
	public List<CorpusScalarAggregateScore> scoreScalarCorpus() {
		//CorpusScore corpusScore = new CorpusScore();
		List<CorpusScalarAggregateScore> retval = new ArrayList<CorpusScalarAggregateScore>();
		Iterator<String> annotationDocumentScalarAnnotationScoresKeyIter = annotationDocumentScalarAnnotationScores.keySet().iterator();
		while (annotationDocumentScalarAnnotationScoresKeyIter.hasNext()) {
			String annotationName = annotationDocumentScalarAnnotationScoresKeyIter.next();
			Map<String, BigDecimal> rawScores = annotationDocumentScalarAnnotationScores.get(annotationName);
			retval.add(new CorpusScalarAggregateScore(annotationName, rawScores, new BigDecimal(1)));
		}
		return retval;
		
	}
	public List<CorpusDocumentAggregateScore> scoreCorpus() {
		//CorpusScore corpusScore = new CorpusScore();
		List<CorpusDocumentAggregateScore> retval = new ArrayList<CorpusDocumentAggregateScore>();
		Iterator<String> annotationDocumentAnnotationScoresKeyIter = annotationDocumentAnnotationScores.keySet().iterator();
		while (annotationDocumentAnnotationScoresKeyIter.hasNext()) {
			String annotationName = annotationDocumentAnnotationScoresKeyIter.next();
			Map<String, DocumentAggregateScore> rawScores = annotationDocumentAnnotationScores.get(annotationName);
			CorpusDocumentAggregateScore totalRawScores = new CorpusDocumentAggregateScore(annotationName, rawScores);
			//corpusScore.getCorpusAggregateScore().put(annotationName, totalRawScores);
			retval.add(totalRawScores);
		}
		return retval;
		
	}
	


/*	public void scoreSingleDocument(CorpusScore corpusScore, ObjectNode documentNode) {
		ObjectMapper mapper = new ObjectMapper();
		//for each Aggregate at the document level
		Iterator<String> annotationScoresIter = documentNode.fieldNames();
		while (annotationScoresIter.hasNext()) {
			String annotationName = annotationScoresIter.next();
			if (annotationName.endsWith("Aggregate")) {
				//assume MapAggregateScore - need to deal with Scalar separately?
				//OOPLocationsAggregate
				DocumentAggregateScore annotationScore = mapper.convertValue(documentNode.get(annotationName), DocumentAggregateScore.class);
				
				//Have we ever moved this annotation type to the corpusScore?
				CorpusDocumentAggregateScore corpusAggregateScore = findCorpusAggregateScoreByAnnotationName(corpusScore.getAggregateScores(), annotationName);
				//if not, create one
				if (corpusAggregateScore == null) {
					corpusAggregateScore = new CorpusDocumentAggregateScore(corpusScore.getCorpusName(), annotationName);
					corpusScore.getAggregateScores().add(corpusAggregateScore);
				}
				//move this document's annotationScore to the corpus score
				corpusAggregateScore.getAnnotationScores().add(
						new DocumentMapAggregateScore(
								corpusScore.getCorpusName(), 
								annotationName, 
								documentNode.get("edu.stanford.nlp.ling.CoreAnnotations$DocIDAnnotation").asText(),
								annotationScore)
						);

				for (AggregateScore subAnnotationScore : annotationScore.getAggregateScores()) {
					//Kashmir
					
					//Have we ever moved this subAnnotation value to the corpusScore?
					CorpusDocumentAggregateScore corpusDocumentAggregateScore = findCorpusAggregateScoreBySubAnnotationName(
							corpusAggregateScore.getSubAnnotationScores(), 
							subAnnotationScore.getName()
					);
					//if not, create one
					if (corpusDocumentAggregateScore == null) {
						corpusDocumentAggregateScore = new CorpusDocumentAggregateScore(corpusScore.getCorpusName(), annotationName, subAnnotationScore.getName());
						corpusAggregateScore.getSubAnnotationScores().add(corpusDocumentAggregateScore);
					}
					
					//move this documents subAnnotationScore to the corpus score
					DocumentAggregateScore documentAggregateScore = new DocumentAggregateScore(
							corpusScore.getCorpusName(), 
							annotationName, 
							subAnnotationScore.getName(),
							documentNode.get("edu.stanford.nlp.ling.CoreAnnotations$DocIDAnnotation").asText(),
							subAnnotationScore
					);
					corpusDocumentAggregateScore.getAggregateScores().add(documentAggregateScore);
				}
			}
		}	
	}
	
	public void scoreCorpusAnnotators(CorpusScore corpusScore) {
		StandardDeviation sd = new StandardDeviation();
		for (CorpusDocumentAggregateScore corpusAggregateScore : corpusScore.getAggregateScores()) {
			//OOPLocations

			double[] primitiveNormalizedScores = new double[corpusAggregateScore.getAnnotationScores().size()];
			Iterator<DocumentMapAggregateScore> documentMapAggregateIter = corpusAggregateScore.getAnnotationScores().iterator();
			for (int i=0;i<corpusAggregateScore.getAnnotationScores().size() && documentMapAggregateIter.hasNext();i++) {
				DocumentMapAggregateScore documentMapAggregateScore = documentMapAggregateIter.next();
				//calculate the totals/stats across all documents
				//aka MapScorer.scoreDocumentAggregate
				corpusAggregateScore.setTotalRaw(corpusAggregateScore.getTotalRaw().add(documentMapAggregateScore.getRaw()));
				corpusAggregateScore.setTotalNormalized(corpusAggregateScore.getTotalNormalized().add(documentMapAggregateScore.getNormalized()));
				corpusAggregateScore.setCount(corpusAggregateScore.getCount().add(new BigDecimal(1)));
				primitiveNormalizedScores[i] = documentMapAggregateScore.getNormalized().doubleValue();
			}
			corpusAggregateScore.setStddev(new BigDecimal(sd.evaluate(primitiveNormalizedScores)));
			sd.clear();
			corpusAggregateScore.setMean(corpusAggregateScore.getTotalNormalized().divide(corpusAggregateScore.getCount()));

			Collections.sort(corpusAggregateScore.getAnnotationScores());
			documentMapAggregateIter = corpusAggregateScore.getAnnotationScores().iterator();
			for (int i=0;i<corpusAggregateScore.getAnnotationScores().size() && documentMapAggregateIter.hasNext();i++) {
				DocumentMapAggregateScore documentMapAggregateScore = documentMapAggregateIter.next();
				//roll up the stats to the DocumentMapAggregateScore
				//aka CorpusScorer.scoreCorpusSubAnnotators
				documentMapAggregateScore.setPercentage(				
						documentMapAggregateScore.getNormalized().divide(
								corpusAggregateScore.getTotalNormalized(), 10, RoundingMode.HALF_DOWN
						)
					);
				documentMapAggregateScore.setZ(documentMapAggregateScore.getNormalized().subtract(corpusAggregateScore.getMean()).divide(corpusAggregateScore.getStddev()));
				documentMapAggregateScore.setRank(new BigDecimal(i));
				documentMapAggregateScore.setPercentile(new BigDecimal(1).subtract(new BigDecimal(i).divide(corpusAggregateScore.getCount())));
				if (corpusAggregateScore.getMax().compareTo(documentMapAggregateScore.getNormalized()) < 0 ) {
					corpusAggregateScore.setMax(documentMapAggregateScore.getNormalized());
				}
				if (
						(corpusAggregateScore.getMin().compareTo(documentMapAggregateScore.getNormalized()) > 0 ) 
						|| corpusAggregateScore.getMin().equals(new BigDecimal(0))
				) {
					corpusAggregateScore.setMin(documentMapAggregateScore.getNormalized());
				}
				if (corpusAggregateScore.getMedian().equals(new BigDecimal(0)) && (i > corpusAggregateScore.getCount().doubleValue()/2)) {
					corpusAggregateScore.setMedian(documentMapAggregateScore.getNormalized());
				}
			
			}
		}
	}
			
	public void scoreCorpusSubAnnotators(CorpusScore corpusScore) {
		for (CorpusDocumentAggregateScore corpusMapAggregateScore : corpusScore.getAggregateScores()) {
			//OOPLocations
			for (CorpusDocumentAggregateScore corpusAggregateScore : corpusMapAggregateScore.getSubAnnotationScores()) {
				//Kashmir
				double[] primitiveRawScores = new double[corpusAggregateScore.getAggregateScores().size()];
				double[] primitiveRankScores = new double[corpusAggregateScore.getAggregateScores().size()];
				double[] primitivePercentageScores = new double[corpusAggregateScore.getAggregateScores().size()];
				double[] primitivePercentileScores = new double[corpusAggregateScore.getAggregateScores().size()];
				double[] primitiveNormalizedScores = new double[corpusAggregateScore.getAggregateScores().size()];
				double[] primitiveZScores = new double[corpusAggregateScore.getAggregateScores().size()];
				
				//For every document's Kashmir score, create stats
				Collections.sort(corpusAggregateScore.getAggregateScores());
				Iterator<DocumentAggregateScore> documentAggregateScoreIter = corpusAggregateScore.getAggregateScores().iterator();
				for (int i=0;i<corpusAggregateScore.getAggregateScores().size() && documentAggregateScoreIter.hasNext(); i++) {
					//Kashmir, document 1234
					DocumentAggregateScore documentAggregateScore = documentAggregateScoreIter.next();
					primitiveRawScores[i] = documentAggregateScore.getRaw().doubleValue();
					primitiveRankScores[i] = documentAggregateScore.getRank().doubleValue();
					primitivePercentageScores[i] = documentAggregateScore.getPercentage().doubleValue();
					primitivePercentileScores[i] = documentAggregateScore.getPercentile().doubleValue();
					primitiveNormalizedScores[i] = documentAggregateScore.getNormalized().doubleValue();
					primitiveZScores[i] = documentAggregateScore.getZ().doubleValue();
					
					corpusAggregateScore.setTotalRaw(corpusAggregateScore.getTotalRaw().add(documentAggregateScore.getRaw()));
					if (corpusAggregateScore.getRaw().getMax().compareTo(documentAggregateScore.getRaw()) < 0 ) {
						corpusAggregateScore.getRaw().setMax(documentAggregateScore.getRaw());
					}
					if (
							(corpusAggregateScore.getRaw().getMin().compareTo(documentAggregateScore.getRaw()) > 0 ) 
							|| corpusAggregateScore.getRaw().getMin().equals(new BigDecimal(0))
					) {
						corpusAggregateScore.getRaw().setMin(documentAggregateScore.getRaw());
					}
					if (corpusAggregateScore.getRaw().getMedian().equals(new BigDecimal(0)) && (i > corpusAggregateScore.getAggregateScores().size()/2)) {
						corpusAggregateScore.getRaw().setMedian(documentAggregateScore.getRaw());
					}
					
					corpusAggregateScore.setTotalRank(corpusAggregateScore.getTotalRank().add(documentAggregateScore.getRank()));
					if (corpusAggregateScore.getRank().getMax().compareTo(documentAggregateScore.getRank()) < 0 ) {
						corpusAggregateScore.getRank().setMax(documentAggregateScore.getRank());
					}
					if (
							(corpusAggregateScore.getRank().getMin().compareTo(documentAggregateScore.getRank()) > 0 ) 
							|| corpusAggregateScore.getRank().getMin().equals(new BigDecimal(0))
					) {
						corpusAggregateScore.getRank().setMin(documentAggregateScore.getRank());
					}
					if (corpusAggregateScore.getRank().getMedian().equals(new BigDecimal(0)) && (i > corpusAggregateScore.getAggregateScores().size()/2)) {
						corpusAggregateScore.getRank().setMedian(documentAggregateScore.getRank());
					}
					
					corpusAggregateScore.setTotalPercentage(corpusAggregateScore.getTotalPercentage().add(documentAggregateScore.getPercentage()));
					if (corpusAggregateScore.getPercentage().getMax().compareTo(documentAggregateScore.getPercentage()) < 0 ) {
						corpusAggregateScore.getPercentage().setMax(documentAggregateScore.getPercentage());
					}
					if (
							(corpusAggregateScore.getPercentage().getMin().compareTo(documentAggregateScore.getPercentage()) > 0 ) 
							|| corpusAggregateScore.getPercentage().getMin().equals(new BigDecimal(0))
					) {
						corpusAggregateScore.getPercentage().setMin(documentAggregateScore.getPercentage());
					}
					if (corpusAggregateScore.getPercentage().getMedian().equals(new BigDecimal(0)) && (i > corpusAggregateScore.getAggregateScores().size()/2)) {
						corpusAggregateScore.getPercentage().setMedian(documentAggregateScore.getPercentage());
					}
					
					corpusAggregateScore.setTotalPercentile(corpusAggregateScore.getTotalPercentile().add(documentAggregateScore.getPercentile()));
					if (corpusAggregateScore.getPercentile().getMax().compareTo(documentAggregateScore.getPercentile()) < 0 ) {
						corpusAggregateScore.getPercentile().setMax(documentAggregateScore.getPercentile());
					}
					if (
							(corpusAggregateScore.getPercentile().getMin().compareTo(documentAggregateScore.getPercentile()) > 0 ) 
							|| corpusAggregateScore.getPercentile().getMin().equals(new BigDecimal(0))
					) {
						corpusAggregateScore.getPercentile().setMin(documentAggregateScore.getPercentile());
					}
					if (corpusAggregateScore.getPercentile().getMedian().equals(new BigDecimal(0)) && (i > corpusAggregateScore.getAggregateScores().size()/2)) {
						corpusAggregateScore.getPercentile().setMedian(documentAggregateScore.getPercentile());
					}
					
					corpusAggregateScore.setTotalNormalized(corpusAggregateScore.getTotalNormalized().add(documentAggregateScore.getNormalized()));
					if (corpusAggregateScore.getNormalized().getMax().compareTo(documentAggregateScore.getNormalized()) < 0 ) {
						corpusAggregateScore.getNormalized().setMax(documentAggregateScore.getNormalized());
					}
					if (
							(corpusAggregateScore.getNormalized().getMin().compareTo(documentAggregateScore.getNormalized()) > 0 ) 
							|| corpusAggregateScore.getNormalized().getMin().equals(new BigDecimal(0))
					) {
						corpusAggregateScore.getNormalized().setMin(documentAggregateScore.getNormalized());
					}
					if (corpusAggregateScore.getNormalized().getMedian().equals(new BigDecimal(0)) && (i > corpusAggregateScore.getAggregateScores().size()/2)) {
						corpusAggregateScore.getNormalized().setMedian(documentAggregateScore.getNormalized());
					}
					
					corpusAggregateScore.setTotalZ(corpusAggregateScore.getTotalZ().add(documentAggregateScore.getZ()));
					if (corpusAggregateScore.getZ().getMax().compareTo(documentAggregateScore.getZ()) < 0 ) {
						corpusAggregateScore.getZ().setMax(documentAggregateScore.getZ());
					}
					if (
							(corpusAggregateScore.getZ().getMin().compareTo(documentAggregateScore.getZ()) > 0 ) 
							|| corpusAggregateScore.getZ().getMin().equals(new BigDecimal(0))
					) {
						corpusAggregateScore.getZ().setMin(documentAggregateScore.getZ());
					}
					if (corpusAggregateScore.getZ().getMedian().equals(new BigDecimal(0)) && (i > corpusAggregateScore.getAggregateScores().size()/2)) {
						corpusAggregateScore.getZ().setMedian(documentAggregateScore.getZ());
					}
					
				}
				StandardDeviation sd = new StandardDeviation();
				corpusAggregateScore.getRaw().setMean(corpusAggregateScore.getTotalRaw().divide(corpusAggregateScore.getCount()));
				corpusAggregateScore.getRaw().setStddev(new BigDecimal(sd.evaluate(primitiveRawScores)));
				sd.clear();
				corpusAggregateScore.getRank().setMean(corpusAggregateScore.getTotalRank().divide(corpusAggregateScore.getCount()));
				corpusAggregateScore.getRank().setStddev(new BigDecimal(sd.evaluate(primitiveRankScores)));
				sd.clear();
				corpusAggregateScore.getPercentage().setMean(corpusAggregateScore.getTotalPercentage().divide(corpusAggregateScore.getCount()));
				corpusAggregateScore.getPercentage().setStddev(new BigDecimal(sd.evaluate(primitivePercentageScores)));
				sd.clear();
				corpusAggregateScore.getPercentile().setMean(corpusAggregateScore.getTotalPercentile().divide(corpusAggregateScore.getCount()));
				corpusAggregateScore.getPercentile().setStddev(new BigDecimal(sd.evaluate(primitivePercentileScores)));
				sd.clear();
				corpusAggregateScore.getNormalized().setMean(corpusAggregateScore.getTotalNormalized().divide(corpusAggregateScore.getCount()));
				corpusAggregateScore.getNormalized().setStddev(new BigDecimal(sd.evaluate(primitiveNormalizedScores)));
				sd.clear();
				corpusAggregateScore.getZ().setMean(corpusAggregateScore.getTotalZ().divide(corpusAggregateScore.getCount()));
				corpusAggregateScore.getZ().setStddev(new BigDecimal(sd.evaluate(primitiveZScores)));
				sd.clear();
			}
		}
	}
	
	
*/
}
