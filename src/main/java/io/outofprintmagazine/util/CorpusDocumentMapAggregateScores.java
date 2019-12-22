package io.outofprintmagazine.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class CorpusDocumentMapAggregateScores {

	public CorpusDocumentMapAggregateScores() {
		super();
	}
	
	private Map<String, BigDecimal> raw = new HashMap<String, BigDecimal>();
	private Map<String, BigDecimal> normalized = new HashMap<String, BigDecimal>();
	private Map<String, BigDecimal> count = new HashMap<String, BigDecimal>();
	private Map<String, BigDecimal> rank = new HashMap<String, BigDecimal>();
	private Map<String, BigDecimal> percentage = new HashMap<String, BigDecimal>();
	private Map<String, BigDecimal> percentile = new HashMap<String, BigDecimal>();
	private Map<String, BigDecimal> z = new HashMap<String, BigDecimal>();
	
	public Map<String, BigDecimal> getRaw() {
		return raw;
	}
	
	public void setRaw(Map<String, BigDecimal> raw) {
		this.raw = raw;
	}
	
	public Map<String, BigDecimal> getNormalized() {
		return normalized;
	}
	
	public void setNormalized(Map<String, BigDecimal> normalized) {
		this.normalized = normalized;
	}
	
	public Map<String, BigDecimal> getCount() {
		return count;
	}
	
	public void setCount(Map<String, BigDecimal> count) {
		this.count = count;
	}
	
	public Map<String, BigDecimal> getRank() {
		return rank;
	}
	
	public void setRank(Map<String, BigDecimal> rank) {
		this.rank = rank;
	}
	
	public Map<String, BigDecimal> getPercentage() {
		return percentage;
	}
	
	public void setPercentage(Map<String, BigDecimal> percentage) {
		this.percentage = percentage;
	}
	
	public Map<String, BigDecimal> getPercentile() {
		return percentile;
	}
	
	public void setPercentile(Map<String, BigDecimal> percentile) {
		this.percentile = percentile;
	}
	
	public Map<String, BigDecimal> getZ() {
		return z;
	}
	
	public void setZ(Map<String, BigDecimal> z) {
		this.z = z;
	}

	
}
