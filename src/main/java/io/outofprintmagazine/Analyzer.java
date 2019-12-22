package io.outofprintmagazine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.JSONOutputter;
import io.outofprintmagazine.nlp.CoreNlpUtils;
import io.outofprintmagazine.nlp.TextUtils;
import io.outofprintmagazine.nlp.pipeline.scorers.CorpusScorer;
import io.outofprintmagazine.nlp.pipeline.serializers.CoreNLPSerializer;
import io.outofprintmagazine.util.CorpusDocumentAggregateScore;
import io.outofprintmagazine.util.CorpusScalarAggregateScore;
import io.outofprintmagazine.util.resource.OOPIssue;
import io.outofprintmagazine.util.resource.OOPStory;

public class Analyzer {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(Analyzer.class);
	
	static enum OutputFormat {
		STANFORD,
		OOP
	}
	
	public Analyzer() {
		// TODO Auto-generated constructor stub
	}

	public static void test(String[] args) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		String text = "The quick black fox jumped over the lazy black dog.";
	    //text = "I looked at the beige walls. John Smith grew up in Hawaii. Mr. John Smith is our president. He is wearing a green tie. Obama's shrinking hairline receeded each year of his presidency. When John Smith left office, his hair was completely gray. John Smith ran for office. The walls were painted yellow. Obama beat John Smith until he was black and blue. The conflict between Obama and John Smith quickly erupted into violence.";
		//text = "Naomi Plumb looked at the ribbed kettle in her hands and felt cross.";
		//text = "She walked over to the window and reflected on her urban surroundings.\n\n She had always loved creepy Exeter with its foolish, fluffy fields. It was a place that encouraged her tendency to feel cross with albraday - she would have to tweet about it";
		//text = "Naomi Plumb looked at the ribbed kettle in her hands and felt cross; Uma shook her long, black, hair.\n\nShe had always loved Exeter with its gentle river and green fields. It was a place that made her feel peaceful. Naomi Plumb said \"boo\" to John Smith. Bah! It was a perfectly cromulent day. We will clobber that team on Sunday. ads dsaf sdalkfj dsalkfj.";
		text = "Hmm, thought Babu as he pocketed the receipt while walking out of the Western Union branch at Sabayeen, Jeddah. Sans a passport, this was a monthly ritual for Babu, it was on the identity of Shukran Jaffer that the 1000 Saudi Riyals got transferred to his wife. Beg his friends to help send the money home, money that helped his wife keep the house going, money that he scrubbed floors, washed community toilets and ran personal errands to earn and save. Money his friends gave him for servitude. He has never been counted in his family back home; at least here he was the last among equals.";
		SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss.S");
		Analyzer ta = new Analyzer();
		System.out.println(fmt.format(new java.util.Date(System.currentTimeMillis())) + " finished ta init");

		System.out.println(fmt.format(new java.util.Date(System.currentTimeMillis())) + " started annotation");
		//CoreDocument document = ta.annotate(text);
		System.out.println(fmt.format(new java.util.Date(System.currentTimeMillis())) + " finished annotation");
		
		//ta.score(document)
		ObjectNode json = ta.analyze(text, new Properties()).get("OOP");
		ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
		mapper.writeValue(new File("output.json"), json);
		System.out.println(fmt.format(new java.util.Date(System.currentTimeMillis())) + " finished analysis");

	}
	
