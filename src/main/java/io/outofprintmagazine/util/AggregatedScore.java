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
import java.math.RoundingMode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AggregatedScore implements Comparable<AggregatedScore>{
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(AggregatedScore.class);

	public AggregatedScore() {
		super();
	}
	
	@Override
	public int compareTo(AggregatedScore o) {
		//sort by normalized desc, name asc
		int retval = 0;
		retval = o.getScore().getNormalized().compareTo(getScore().getNormalized());
		if (retval == 0) {
			retval = getName().compareTo(o.getName());
		}
		return retval;
	}
	
	public AggregatedScore(String name) {
		this();
		this.setName(name);
	}
	
	public AggregatedScore(String name, BigDecimal raw, BigDecimal normalizer) {
		this(name);
		this.getScore().setRaw(raw);
		this.getScore().setNormalized(raw.divide(normalizer, 10, RoundingMode.HALF_UP));
	}
	
	public AggregatedScore(String name, BigDecimal raw, BigDecimal normalizer, BigDecimal count) {
		this(name, raw, normalizer);
		this.getScore().setCount(count);
	}
	
	private String name = new String();
	private Score score = new Score();
	private AggregateScore aggregateScore = new AggregateScore();
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Score getScore() {
		return score;
	}
	
	public void setScore(Score score) {
		this.score = score;
	}
	
	public AggregateScore getAggregateScore() {
		return aggregateScore;
	}
	
	public void setAggregateScore(AggregateScore aggregateScore) {
		this.aggregateScore = aggregateScore;
	}
	
	public String toJsonString() {
		return String.format(
				"{%n\t\"Name\": %s%n\t\"Score\": %s%n\t\"AggregateScore\": %s%n}",
				getName(),
				getScore().toJsonString(), 
				getAggregateScore().toJsonString()
		);
	}
	
}
