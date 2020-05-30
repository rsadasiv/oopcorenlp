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
package io.outofprintmagazine.nlp.pipeline;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ContextualAnnotation {
	
	protected String CanonicalName = "";
	protected int FirstAppearance = -1;
	protected int LastAppearance = -1;
	protected BigDecimal Importance = new BigDecimal(0);
	protected List<BigDecimal> VaderSentiment = new ArrayList<BigDecimal>();
	protected List<BigDecimal> CoreNlpSentiment = new ArrayList<BigDecimal>();
	protected Map<String, Map<String,BigDecimal>> Attributes = new HashMap<String, Map<String,BigDecimal>>();
	protected List<String> Thumbnails = new ArrayList<String>();
	protected BigDecimal VaderSentimentAvg = new BigDecimal(.5);
	protected BigDecimal CoreNlpSentimentAvg = new BigDecimal(.5);

	public String getCanonicalName() {
		return CanonicalName;
	}

	public void setCanonicalName(String canonicalName) {
		CanonicalName = canonicalName;
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

	public Map<String, Map<String, BigDecimal>> getAttributes() {
		return Attributes;
	}
	
	public Map<String, BigDecimal> getAttribute(String subAttributeName) {
		if (!getAttributes().containsKey(subAttributeName)) {
			getAttributes().put(subAttributeName, new HashMap<String, BigDecimal>());
		}
		return getAttributes().get(subAttributeName);
	}
	
	public List<String> getThumbnails() {
		return Thumbnails;
	}

	public void setThumbnails(List<String> bingThumbnails) {
		this.Thumbnails = bingThumbnails;
	}
	

	public void setVaderSentiment(List<BigDecimal> vaderSentiment) {
		VaderSentiment = vaderSentiment;
	}

	public void setCoreNlpSentiment(List<BigDecimal> coreNlpSentiment) {
		CoreNlpSentiment = coreNlpSentiment;
	}

	public BigDecimal getVaderSentimentAvg() {
		return VaderSentimentAvg;
	}

	public void setVaderSentimentAvg(BigDecimal vaderSentimentAvg) {
		VaderSentimentAvg = vaderSentimentAvg;
	}
	
	public void setVaderSentimentAvg() {
		BigDecimal total = new BigDecimal(0);
		for (BigDecimal score : getVaderSentiment()) {
			total = total.add(score);
		}
		setVaderSentimentAvg(total.divide(new BigDecimal(getVaderSentiment().size()), 10, RoundingMode.HALF_DOWN));
	}

	public BigDecimal getCoreNlpSentimentAvg() {
		return CoreNlpSentimentAvg;
	}

	public void setCoreNlpSentimentAvg(BigDecimal coreNlpSentimentAvg) {
		CoreNlpSentimentAvg = coreNlpSentimentAvg;
	}
	
	public void setCoreNlpSentimentAvg() {
		BigDecimal total = new BigDecimal(0);
		for (BigDecimal score : getCoreNlpSentiment()) {
			total = total.add(score);
		}
		setCoreNlpSentimentAvg(total.divide(new BigDecimal(getCoreNlpSentiment().size()), 10, RoundingMode.HALF_DOWN));
	}

	public void setAttributes(Map<String, Map<String, BigDecimal>> attributes) {
		Attributes = attributes;
	}

	public ContextualAnnotation() {
		super();
	}

}
