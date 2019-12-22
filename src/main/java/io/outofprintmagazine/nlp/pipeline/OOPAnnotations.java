package io.outofprintmagazine.nlp.pipeline;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ErasureUtils;
import io.outofprintmagazine.util.ActorAnnotation;
import io.outofprintmagazine.util.DocumentAggregateScore;

public class OOPAnnotations {

	private OOPAnnotations() { } // only static members
	
	  public static class OOPDocumentLengthAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  //Informative
	  public static class OOPWordnetGlossAnnotation implements CoreAnnotation<Map<String,List<String>>> {
		    @Override
		    public Class<Map<String,List<String>>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPWikipediaGlossAnnotation implements CoreAnnotation<Map<String,List<String>>> {
		    @Override
		    public Class<Map<String,List<String>>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }

	  //Scalar
	  public static class OOPSyllablesAnnotation implements CoreAnnotation<BigDecimal> {
		    @Override
		    public Class<BigDecimal> getType() {
		      return ErasureUtils.uncheckedCast(BigDecimal.class);
		    }
	  }
	  
	  public static class OOPSyllablesAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPFleschKincaidAnnotation implements CoreAnnotation<BigDecimal> {
		    @Override
		    public Class<BigDecimal> getType() {
		      return ErasureUtils.uncheckedCast(BigDecimal.class);
		    }
	  }
	  
	  public static class OOPFleschKincaidAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class CoreNlpSentimentAnnotation implements CoreAnnotation<BigDecimal> {
		    @Override
		    public Class<BigDecimal> getType() {
		      return ErasureUtils.uncheckedCast(BigDecimal.class);
		    }
	  }
	  
	  public static class CoreNlpSentimentAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }	
	  
	  public static class VaderSentimentAnnotation implements CoreAnnotation<BigDecimal> {
		    @Override
		    public Class<BigDecimal> getType() {
		      return ErasureUtils.uncheckedCast(BigDecimal.class);
		    }
	  }
	  
	  public static class VaderSentimentAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class VaderRollingSentimentAnnotation implements CoreAnnotation<BigDecimal> {
		    @Override
		    public Class<BigDecimal> getType() {
		      return ErasureUtils.uncheckedCast(BigDecimal.class);
		    }
	  }
	  
	  public static class VaderRollingSentimentAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }

	  //Problematic
	  public static class OOPActorsAnnotation implements CoreAnnotation<Map<String,ActorAnnotation>> {
		    @Override
		    public Class<Map<String,ActorAnnotation>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  	  
	  public static class OOPUntaggedTopicsAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  //Map and List
	  public static class OOPQuestionsAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPQuestionsAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPQuestionsAnnotationList implements CoreAnnotation<Map<String,List<String>>> {
		    @Override
		    public Class<Map<String,List<String>>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPQuotesAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPQuotesAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPQuotesAnnotationList implements CoreAnnotation<Map<String,List<String>>> {
		    @Override
		    public Class<Map<String,List<String>>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPWhaAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		    @Override
		    public Class<Map<String, BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPWhaAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPWhaAnnotationList implements CoreAnnotation<Map<String,List<String>>> {
		    @Override
		    public Class<Map<String,List<String>>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPSimileAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPSimileAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPSimileAnnotationList implements CoreAnnotation<Map<String,List<String>>> {
		    @Override
		    public Class<Map<String,List<String>>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }

	  //Map
	  public static class OOPSVOAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPSVOAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPDatesAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }

	  public static class OOPDatesAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPAmericanizeAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPAmericanizeAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }

	  public static class OOPAngliciseAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPAngliciseAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }	  
	  
	  public static class OOPCommonWordsAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPCommonWordsAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPWordlessWordsAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPWordlessWordsAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	   
	  public static class OOPUncommonWordsAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPUncommonWordsAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPVerblessSentencesAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPVerblessSentencesAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPGenderAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPGenderAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class CoreNlpGenderAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class CoreNlpGenderAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPPronounAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPPronounAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPVerbTenseAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPVerbTenseAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPPunctuationMarkAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPPunctuationMarkAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPAdjectivesAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPAdjectivesAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPPointlessAdjectivesAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPPointlessAdjectivesAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPAdjectiveCategoriesAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPAdjectiveCategoriesAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPAdverbsAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPAdverbsAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPPointlessAdverbsAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPPointlessAdverbsAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPAdverbCategoriesAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPAdverbCategoriesAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPPrepositionsAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPPrepositionsAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPPrepositionCategoriesAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPPrepositionCategoriesAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  

	  public static class OOPColorsAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPColorsAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPFlavorsAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }

	  public static class OOPFlavorsAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPVerbsAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPVerbsAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPActionlessVerbsAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPActionlessVerbsAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPNounsAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPNounsAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPTopicsAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPTopicsAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }

	  public static class OOPWikipediaRelatedNounsAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPWikipediaRelatedNounsAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPWikipediaPageviewTopicsAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPWikipediaPageviewTopicsAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPWikipediaCategoriesAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPWikipediaCategoriesAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPVerbHypernymsAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPVerbHypernymsAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPVerbGroupsAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPVerbGroupsAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPNounGroupsAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPNounGroupsAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPVerbnetGroupsAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPVerbnetGroupsAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPNounHypernymsAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPNounHypernymsAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPTemporalNGramsAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPTemporalNGramsAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }

	  
	  public static class OOPLocationsAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPLocationsAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPPeopleAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPPeopleAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  	  
	  public static class OOPNonAffirmativeAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPNonAffirmativeAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPWordsAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPWordsAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPPossessivesAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPPossessivesAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }

	  public static class OOPBiberAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPBiberAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
	  
	  public static class OOPBiberDimensionsAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
		    @Override
		    public Class<Map<String,BigDecimal>> getType() {
		      return ErasureUtils.uncheckedCast(Map.class);
		    }
	  }
	  
	  public static class OOPBiberDimensionsAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
		    @Override
		    public Class<DocumentAggregateScore> getType() {
		      return DocumentAggregateScore.class;
		    }
	  }
 
}
