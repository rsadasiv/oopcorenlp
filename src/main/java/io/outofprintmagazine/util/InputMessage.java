package io.outofprintmagazine.util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

public class InputMessage {

	public InputMessage() {
		super();
	}
	
	public InputMessage(
			String outputDirectory,
			Properties metadata,
			String inputFile) {
		super();
		setOutputDirectory(outputDirectory);
		setMetadata(metadata);
		setInputFile(inputFile);
	}
	
	private String outputDirectory;
	private Properties metadata;
	private String inputFile;
	
	public String getOutputDirectory() {
		return outputDirectory;
	}
	
	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}
	
	public Properties getMetadata() {
		return metadata;
	}
	
	public void setMetadata(Properties metadata) {
		this.metadata = metadata;
	}
	
	public String getInputFile() {
		return inputFile;
	}
	
	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}
	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("outputDirectory: ");
		buf.append('\n');
		buf.append(getOutputDirectory());
		buf.append('\n');
		StringWriter writer = new StringWriter();
		try {
			getMetadata().store(writer, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	buf.append("metadata: ");
		buf.append('\n');
    	buf.append(writer.toString());
		buf.append('\n');
		buf.append("inputFile: ");
		buf.append('\n');
		buf.append(getInputFile());
		buf.append('\n');
		return buf.toString();
	}
}
