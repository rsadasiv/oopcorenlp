package io.outofprintmagazine.nlp.pipeline;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SettingAnnotation extends ContextualAnnotation {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(SettingAnnotation.class);
	
	private Logger getLogger() {
		return logger;
	}
	
	public SettingAnnotation() {
		super();
	}
}
