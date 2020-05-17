package io.outofprintmagazine.nlp.pipeline;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ActorAnnotation extends ContextualAnnotation{

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(ActorAnnotation.class);
	
	private Logger getLogger() {
		return logger;
	}
	
	public ActorAnnotation() {
		super();
	}
	
	protected String CoreNlpGender = "";
	protected String OOPGender = "";
	protected List<String> Quotes = new ArrayList<String>();
	protected BigDecimal Extrovert = new BigDecimal(0);
	protected BigDecimal Introvert = new BigDecimal(0);
	protected BigDecimal Sensing = new BigDecimal(0);
	protected BigDecimal Intuitive = new BigDecimal(0);
	protected BigDecimal Thinking = new BigDecimal(0);
	protected BigDecimal Feeling = new BigDecimal(0);
	protected BigDecimal Judging = new BigDecimal(0);
	protected BigDecimal Perceiving = new BigDecimal(0);

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


	public List<String> getQuotes() {
		return Quotes;
	}

	public void setQuotes(List<String> quotes) {
		Quotes = quotes;
	}
	
	public BigDecimal getExtrovert() {
		return Extrovert;
	}

	public void setExtrovert(BigDecimal extrovert) {
		Extrovert = extrovert;
	}

	public BigDecimal getIntrovert() {
		return Introvert;
	}

	public void setIntrovert(BigDecimal introvert) {
		Introvert = introvert;
	}

	public BigDecimal getSensing() {
		return Sensing;
	}

	public void setSensing(BigDecimal sensing) {
		Sensing = sensing;
	}

	public BigDecimal getIntuitive() {
		return Intuitive;
	}

	public void setIntuitive(BigDecimal intuitive) {
		Intuitive = intuitive;
	}

	public BigDecimal getThinking() {
		return Thinking;
	}

	public void setThinking(BigDecimal thinking) {
		Thinking = thinking;
	}

	public BigDecimal getFeeling() {
		return Feeling;
	}

	public void setFeeling(BigDecimal feeling) {
		Feeling = feeling;
	}

	public BigDecimal getJudging() {
		return Judging;
	}

	public void setJudging(BigDecimal judging) {
		Judging = judging;
	}

	public BigDecimal getPerceiving() {
		return Perceiving;
	}

	public void setPerceiving(BigDecimal perceiving) {
		Perceiving = perceiving;
	}
}
