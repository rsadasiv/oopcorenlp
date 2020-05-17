package io.outofprintmagazine.nlp.pipeline;

import java.math.BigDecimal;

public class PhraseAnnotation {

	private String Name = "";
	private BigDecimal Value = new BigDecimal(0);
	
	public PhraseAnnotation() {
		super();
	}

	public PhraseAnnotation(String name, BigDecimal value) {
		this();
		this.setName(name);
		this.setValue(value.add(new BigDecimal(0)));
	}
	
	public PhraseAnnotation(PhraseAnnotation p) {
		this(p.getName(), p.getValue());
	}
	

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public BigDecimal getValue() {
		return Value;
	}

	public void setValue(BigDecimal value) {
		Value = value;
	}
	
	@Override
	public String toString() {
		return (String.format("%s:%d", getName(), getValue().intValue()));
	}

}
