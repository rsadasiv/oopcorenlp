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
