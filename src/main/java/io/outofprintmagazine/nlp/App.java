package io.outofprintmagazine.nlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.util.StringUtils;
import io.outofprintmagazine.util.ParameterStore;
import io.outofprintmagazine.util.ParameterStorePropertiesFile;

/**
 * <p>The main cli entry point for oopcorenlp.</p> 
 * 
 * <p>To get the sample output run:</p>
 * <p>java -jar oopcorenlp-1.0.jar -a generate</p>
 * <p>java -Xms8096m -Xmx10120m -jar oopcorenlp-1.0.jar -a analyze</p>
 * <p>&nbsp;</p>
 * <p>To get custom output, generate, tweak, analyze.</p>
 * <p>Annotator order is important. Try commenting out existing lines rather than editing.</p>
 * <p>Copy output files to oopcorenlp_web WebContent/Corpora/corpus/ directory and build/deploy to view results.</p>
 * <p>&nbsp;</p>
 * <pre>
 * usage: oopcorenlp
 * -a,--action                 REQUIRED. analyze or generate. analyze requires all parameters. generate requires outputPath.
 * -h,--help                   print usage
 * -i,--inputPath              Location for input files (.).
 * -m,--metadata               Name of metadata file in Properties format (metadata.properties).
 * -n,--annotators             Name of annotator list file in text format (annotators.txt).
 * -o,--outputPath             Location for output files (.).
 * -p,--parameterStore         Name of parameterStore file in Properties format (parameterStore.properties).
 * -s,--story                  Name of story file in text format (story.txt). Ascii character set, vertical double quote for quotations, vertical single quote for apostrophe, block paragraphs.
 * </pre>
 * <p>NB: Needs a lot of memory to run. -Xms8096m -Xmx10120m at a minimum.</p>
 * @see Analyzer
 * @author Ram Sadasiv
 *
 */
public class App {

	private static final Logger logger = LogManager.getLogger(App.class);
	
	public App() {
		super();
	}
	
	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, ParseException {
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption( "a", "action", true, "REQUIRED. analyze or generate. analyze requires all parameters. generate requires outputPath." );
		options.addOption( "i", "inputPath", true, "Location for input files (.)." );
		options.addOption( "o", "outputPath", true, "Location for output files (.)." );
		options.addOption( "p", "parameterStore", true, "Name of parameterStore file in Properties format (parameterStore.properties)." );
		options.addOption( "m", "metadata", true, "Name of metadata file in Properties format (metadata.properties)." );
		options.addOption( "n", "annotators", true, "Name of annotator list file in text format (annotators.txt)." );
		options.addOption( "s", "story", true, "Name of story file in text format (story.txt)." );
		options.addOption( "h", "help", false, "print usage" );
		CommandLine cmd = parser.parse( options, args);
		if (cmd.hasOption("help") || cmd.getOptions().length == 0 || !cmd.hasOption("action")) {
			HelpFormatter formatter = new HelpFormatter();
		    formatter.printHelp("oopcorenlp", options);
		    return;
		}
		if (cmd.getOptionValue("action").equals("generate")) {
			App app = new App();
			String outputPath = cmd.hasOption("outputPath")?cmd.getOptionValue("outputPath"):".";
			logger.debug(outputPath);
			app.generateTemplates(outputPath);
		    return;
		}
		if (cmd.getOptionValue("action").equals("analyze")) {
			App app = new App();
			String outputPath = cmd.hasOption("outputPath")?cmd.getOptionValue("outputPath"):".";
			String inputPath = cmd.hasOption("inputPath")?cmd.getOptionValue("inputPath"):".";
			String parameterStore = cmd.hasOption("parameterStore")?cmd.getOptionValue("parameterStore"):"parameterStore.properties";
			String metadata = cmd.hasOption("metadata")?cmd.getOptionValue("metadata"):"metadata.properties";
			String annotators = cmd.hasOption("annotators")?cmd.getOptionValue("annotators"):"annotators.txt";
			String story = cmd.hasOption("story")?cmd.getOptionValue("story"):"story.txt";

			app.runAnalyzer(
					app.loadParameterStore(inputPath, parameterStore),
					app.readFileToList(inputPath, annotators),
					app.loadProperties(inputPath, metadata),
					app.readFile(inputPath, story),
					outputPath
			);
		    return;
		}
		HelpFormatter formatter = new HelpFormatter();
	    formatter.printHelp("oopcorenlp", options);
 		
	}
	
	public void generateTemplates(String outputPath) throws IOException {
		logger.debug(IOUtils.toString(
				this.getClass().getClassLoader().getResourceAsStream("io/outofprintmagazine/util/oopcorenlp.properties"),
	    		"utf-8"
		));
		writeFile(outputPath, "parameterStore.properties", getClass().getClassLoader().getResourceAsStream("io/outofprintmagazine/util/oopcorenlp.properties"));
		writeFile(outputPath, "metadata.properties", getClass().getClassLoader().getResourceAsStream("io/outofprintmagazine/util/metadata.properties"));
		writeFile(outputPath, "annotators.txt", getClass().getClassLoader().getResourceAsStream("io/outofprintmagazine/util/annotators.txt"));
		writeFile(outputPath, "story.txt", getClass().getClassLoader().getResourceAsStream("io/outofprintmagazine/util/story.txt"));
	}
	
