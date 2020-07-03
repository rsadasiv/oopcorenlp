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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import io.outofprintmagazine.nlp.pipeline.scorers.Scorer;
import io.outofprintmagazine.nlp.pipeline.scorers.StringScorer;
import io.outofprintmagazine.nlp.pipeline.serializers.Serializer;
import io.outofprintmagazine.nlp.pipeline.serializers.StringSerializer;
import io.outofprintmagazine.nlp.utils.WikipediaUtils2;

public class WikipediaGlossAnnotator extends AbstractPosAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(WikipediaGlossAnnotator.class);
	
	@Override
	protected Logger getLogger() {
		return logger;
	}
	
	public WikipediaGlossAnnotator() {
		super();
		this.setTags(Arrays.asList("NN","NNS", "NNP", "NNPS"));
		this.setScorer((Scorer)new StringScorer(this.getAnnotationClass()));
		this.setSerializer((Serializer)new StringSerializer(this.getAnnotationClass()));		
	}
	
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPWikipediaGlossAnnotation.class;
	}
	
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		List<String> queries = new ArrayList<String>();
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				String topic = scoreLemma(token);
				if (topic != null) {
					queries.add(topic);
				}
			}
		}
		try {
			WikipediaUtils2.getInstance(getParameterStore()).addToWordCache(queries);
			for (int i=0;i<document.tokens().size();i++) {
				CoreLabel token = document.tokens().get(i);
				try {
					String topic = scoreLemma(token);
					if (topic != null) {
						String wordCache = WikipediaUtils2.getInstance(getParameterStore()).getWordCache(token.lemma());
						if (wordCache != null && !wordCache.equals("")) {
							//Map<String, List<String>> scoreMap = new HashMap<String, List<String>>();
							//scoreMap.put(toAlphaNumeric(token.lemma()), Arrays.asList(wordCache));
							token.set(getAnnotationClass(), wordCache);
						}
					}
				}
				catch (Throwable t) {
					logger.error("wtf?", t);
				}
			}
			WikipediaUtils2.getInstance(getParameterStore()).pruneWordCache();
		}
		catch (Exception e) {
			logger.error(e);
		}
	}
	
/*	@SuppressWarnings("unchecked")
	@Override
	public void annotate(Annotation annotation) {
		CoreDocument document = new CoreDocument(annotation);
		for (CoreSentence sentence : document.sentences()) {
			for (CoreLabel token : sentence.tokens()) {
				Map<String, List<String>> scoreMap = new HashMap<String, List<String>>();
				String topic = scoreToken(token);
				if (topic != null) {
					try {
						String pageText = WikipediaUtils.getInstance().getWikipediaPageText(topic);
						if (pageText != null && pageText.length() > 0 && pageText.indexOf(".") > -1 ) {
							scoreMap.put(token.lemma(), Arrays.asList(pageText.substring(0, pageText.indexOf(".")).trim()));
							token.set(getAnnotationClass(), scoreMap);
						}
					}
					catch (Exception e) {
						logger.error(e);
					}
				}

			}
		}
		score(document);
	}*/

	@Override
	public String getDescription() {
		return "Wikipedia gloss (first sentence of page body) if NN,NNS,NNP,NNPS.";
	}

}
