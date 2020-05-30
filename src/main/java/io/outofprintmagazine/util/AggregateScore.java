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

import java.math.BigDecimal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AggregateScore {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(AggregateScore.class);

	public AggregateScore() {
		super();
	}
	
	private BigDecimal rank = new BigDecimal(0);
	private BigDecimal percentage = new BigDecimal(0);
	private BigDecimal percentile = new BigDecimal(0);
	private BigDecimal z = new BigDecimal(0);
	
	public BigDecimal getRank() {
		return rank;
	}
	
	public void setRank(BigDecimal rank) {
		this.rank = rank;
	}
	
	public BigDecimal getPercentage() {
		return percentage;
	}
	
	public void setPercentage(BigDecimal percentage) {
		this.percentage = percentage;
	}
	
	public BigDecimal getPercentile() {
		return percentile;
	}
	
	public void setPercentile(BigDecimal percentile) {
		this.percentile = percentile;
	}

	public BigDecimal getZ() {
		return z;
	}
	
	public void setZ(BigDecimal z) {
		this.z = z;
	}
	
	public String toJsonString() {
		return String.format(
				"{%n\t\"Rank\": %f%n\t\"Percentage\": %f%n\t\"Percentile\": %f%n\t\"Z\": %s%n}",
				getRank(), 
				getPercentage(), 
				getPercentile(),
				getZ()
		);
	}

}
