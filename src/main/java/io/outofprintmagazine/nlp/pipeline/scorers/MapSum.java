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
package io.outofprintmagazine.nlp.pipeline.scorers;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MapSum extends MapScorer implements Scorer {

	private static final Logger logger = LogManager.getLogger(MapSum.class);
	
	protected Logger getLogger() {
		return logger;
	}

	public MapSum() {
		super();
	}
	
	@SuppressWarnings("rawtypes")
	public MapSum( Class annotationClass) {
		super(annotationClass);
	}
	

	@Override
	public Map<String, BigDecimal> aggregateScores(List<Map<String, BigDecimal>> allScores) {
		Map<String, BigDecimal> scoreMap = new HashMap<String, BigDecimal>();
		for (Map<String, BigDecimal> rawScore : allScores) {
			for (String key : rawScore.keySet()) {
				BigDecimal existingScore = scoreMap.get(key);
				if (existingScore == null) {
					scoreMap.put(key, rawScore.get(key));
				} else {
					scoreMap.put(key, existingScore.add(rawScore.get(key)));
				}
			}
		}
		return scoreMap;
	}



}