//	Input
//	File
//	S3
//Output
//	File
//	S3
//ControlMessages
//	File
//	Sqs
//
	
	public static void inputFile_main(String[] args) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		Analyzer ta = new Analyzer();
		File inputFile = new File("data/input.tsv");
	    BufferedReader br = new BufferedReader(new FileReader(inputFile));
	    String sCurrentLine;
        while ((sCurrentLine = br.readLine()) != null) {
        	String[] columns = sCurrentLine.split("\\t");
        	if (columns.length == 2) {
        		logger.info(columns[0]);
        		ta.processControlMessage(new File(columns[0]), new File(columns[1]));
        	}
        }
        br.close();
	}
	
	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, ParseException {
		Analyzer ta = new Analyzer();
		//ta.test(null);
		//ta.processDirectory("C:\\Users\\rsada\\git\\oop_nlp\\similarity\\resources\\Submissions", "C:\\Users\\rsada\\git\\oop_nlp\\similarity\\resources\\Submissions\\Annotations");
//		ta.processIssues("C:\\Users\\rsada\\eclipse-workspace\\oopcorenlp_web\\WebContent\\Corpora\\Published");
		ta.scoreCorpus("C:\\Users\\rsada\\eclipse-workspace\\oopcorenlp_web\\WebContent\\Corpora\\Published");
//		Properties metadata = new Properties();
//		metadata.put(CoreAnnotations.DocIDAnnotation.class.getName(), "d659265c-641f-41b9-a5b2-5db65c308640");
//		metadata.put(CoreAnnotations.DocTypeAnnotation.class.getName(), "Temp");
//		metadata.put(CoreAnnotations.AuthorAnnotation.class.getName(), "Sashikanta Mishra");
//		metadata.put(CoreAnnotations.DocDateAnnotation.class.getName(), "march_2018");
//		metadata.put(CoreAnnotations.DocTitleAnnotation.class.getName(), "The DNA Test");
//		metadata.put(CoreAnnotations.DocSourceTypeAnnotation.class.getName(), "ISSUE 30 MARCH 2018");
//		ta.processFile(
//				"C:\\Users\\rsada\\eclipse-workspace\\oopcorenlp_web\\WebContent\\Corpora\\Temp", 
//				"d659265c-641f-41b9-a5b2-5db65c308640",
//				metadata
//		);
	}
	
	public void processFile(String corpusDirectory, String inputFileName, Properties metadata) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		Map<String,ObjectNode> json = analyze(
			IOUtils.slurpFile(
				corpusDirectory + 
				System.getProperty("file.separator", "/") 
				+ "Text" 
				+ System.getProperty("file.separator", "/") 
				+ inputFileName 
				+ ".txt"
			), 
			metadata
		);
		writeFile(corpusDirectory + System.getProperty("file.separator", "/") + "Annotations" + System.getProperty("file.separator", "/") + "STANFORD" + System.getProperty("file.separator", "/") + inputFileName +".json", json.get("STANFORD"));
		writeFile(corpusDirectory + System.getProperty("file.separator", "/") + "Annotations" + System.getProperty("file.separator", "/") + "OOP" + System.getProperty("file.separator", "/") + inputFileName +".json", json.get("OOP"));
		writeFile(corpusDirectory + System.getProperty("file.separator", "/") + "Annotations" + System.getProperty("file.separator", "/") + "PIPELINE" + System.getProperty("file.separator", "/") + inputFileName +".json", json.get("PIPELINE"));			
	}
	
	public void processIssues(String outputDirectory) throws IOException, ParseException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		List<OOPIssue> allIssues = TextUtils.getInstance().extractAllIssues();
		for (OOPIssue issue : allIssues) {
			for (OOPStory story : issue.getStories()) {
				String storyId = java.util.UUID.randomUUID().toString();
				File storyFile = new File(outputDirectory + System.getProperty("file.separator", "/") + "Text" + System.getProperty("file.separator", "/") + storyId + ".txt");
				storyFile.createNewFile();
				PrintWriter fout = new PrintWriter(storyFile);
				String text = TextUtils.getInstance().processPlainUnicode(story.getBody());
				fout.print(text);
				fout.close();
				Properties metadata = new Properties();
    			metadata.put(CoreAnnotations.DocIDAnnotation.class.getName(), storyId);
    			metadata.put(CoreAnnotations.DocTypeAnnotation.class.getName(), "Published");
    			metadata.put(CoreAnnotations.AuthorAnnotation.class.getName(), story.getAuthor());
    			metadata.put(CoreAnnotations.DocDateAnnotation.class.getName(), issue.getIssueDate());
    			metadata.put(CoreAnnotations.DocTitleAnnotation.class.getName(), story.getTitle());
    			metadata.put(CoreAnnotations.DocSourceTypeAnnotation.class.getName(), issue.getIssueNumber());
    			logger.info("Analyzing: " + story.getTitle());
    			Map<String,ObjectNode> json = analyze(text, metadata);
    			writeFile(outputDirectory + System.getProperty("file.separator", "/") + "Annotations" + System.getProperty("file.separator", "/") + "STANFORD" + System.getProperty("file.separator", "/") + storyId +".json", json.get("STANFORD"));
    			writeFile(outputDirectory + System.getProperty("file.separator", "/") + "Annotations" + System.getProperty("file.separator", "/") + "OOP" + System.getProperty("file.separator", "/") + storyId +".json", json.get("OOP"));
    			writeFile(outputDirectory + System.getProperty("file.separator", "/") + "Annotations" + System.getProperty("file.separator", "/") + "PIPELINE" + System.getProperty("file.separator", "/") + storyId +".json", json.get("PIPELINE"));			
			}
		}
	}
	
	
	public void processDirectory(String inputDirectory, String outputDirectory) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		File inputDir = new File(inputDirectory);
		File outputDir = new File(outputDirectory);
		if (inputDir.isDirectory() && outputDir.isDirectory()) {
			for (final File inputFile : inputDir.listFiles()) {
				if (inputFile.getName().startsWith("14") && inputFile.getName().endsWith(".txt")) {
					logger.info(inputFile.getName());
					processControlMessage(inputFile, outputDir);
				}
			}
		}
	}

	public void processControlMessage(File inputFile, File outputDirectory) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		Properties metadata = new Properties();
		String text = loadEmailSubmission(inputFile.getPath(),metadata);
		Map<String,ObjectNode> json = analyze(text, metadata);
		writeFile(outputDirectory.getPath() + System.getProperty("file.separator", "/") + "STANFORD" + System.getProperty("file.separator", "/") + inputFile.getName().substring(0, inputFile.getName().length()-".txt".length())+".json", json.get("STANFORD"));
		writeFile(outputDirectory.getPath() + System.getProperty("file.separator", "/") + "OOP" + System.getProperty("file.separator", "/") + inputFile.getName().substring(0, inputFile.getName().length()-".txt".length())+".json", json.get("OOP"));
		writeFile(outputDirectory.getPath() + System.getProperty("file.separator", "/") + "PIPELINE" + System.getProperty("file.separator", "/") + inputFile.getName().substring(0, inputFile.getName().length()-".txt".length())+".json", json.get("PIPELINE"));

	}
	
	public String loadEmailSubmission(String fileName, Properties metadata) throws IOException {
		File inputFile = new File(fileName);
		metadata.put(CoreAnnotations.DocIDAnnotation.class.getName(), inputFile.getName());
		metadata.put(CoreAnnotations.DocTypeAnnotation.class.getName(), "Submissions");
	    BufferedReader br = new BufferedReader(new FileReader(fileName));
	    StringBuilder contentBuilder = new StringBuilder();
	    String sCurrentLine;
	    boolean inHeader = true;
        while ((sCurrentLine = br.readLine()) != null) {
        	if (sCurrentLine.trim().length() == 0) {
        		inHeader = false;
        	}
        	if (inHeader) {
        		int firstColonPosition = sCurrentLine.indexOf(":");
        		if (firstColonPosition > -1) {
        			String fieldName = sCurrentLine.substring(0, firstColonPosition);
        			String fieldValue = sCurrentLine.substring(firstColonPosition+1);
        			if (fieldName.equalsIgnoreCase("From")) {
        				metadata.put(CoreAnnotations.AuthorAnnotation.class.getName(), fieldValue);
        			}
        			else if (fieldName.equalsIgnoreCase("Date")) {
        				metadata.put(CoreAnnotations.DocDateAnnotation.class.getName(), fieldValue);
        			}
        			else if (fieldName.equalsIgnoreCase("Subject")) {
        				metadata.put(CoreAnnotations.DocTitleAnnotation.class.getName(), fieldValue);
        			}
        			else if (fieldName.equalsIgnoreCase("Title")) {
        				metadata.put(CoreAnnotations.DocTitleAnnotation.class.getName(), fieldValue);
        			}
        			else if (fieldName.equalsIgnoreCase("To")) {
        				metadata.put(CoreAnnotations.DocSourceTypeAnnotation.class.getName(), fieldValue);
        			}
        		
        		
        		}
        		else {
        			inHeader = false;
        			contentBuilder.append(sCurrentLine).append("\n");
        		}
        	}
        	else {
        		contentBuilder.append(sCurrentLine).append("\n");
        	}
        }
        br.close();
        return TextUtils.getInstance().processPlainUnicode(contentBuilder.toString());
	}
	
	public void writeFile(String fileName, ObjectNode output) throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
		mapper.configure(com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
		mapper.writeValue(new File(fileName), output);		
	}
	
	public ObjectNode analyze(String text, Properties props, String outputFormat) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		return analyze(text, props).get(outputFormat);
	}
	
	public Map<String, ObjectNode> analyze(String text, Properties metadata) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		long startTime = System.currentTimeMillis();
		CoreDocument document = new CoreDocument(text);
		document.annotation().set(
				CoreAnnotations.DocIDAnnotation.class, 
				metadata.getProperty(
						CoreAnnotations.DocIDAnnotation.class.getName(), 
						"LittleNaomi"
						)
				);
		document.annotation().set(
				CoreAnnotations.DocTitleAnnotation.class, 
				metadata.getProperty(
						CoreAnnotations.DocTitleAnnotation.class.getName(), 
						"story title"
						)
				);
		document.annotation().set(
				CoreAnnotations.DocTypeAnnotation.class, 
				metadata.getProperty(
						CoreAnnotations.DocTypeAnnotation.class.getName(), 
						"Submissions"
						)
				);
		document.annotation().set(
				CoreAnnotations.DocSourceTypeAnnotation.class, 
				metadata.getProperty(
						CoreAnnotations.DocSourceTypeAnnotation.class.getName(), 
						"outofprintmagazine@gmail.com"
						)
				);		
		document.annotation().set(
				CoreAnnotations.AuthorAnnotation.class, 
				metadata.getProperty(
						CoreAnnotations.AuthorAnnotation.class.getName(), 
						"Author"
						)
				);
		document.annotation().set(
				CoreAnnotations.DocDateAnnotation.class, 
				metadata.getProperty(
						CoreAnnotations.DocDateAnnotation.class.getName(), 
						"9 January 2014 21:05"
						)
				);

		Map<String,ObjectNode> retval = new HashMap<String, ObjectNode>();
		
		try {
			CoreNlpUtils.getInstance().getPipeline().annotate(document);
			//logger.debug(JSONOutputter.jsonPrint(document.annotation()));
		
			retval.put("STANFORD", (ObjectNode) mapper.readTree(JSONOutputter.jsonPrint(document.annotation())));
			ObjectNode json = mapper.createObjectNode();
			CoreNLPSerializer documentSerializer = new CoreNLPSerializer();
			documentSerializer.serialize(document, json);
			CoreNlpUtils.getInstance().serialize(document, json);
			retval.put("OOP", json);
			JavaPropsMapper propsMapper = new JavaPropsMapper();
			ObjectNode properties = propsMapper.valueToTree(CoreNlpUtils.getDefaultProps());
			CoreNlpUtils.getInstance().describeAnnotations(properties);
			ArrayNode analysisList = properties.putArray("analysis");
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
			analysisList.addObject().put("startTime", fmt.format(new java.util.Date(startTime)));
			long endTime = System.currentTimeMillis();
			analysisList.addObject().put("endTime", fmt.format(new java.util.Date(endTime)));
			analysisList.addObject().put("elapsedMS", Long.toString(endTime-startTime));
			String ipAddr = "localhost.localdomain/127.0.0.1";
			try {
				ipAddr = InetAddress.getLocalHost().toString();
			}
			catch (UnknownHostException e) {
			}
			analysisList.addObject().put("host", ipAddr);
			retval.put("PIPELINE", properties);
		}
		catch (Exception e) {
			logger.error(e);
		}
		return retval;
	}
	
	public void scoreCorpus(String corpusDirectory) throws JsonProcessingException, IOException {
		CorpusScorer corpusScorer = new CorpusScorer();
		ObjectMapper mapper = new ObjectMapper();
		File corpusDir = new File(corpusDirectory);
		File inputDir = new File(corpusDir.getPath() + System.getProperty("file.separator", "/") + "Annotations"  + System.getProperty("file.separator", "/") + "OOP" + System.getProperty("file.separator", "/"));
		if (inputDir.isDirectory()) {
			for (final File inputFile : inputDir.listFiles()) {
				logger.debug(inputFile.getPath());
				try {
					ObjectNode documentScore = (ObjectNode) mapper.readTree(inputFile);
					corpusScorer.addDocumentJson(documentScore);
				}
				catch (Exception e) {
					logger.error(e);
				}
			}
		}
		for (CorpusDocumentAggregateScore annotationScore : corpusScorer.scoreCorpus()) {
			writeFile(
				corpusDir.getPath()
					+ System.getProperty("file.separator", "/") 
					+ "Annotations" 
					+ System.getProperty("file.separator", "/")
					+ "CORPUS" 
					+ System.getProperty("file.separator", "/") 
					+ annotationScore.getName()
					+ ".json",
				mapper.valueToTree(annotationScore)
			);
		}
		for (CorpusScalarAggregateScore annotationScore : corpusScorer.scoreScalarCorpus()) {
			writeFile(
				corpusDir.getPath()
					+ System.getProperty("file.separator", "/") 
					+ "Annotations" 
					+ System.getProperty("file.separator", "/")
					+ "CORPUS" 
					+ System.getProperty("file.separator", "/") 
					+ annotationScore.getName()
					+ ".json",
				mapper.valueToTree(annotationScore)
			);
		}
	}
}
