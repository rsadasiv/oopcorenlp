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
package io.outofprintmagazine.nlp.pipeline.serializers;

import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.stanford.nlp.pipeline.CoreDocument;

/**
 * <p>Interface for custom annotators json serialization.</p>
 * <p>serialize() should decorate the syntax tree from CoreNlpSerializer with Annotations from IOOPAnnotator.</p>
 * <p>serializeAggregate() should decorate the root node of an empty json document with Annotations from IScorer</p>
 * 
 * @author Ram Sadasiv
 *
 */
public interface ISerializer {

	public void serialize(CoreDocument document, ObjectNode json);
	
	public void serializeAggregate(Object aggregate, ObjectNode json);
}
