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
package io.outofprintmagazine.util;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Score implements Serializable {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(Score.class);

	private static final long serialVersionUID = 1L;

	public Score() {
		super();
	}
	
	public Score(BigDecimal raw, BigDecimal normalized, BigDecimal count) {
		this();
		setRaw(raw);
		setNormalized(normalized);
		setCount(count);
	}
	
	public Score add(Score score) {
		return new Score(
					this.getRaw().add(score.getRaw()),
					this.getNormalized().add(score.getNormalized()),
					this.getCount().add(score.getCount())
				);
	}
	
	private BigDecimal raw = new BigDecimal(0);
	private BigDecimal normalized = new BigDecimal(0);
	private BigDecimal count = new BigDecimal(0);

	
	public BigDecimal getRaw() {
		return raw;
	}
	
	public void setRaw(BigDecimal raw) {
		this.raw = raw;
	}
	
	public BigDecimal getNormalized() {
		return normalized;
	}
	
	public void setNormalized(BigDecimal normalized) {
		this.normalized = normalized;
	}
	
	public BigDecimal getCount() {
		return count;
	}
	
	public void setCount(BigDecimal count) {
		this.count = count;
	}
	
	public String toJsonString() {
		return String.format(
				"{%n\t\"Raw\": %f%n\t\"Normalized\": %f%n\t\"Count\": %f%n}",
				getRaw(), 
				getNormalized(), 
				getCount()
		);
	}
}
