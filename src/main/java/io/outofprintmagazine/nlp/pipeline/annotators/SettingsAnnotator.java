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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.flickr4java.flickr.FlickrException;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.util.ArraySet;
import io.outofprintmagazine.nlp.pipeline.ContextualAnnotation;
import io.outofprintmagazine.nlp.pipeline.SettingAnnotation;
import io.outofprintmagazine.nlp.utils.BingUtils;
import io.outofprintmagazine.nlp.utils.FlickrUtils;
import io.outofprintmagazine.nlp.utils.WikimediaUtils;

public class SettingsAnnotator extends AbstractContextualAnnotator implements Annotator, OOPAnnotator {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(SettingsAnnotator.class);
	
	@SuppressWarnings("unused")
	private Logger getLogger() {
		return logger;
	}

	@Override
	protected ContextualAnnotation getConcreteAnnotation() {
		return new SettingAnnotation();
	}
	
	public SettingsAnnotator() {
		super();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(
			new ArraySet<>(
				Arrays.asList(
					CoreAnnotations.TextAnnotation.class, 
					CoreAnnotations.TokensAnnotation.class,
					CoreAnnotations.LemmaAnnotation.class,
					CoreAnnotations.SentencesAnnotation.class,
					CoreAnnotations.NamedEntityTagAnnotation.class,
					CoreAnnotations.NormalizedNamedEntityTagAnnotation.class,
					CoreAnnotations.CanonicalEntityMentionIndexAnnotation.class,
					CorefCoreAnnotations.CorefChainAnnotation.class,
					SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class,
					CoreAnnotations.QuotationsAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPLocationsAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.VaderSentimentAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.CoreNlpSentimentAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.CoreNlpGenderAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPGenderAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbGroupsAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPVerbsAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPNounsAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdjectivesAnnotation.class,
					io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPAdverbsAnnotation.class
					
				)
			)
		);
	}
	
	@Override
	protected void scoreThumbnails(ContextualAnnotation annotation) throws IOException, URISyntaxException, FlickrException {
		String locationName = annotation.getCanonicalName();
		String[] names = annotation.getCanonicalName().split(" ");
		if (names.length > 1) {
			if (names[0].startsWith("East") || names[0].startsWith("West") || names[0].startsWith("North") || names[0].startsWith("South")) {
				locationName = names[1];		}
			else {
				locationName = names[0];
			}
		}
		if (getParameterStore().getProperty("azure_apiKey") != null) {
			annotation.getThumbnails().addAll(BingUtils.getInstance(getParameterStore()).getImagesByText(locationName));
		}
		else if (getParameterStore().getProperty("flickr_apiKey") != null) {
			annotation.getThumbnails().addAll(FlickrUtils.getInstance(getParameterStore()).getImagesByText(locationName));
		}
		else {
			annotation.getThumbnails().addAll(WikimediaUtils.getInstance(getParameterStore()).getImagesByText(locationName));
		}
		
	}

	
	@SuppressWarnings("rawtypes")
	@Override
	public Class getAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPSettingsAnnotation.class;
	}
	
	@Override
	public Class getEntityAnnotationClass() {
		return io.outofprintmagazine.nlp.pipeline.OOPAnnotations.OOPLocationsAnnotation.class;
	}
	
	@Override
	public String getDescription() {
		return "OOPLocationAnnotation with sentiment, quotes, coref, and universal dependencies nsubj, nmod:poss, advmod, amod (http://universaldependencies.org/docsv1/en/dep/index.html)." ;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void annotate(Annotation annotation) {
		super.annotate(annotation);
		
	}
}