	public void runAnalyzer(
			ParameterStore parameterStore,
			List<String> customAnnotators,
			Properties metadata,
			String text,
			String outputPath
			) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		String cleanText = processPlainUnicode(text);
		generateDocID(cleanText, metadata);
		writeOutput(
				outputPath, 
				metadata, 
				cleanText, 
				new Analyzer(
						parameterStore, 
						customAnnotators
				).analyze(
						metadata, 
						cleanText
				)
		);
	}
	
	private String processPlainUnicode(String input) throws IOException {
		Pattern startLine = Pattern.compile("^\\u2018");
		Pattern startWord = Pattern.compile("\\s\\u2018(\\S)");
		Pattern endLine = Pattern.compile("\\u2019$");
		Pattern endWord = Pattern.compile("(\\S)\\u2019\\s");
		Pattern endSentence = Pattern.compile("\\u2019(\\.)");
		StringBuffer output = new StringBuffer();
		for (String line : IOUtils.readLines(new StringReader(input))) {
			line = line.replaceAll("[\\u00A0\\u2007\\u202F]+", " ").trim();
			line = line.replaceAll("[\\u2028]", "/n").trim();
			if (line.length() > 0) {
				line = startLine.matcher(line).replaceAll("\"");
				line = startWord.matcher(line).replaceAll(" \"$1");
				line = endLine.matcher(line).replaceAll("\"");
				line = endWord.matcher(line).replaceAll("$1\" ");
				line = endSentence.matcher(line).replaceAll("\"$1");
				line = StringUtils.toAscii(StringUtils.normalize(line));
				output.append(line);
				output.append('\n');
				output.append('\n');
			}
		}
		return output.toString();

	}
	
	private void generateDocID(String doc, Properties metadata) {
		metadata.setProperty(
				CoreAnnotations.DocIDAnnotation.class.getSimpleName(), 
				DigestUtils.md5Hex(doc)
		);
	}
	
	private void writeOutput(String outputPath, Properties metadata, String document, Map<String,ObjectNode> json) throws IOException {
		String docID = metadata.getProperty(CoreAnnotations.DocIDAnnotation.class.getSimpleName());
		writeFile(outputPath, "TXT_" + docID + ".txt", document); 
		writeFile(outputPath, "STANFORD_" + docID + ".json", json.get("STANFORD"));
		writeFile(outputPath, "OOP_" + docID + ".json", json.get("OOP"));
		writeFile(outputPath, "PIPELINE_" + docID + ".json", json.get("PIPELINE"));
	}
		
	protected ParameterStore loadParameterStore(String path, String fileName) throws IOException {
		return new ParameterStorePropertiesFile(path, fileName);
	}
	
	protected Properties loadProperties(String path, String fileName) throws IOException {
    	InputStream input = null;
    	Properties props = new Properties();
    	try {
    		input = new FileInputStream(String.format("%s/%s", path, fileName));
    		props.load(input);
    		return props;
    	}
    	finally {
    		if (input != null) {
    			input.close();
    		}
    		input = null;
    	}
	}
	
	private void writeFile(String path, String fileName, String content) throws IOException {
		FileOutputStream fout = null;
		try {
			File f = new File(path + System.getProperty("file.separator", "/") + fileName);
			if (!f.getParentFile().exists()) {
				f.getParentFile().mkdirs();
			}
			fout = new FileOutputStream(f);
			fout.write(content.getBytes());
			fout.flush();
		}
		finally {
			if (fout != null) {
				fout.close();
				fout = null;
			}
		}
	}
	
	private void writeFile(String path, String fileName, JsonNode content) throws IOException {
		File f = new File(path + System.getProperty("file.separator", "/") + fileName);
		ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
		mapper.configure(com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
		mapper.writeValue(f, content);
	}
	
	private void writeFile(String path, String fileName, InputStream in) throws IOException {
		File f = new File(path + System.getProperty("file.separator", "/") + fileName);
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(f);
			IOUtils.copy(in,fout);
		}
		finally {
			in.close();
			if (fout != null) {
				fout.flush();
				fout.close();
			}
		}
	}
	
	private String readFile(String path, String fileName) throws IOException {
		File f = new File(path + System.getProperty("file.separator", "/") + fileName);
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(f);
			return IOUtils.toString(
	    		fin,
	    		"utf-8"
			);
		}
		finally {
			if (fin != null) {
				fin.close();
				fin = null;
			}
		}		
	}
	
	private List<String> readFileToList(String path, String fileName) throws IOException {
		List<String> allLines = FileUtils.readLines(
					new File(
							path + System.getProperty("file.separator", "/") + fileName
					),
					"utf-8"
		);
		List<String> retval = new ArrayList<String>();
		for (String line : allLines) {
			if (!line.startsWith("#") && !line.startsWith("/")) {
				retval.add(line);
			}
		}
		return retval;
	}
}