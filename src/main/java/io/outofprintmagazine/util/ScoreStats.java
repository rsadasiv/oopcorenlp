package io.outofprintmagazine.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScoreStats {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(ScoreStats.class);

	public ScoreStats() {
		super();
	}
	
	private Score score = new Score();
	private Stats stats = new Stats();
	
	public Score getScore() {
		return score;
	}
	
	public void setScore(Score score) {
		this.score = score;
	}
	
	public Stats getStats() {
		return stats;
	}
	
	public void setStats(Stats stats) {
		this.stats = stats;
	}
	
	public String toJsonString() {
		return String.format(
				"{%n\t\"Score\": %s%n\t\"Stats\": %s%n}",
				getScore().toJsonString(), 
				getStats().toJsonString()
		);
	}	

}
