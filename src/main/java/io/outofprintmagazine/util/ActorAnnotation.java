package io.outofprintmagazine.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonFormat;

public class ActorAnnotation {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(ActorAnnotation.class);
	
	private String CanonicalName = "";
	private String CoreNlpGender = "";
	private String OOPGender = "";
	private int FirstAppearance = -1;
	private int LastAppearance = -1;
	private BigDecimal Importance = new BigDecimal(0);
	private List<BigDecimal> VaderSentiment = new ArrayList<BigDecimal>();
	private List<BigDecimal> CoreNlpSentiment = new ArrayList<BigDecimal>();
	private List<String> Quotes = new ArrayList<String>();
	private Map<String, Map<String,BigDecimal>> Attributes = new HashMap<String, Map<String,BigDecimal>>();

	public String getCanonicalName() {
		return CanonicalName;
	}

	public void setCanonicalName(String canonicalName) {
		CanonicalName = canonicalName;
	}


	public String getCoreNlpGender() {
		return CoreNlpGender;
	}

	public void setCoreNlpGender(String coreNlpGender) {
		CoreNlpGender = coreNlpGender;
	}

	public String getOOPGender() {
		return OOPGender;
	}

	public void setOOPGender(String oOPGender) {
		OOPGender = oOPGender;
	}

	public int getFirstAppearance() {
		return FirstAppearance;
	}

	public void setFirstAppearance(int firstAppearance) {
		FirstAppearance = firstAppearance;
	}

	public int getLastAppearance() {
		return LastAppearance;
	}

	public void setLastAppearance(int lastAppearance) {
		LastAppearance = lastAppearance;
	}

	public BigDecimal getImportance() {
		return Importance;
	}

	public void setImportance(BigDecimal importance) {
		Importance = importance;
	}
	
	public void addImportance(int score) {
		Importance = getImportance().add(new BigDecimal(score));
	}

	public List<BigDecimal> getVaderSentiment() {
		return VaderSentiment;
	}

	public void addVaderSentiment(BigDecimal vaderSentiment) {
		this.VaderSentiment.add(vaderSentiment);
	}

	public List<BigDecimal> getCoreNlpSentiment() {
		return CoreNlpSentiment;
	}

	public void addCoreNlpSentiment(BigDecimal coreNlpSentiment) {
		this.CoreNlpSentiment.add(coreNlpSentiment);
	}

	public List<String> getQuotes() {
		return Quotes;
	}

	public void setQuotes(List<String> quotes) {
		Quotes = quotes;
	}

	public Map<String, Map<String, BigDecimal>> getAttributes() {
		return Attributes;
	}
	
	public Map<String, BigDecimal> getAttribute(String subAttributeName) {
		if (!getAttributes().containsKey(subAttributeName)) {
			getAttributes().put(subAttributeName, new HashMap<String, BigDecimal>());
		}
		return getAttributes().get(subAttributeName);
	}

	public ActorAnnotation() {
		super();
	}

}
