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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActorAnnotation extends ContextualAnnotation{
	
	public ActorAnnotation() {
		super();
		OOPMyersBriggs.put("extrovert", new BigDecimal(0));
		OOPMyersBriggs.put("introvert", new BigDecimal(0));
		OOPMyersBriggs.put("sensing", new BigDecimal(0));
		OOPMyersBriggs.put("intuitive", new BigDecimal(0));
		OOPMyersBriggs.put("thinking", new BigDecimal(0));
		OOPMyersBriggs.put("feeling", new BigDecimal(0));
		OOPMyersBriggs.put("judging", new BigDecimal(0));
		OOPMyersBriggs.put("perceiving", new BigDecimal(0));
	}
	
	protected String CoreNlpGender = "";
	protected String OOPGender = "";
	protected List<String> Quotes = new ArrayList<String>();
	protected Map<String, BigDecimal> OOPMyersBriggs = new HashMap<String, BigDecimal>();
	

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
	
	public Map<String,BigDecimal> getOOPMyersBriggs() {
		return OOPMyersBriggs;
	}
	
	public void setOOPMyersBriggs(Map<String,BigDecimal> mb) {
		OOPMyersBriggs = mb;
	}

}
