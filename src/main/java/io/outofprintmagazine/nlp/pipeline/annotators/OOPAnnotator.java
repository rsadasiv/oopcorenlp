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

import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.CoreDocument;
import io.outofprintmagazine.util.ParameterStore;

/**
 * <p>Interface for all custom annotators.</p>
 * <p>Extends the corenlp annotator interface.</p>
 * <p>Implementations must be re-entrant.</p>
 * <p>Instances will be used by Analyzer as follows:</p>
 * <code>
 * OOPAnnotator oopAnnotator = new Annotator();
 * oopAnnotator.init(parameterStore);
 * oopAnnotator.annotate(coreDocument);
 * oopAnnotator.serialize(coreDocument, json);
 * oopAnnotator.serializeAggregateDocument(coreDocument, json);
 * oopAnnotator.getAnnotationClass();
 * oopAnnotator.getDescription();
 * </code>
 * @see AbstractAnnotator
 * @author Ram Sadasiv
 *
 */
public interface OOPAnnotator extends Annotator {
	
	public Class getAnnotationClass();
	
	public String getDescription();
	
	public void score(CoreDocument document);
	
	public void serialize(CoreDocument document, ObjectNode json);
	
	public void serializeAggregateDocument(CoreDocument document, ObjectNode json);
	
	public void init(ParameterStore properties);

}
