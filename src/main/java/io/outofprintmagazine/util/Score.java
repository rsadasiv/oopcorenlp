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
