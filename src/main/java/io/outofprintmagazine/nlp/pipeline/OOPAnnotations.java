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
package io.outofprintmagazine.nlp.pipeline;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ErasureUtils;

public class OOPAnnotations {

	private OOPAnnotations() {} // only static members

	// Problematic
	public static class OOPActorsAnnotation implements CoreAnnotation<Map<String, ActorAnnotation>> {
		@Override
		public Class<Map<String, ActorAnnotation>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}
	
	public static class OOPSettingsAnnotation implements CoreAnnotation<Map<String, SettingAnnotation>> {
		@Override
		public Class<Map<String, SettingAnnotation>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	// per token
	// Informative
	public static class OOPWordnetGlossAnnotation implements CoreAnnotation<String> {
		@Override
		public Class<String> getType() {
			return ErasureUtils.uncheckedCast(String.class);
		}
	}

	public static class OOPWikipediaGlossAnnotation implements CoreAnnotation<String> {
		@Override
		public Class<String> getType() {
			return ErasureUtils.uncheckedCast(String.class);
		}
	}

	public static class PerfecttenseGlossAnnotation implements CoreAnnotation<String> {
		@Override
		public Class<String> getType() {
			return ErasureUtils.uncheckedCast(String.class);
		}
	}

	//per token/sentence/document
	// Scalar	
	public static class OOPCharCountAnnotation implements CoreAnnotation<BigDecimal> {
		@Override
		public Class<BigDecimal> getType() {
			return ErasureUtils.uncheckedCast(BigDecimal.class);
		}
	}
	
	public static class OOPSyllableCountAnnotation implements CoreAnnotation<BigDecimal> {
		@Override
		public Class<BigDecimal> getType() {
			return ErasureUtils.uncheckedCast(BigDecimal.class);
		}
	}
	
	public static class OOPTokenCountAnnotation implements CoreAnnotation<BigDecimal> {
		@Override
		public Class<BigDecimal> getType() {
			return ErasureUtils.uncheckedCast(BigDecimal.class);
		}
	}
	
	public static class OOPWordCountAnnotation implements CoreAnnotation<BigDecimal> {
		@Override
		public Class<BigDecimal> getType() {
			return ErasureUtils.uncheckedCast(BigDecimal.class);
		}
	}
	
	public static class OOPSentenceCountAnnotation implements CoreAnnotation<BigDecimal> {
		@Override
		public Class<BigDecimal> getType() {
			return ErasureUtils.uncheckedCast(BigDecimal.class);
		}
	}
	
	public static class OOPParagraphCountAnnotation implements CoreAnnotation<BigDecimal> {
		@Override
		public Class<BigDecimal> getType() {
			return ErasureUtils.uncheckedCast(BigDecimal.class);
		}
	}

	public static class OOPFleschKincaidAnnotation implements CoreAnnotation<BigDecimal> {
		@Override
		public Class<BigDecimal> getType() {
			return ErasureUtils.uncheckedCast(BigDecimal.class);
		}
	}

	public static class CoreNlpSentimentAnnotation implements CoreAnnotation<BigDecimal> {
		@Override
		public Class<BigDecimal> getType() {
			return ErasureUtils.uncheckedCast(BigDecimal.class);
		}
	}

	public static class VaderSentimentAnnotation implements CoreAnnotation<BigDecimal> {
		@Override
		public Class<BigDecimal> getType() {
			return ErasureUtils.uncheckedCast(BigDecimal.class);
		}
	}
	
	public static class OOPVerblessSentencesAnnotation implements CoreAnnotation<BigDecimal> {
		@Override
		public Class<BigDecimal> getType() {
			return ErasureUtils.uncheckedCast(BigDecimal.class);
		}
	}

	// Categorical by sentence/document
	// List of PhraseScore
	public static class OOPQuotesAnnotation implements CoreAnnotation<List<PhraseAnnotation>> {
		@Override
		public Class<List<PhraseAnnotation>> getType() {
			return ErasureUtils.uncheckedCast(List.class);
		}
	}

	public static class OOPLikeAnnotation implements CoreAnnotation<List<PhraseAnnotation>> {
		@Override
		public Class<List<PhraseAnnotation>> getType() {
			return ErasureUtils.uncheckedCast(List.class);
		}
	}

