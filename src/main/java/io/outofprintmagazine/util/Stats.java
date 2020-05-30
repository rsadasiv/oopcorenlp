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

public class Stats {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(Stats.class);

	public Stats() {
		super();
	}
	
	private BigDecimal min = new BigDecimal(0);
	private BigDecimal max = new BigDecimal(0);
	private BigDecimal mean = new BigDecimal(0);
	private BigDecimal median = new BigDecimal(0);
	private BigDecimal stddev = new BigDecimal(0);
	
	public BigDecimal getMin() {
		return min;
	}
	
	public void setMin(BigDecimal min) {
		this.min = min;
	}
	
	public BigDecimal getMax() {
		return max;
	}
	
	public void setMax(BigDecimal max) {
		this.max = max;
	}
	
	public BigDecimal getMean() {
		return mean;
	}
	
	public void setMean(BigDecimal mean) {
		this.mean = mean;
	}
	
	public BigDecimal getMedian() {
		return median;
	}
	
	public void setMedian(BigDecimal median) {
		this.median = median;
	}
	
	public BigDecimal getStddev() {
		return stddev;
	}
	
	public void setStddev(BigDecimal stddev) {
		this.stddev = stddev;
	}
	
	public String toJsonString() {
		return String.format(
				"{%n\t\"Min\": %f%n\t\"Max\": %f%n\t\"Mean\": %f%n\t\"Median\": %f%n\t\"Stddev\": %f%n}",
				getMin(), 
				getMax(), 
				getMean(),
				getMedian(),
				getStddev()
		);
	}
}
