package io.outofprintmagazine.util;

public class CorpusDocumentMapAggregateScore {

	/*
	 * rollup of SubAnnotation level score (AggregatedScore) across all documents of a corpus
	 */
	public CorpusDocumentMapAggregateScore() {
		super();
	}
	
	public CorpusDocumentMapAggregateScore(String name) {
		this();
		setName(name);
	}
	
	private String name = "";
	private DocumentAggregateScore raw = new DocumentAggregateScore();
	private DocumentAggregateScore normalized = new DocumentAggregateScore();
	private DocumentAggregateScore count = new DocumentAggregateScore();
	private DocumentAggregateScore rank = new DocumentAggregateScore();
	private DocumentAggregateScore percentage = new DocumentAggregateScore();
	private DocumentAggregateScore percentile = new DocumentAggregateScore();
	private DocumentAggregateScore z = new DocumentAggregateScore();
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DocumentAggregateScore getRaw() {
		return raw;
	}

	public void setRaw(DocumentAggregateScore raw) {
		this.raw = raw;
	}

	public DocumentAggregateScore getNormalized() {
		return normalized;
	}

	public void setNormalized(DocumentAggregateScore normalized) {
		this.normalized = normalized;
	}

	public DocumentAggregateScore getCount() {
		return count;
	}

	public void setCount(DocumentAggregateScore count) {
		this.count = count;
	}

	public DocumentAggregateScore getRank() {
		return rank;
	}
	
	public void setRank(DocumentAggregateScore rank) {
		this.rank = rank;
	}
	
	public DocumentAggregateScore getPercentage() {
		return percentage;
	}
	
	public void setPercentage(DocumentAggregateScore percentage) {
		this.percentage = percentage;
	}
	
	public DocumentAggregateScore getPercentile() {
		return percentile;
	}
	
	public void setPercentile(DocumentAggregateScore percentile) {
		this.percentile = percentile;
	}
	
	public DocumentAggregateScore getZ() {
		return z;
	}
	
	public void setZ(DocumentAggregateScore z) {
		this.z = z;
	}
	
	

}
