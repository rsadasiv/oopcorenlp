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
