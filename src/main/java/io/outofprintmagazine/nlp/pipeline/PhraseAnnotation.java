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

public class PhraseAnnotation {

	private String Name = "";
	private BigDecimal Value = new BigDecimal(0);
	
	public PhraseAnnotation() {
		super();
	}

	public PhraseAnnotation(String name, BigDecimal value) {
		this();
		this.setName(name);
		this.setValue(value.add(new BigDecimal(0)));
	}
	
	public PhraseAnnotation(PhraseAnnotation p) {
		this(p.getName(), p.getValue());
	}
	

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public BigDecimal getValue() {
		return Value;
	}

	public void setValue(BigDecimal value) {
		Value = value;
	}
	
	@Override
	public String toString() {
		return (String.format("%s:%d", getName(), getValue().intValue()));
	}

}
