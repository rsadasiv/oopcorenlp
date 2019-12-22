package io.outofprintmagazine.util;

import java.math.BigDecimal;

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
		this.getScore().setNormalized(raw.divide(normalizer, 10, BigDecimal.ROUND_HALF_UP));
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