	public static class OOPAsAnnotation implements CoreAnnotation<List<PhraseAnnotation>> {
		@Override
		public Class<List<PhraseAnnotation>> getType() {
			return ErasureUtils.uncheckedCast(List.class);
		}
	}

	public static class OOPWhoAnnotation implements CoreAnnotation<List<PhraseAnnotation>> {
		@Override
		public Class<List<PhraseAnnotation>> getType() {
			return ErasureUtils.uncheckedCast(List.class);
		}
	}

	public static class OOPWhatAnnotation implements CoreAnnotation<List<PhraseAnnotation>> {
		@Override
		public Class<List<PhraseAnnotation>> getType() {
			return ErasureUtils.uncheckedCast(List.class);
		}
	}

	public static class OOPWhenAnnotation implements CoreAnnotation<List<PhraseAnnotation>> {
		@Override
		public Class<List<PhraseAnnotation>> getType() {
			return ErasureUtils.uncheckedCast(List.class);
		}
	}

	public static class OOPWhereAnnotation implements CoreAnnotation<List<PhraseAnnotation>> {
		@Override
		public Class<List<PhraseAnnotation>> getType() {
			return ErasureUtils.uncheckedCast(List.class);
		}
	}

	public static class OOPWhyAnnotation implements CoreAnnotation<List<PhraseAnnotation>> {
		@Override
		public Class<List<PhraseAnnotation>> getType() {
			return ErasureUtils.uncheckedCast(List.class);
		}
	}

	public static class OOPHowAnnotation implements CoreAnnotation<List<PhraseAnnotation>> {
		@Override
		public Class<List<PhraseAnnotation>> getType() {
			return ErasureUtils.uncheckedCast(List.class);
		}
	}

	public static class OOPIfAnnotation implements CoreAnnotation<List<PhraseAnnotation>> {
		@Override
		public Class<List<PhraseAnnotation>> getType() {
			return ErasureUtils.uncheckedCast(List.class);
		}
	}

	public static class OOPBecauseAnnotation implements CoreAnnotation<List<PhraseAnnotation>> {
		@Override
		public Class<List<PhraseAnnotation>> getType() {
			return ErasureUtils.uncheckedCast(List.class);
		}
	}

	public static class OOPDatesAnnotation implements CoreAnnotation<List<PhraseAnnotation>> {
		@Override
		public Class<List<PhraseAnnotation>> getType() {
			return ErasureUtils.uncheckedCast(List.class);
		}
	}

	// Categorical by token/sentence/document
	// Map<String,BigDecimal>
	public static class PerfecttenseAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}


	public static class OOPSVOAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPAmericanizeAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPAngliciseAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPCommonWordsAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPFunctionWordsAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}


	public static class OOPWordlessWordsAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPUncommonWordsAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPGenderAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class CoreNlpGenderAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPPronounAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPVerbTenseAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}
	
	public static class OOPPunctuationMarkAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPAdjectivesAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPPointlessAdjectivesAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPAdjectiveCategoriesAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPAdverbsAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPPointlessAdverbsAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPAdverbCategoriesAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPPrepositionsAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPPrepositionCategoriesAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPColorsAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPFlavorsAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPVerbsAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPActionlessVerbsAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPNounsAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPTopicsAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

//	  public static class OOPWikipediaRelatedNounsAnnotation implements CoreAnnotation<Map<String,BigDecimal>> {
//		    @Override
//		    public Class<Map<String,BigDecimal>> getType() {
//		      return ErasureUtils.uncheckedCast(Map.class);
//		    }
//	  }
//	  
//	  public static class OOPWikipediaRelatedNounsAnnotationAggregate implements CoreAnnotation<DocumentAggregateScore> {
//		    @Override
//		    public Class<DocumentAggregateScore> getType() {
//		      return DocumentAggregateScore.class;
//		    }
//	  }

	public static class OOPWikipediaPageviewTopicsAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPWikipediaCategoriesAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPVerbHypernymsAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPVerbGroupsAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPNounGroupsAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPVerbnetGroupsAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPNounHypernymsAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPTemporalNGramsAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPLocationsAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPPeopleAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPNonAffirmativeAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPWordsAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPPossessivesAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPBiberAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPMyersBriggsAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}

	public static class OOPBiberDimensionsAnnotation implements CoreAnnotation<Map<String, BigDecimal>> {
		@Override
		public Class<Map<String, BigDecimal>> getType() {
			return ErasureUtils.uncheckedCast(Map.class);
		}
	}
}
