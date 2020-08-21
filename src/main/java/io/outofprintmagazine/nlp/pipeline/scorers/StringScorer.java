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
package io.outofprintmagazine.nlp.pipeline.scorers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.stanford.nlp.pipeline.CoreDocument;

public class StringScorer implements IScorer {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(StringScorer.class);


	protected Class annotationClass = null;


	public StringScorer() {
		super();
	}

	public StringScorer(Class annotationClass) {
		super();
		this.setAnnotationClass(annotationClass);
	}
	

	public void setAnnotationClass(Class annotationClass) {
		this.annotationClass = annotationClass;
	}

	public Class getAnnotationClass() {
		return this.annotationClass;
	}
	
	@Override
	public void score(CoreDocument document) {
		//pass
	}

	@Override
	public Object aggregateDocument(CoreDocument document) {
		// pass
		return null;
	}
	
}
