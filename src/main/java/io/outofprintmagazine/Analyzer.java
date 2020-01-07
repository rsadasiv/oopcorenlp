package io.outofprintmagazine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;

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
import io.outofprintmagazine.util.InputMessage;

public class Analyzer {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(Analyzer.class);
	
	
	public Analyzer() {
		super();
	}

	private Queue<InputMessage> inputMessages = new LinkedList<InputMessage>();
	
	public Queue<InputMessage> getInputMessages() {
		return inputMessages;
	}

	public void setInputMessages(Queue<InputMessage> inputMessages) {
		this.inputMessages = inputMessages;
	}
	
	public void init() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, ParseException {
		getInputMessages().addAll(
				TextUtils.getInstance().readEmailDirectory(
						"C:\\Users\\rsada\\git\\oop_nlp\\similarity\\resources\\Submissions", 
						"C:\\Users\\rsada\\eclipse-workspace\\oopcorenlp_web\\WebContent\\Corpora"
				)
		);
		
		getInputMessages().addAll(
				TextUtils.getInstance().readOutOfPrintIssues(
						"C:\\Users\\rsada\\eclipse-workspace\\oopcorenlp_web\\WebContent\\Corpora"
				)
		);

//		Properties metadata = new Properties();
//		metadata.put(CoreAnnotations.DocIDAnnotation.class.getName(), "d659265c-641f-41b9-a5b2-5db65c308640");
//		metadata.put(CoreAnnotations.DocTypeAnnotation.class.getName(), "Temp");
//		metadata.put(CoreAnnotations.AuthorAnnotation.class.getName(), "Sashikanta Mishra");
//		metadata.put(CoreAnnotations.DocDateAnnotation.class.getName(), "march_2018");
//		metadata.put(CoreAnnotations.DocTitleAnnotation.class.getName(), "The DNA Test");
//		metadata.put(CoreAnnotations.DocSourceTypeAnnotation.class.getName(), "ISSUE 30 MARCH 2018");
//		getInputMessages().add(
//				new InputMessage(
//						"C:\\Users\\rsada\\eclipse-workspace\\oopcorenlp_web\\WebContent\\Corpora", 
//						metadata,
//						IOUtils.slurpFile(
//								"C:\\Users\\rsada\\eclipse-workspace\\oopcorenlp_web\\WebContent\\Corpora"
//								+ System.getProperty("file.separator", "/") 
//								+ metadata.getProperty(CoreAnnotations.DocTypeAnnotation.class.getName()) 
//								+ System.getProperty("file.separator", "/") 
//								+ "Text" 
//								+ System.getProperty("file.separator", "/") 
//								+ metadata.getProperty(CoreAnnotations.DocIDAnnotation.class.getName())
//								+ ".txt"
//						)
//				)
//		);
	}

	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, ParseException {
		Analyzer ta = new Analyzer();
		ta.init();
		InputMessage lastMessage = null;
		while (!ta.getInputMessages().isEmpty()) {
			lastMessage = ta.getInputMessages().poll();
			ta.processInputMessage(lastMessage);
		}
		if (lastMessage != null) {
			ta.scoreCorpus(lastMessage.getOutputDirectory());
		}
		
	}
		
	public void processInputMessage(InputMessage inputMessage) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		String storyText = TextUtils.getInstance().processPlainUnicode(inputMessage.getInputFile());
		Map<String,ObjectNode> json = analyze(inputMessage.getMetadata(), storyText);
		
		//write properties
		inputMessage.getMetadata().store(
			new FileOutputStream(
					inputMessage.getOutputDirectory()
					+ System.getProperty("file.separator", "/") 
					+ inputMessage.getMetadata().getProperty(CoreAnnotations.DocTypeAnnotation.class.getName()) 
					+ System.getProperty("file.separator", "/") 
					+ "Text" 
					+ System.getProperty("file.separator", "/") 
					+ inputMessage.getMetadata().getProperty(CoreAnnotations.DocIDAnnotation.class.getName()) 
					+".properties"
			), 
			null
		);
		//write txt
		IOUtils.writeStringToFile(
				storyText, 
				inputMessage.getOutputDirectory()
				+ System.getProperty("file.separator", "/") 
				+ inputMessage.getMetadata().getProperty(CoreAnnotations.DocTypeAnnotation.class.getName()) 
				+ System.getProperty("file.separator", "/") 
				+ "Text" 
				+ System.getProperty("file.separator", "/") 
				+ inputMessage.getMetadata().getProperty(CoreAnnotations.DocIDAnnotation.class.getName()) 
				+".txt", 
				"US-ASCII"
		);
		
		//write stanford
		writeFile(
				inputMessage.getOutputDirectory()
				+ System.getProperty("file.separator", "/") 
				+ inputMessage.getMetadata().getProperty(CoreAnnotations.DocTypeAnnotation.class.getName()) 
				+ System.getProperty("file.separator", "/") 
				+ "Annotations" 
				+ System.getProperty("file.separator", "/") 
				+ "STANFORD" 
				+ System.getProperty("file.separator", "/") 
				+ inputMessage.getMetadata().getProperty(CoreAnnotations.DocIDAnnotation.class.getName()) 
				+".json", 
			json.get("STANFORD")
		);
		
		//write oop
		writeFile(
				inputMessage.getOutputDirectory() 
				+ System.getProperty("file.separator", "/") 
				+ inputMessage.getMetadata().getProperty(CoreAnnotations.DocTypeAnnotation.class.getName()) 
				+ System.getProperty("file.separator", "/") 
				+ "Annotations" 
				+ System.getProperty("file.separator", "/") 
				+ "OOP" 
				+ System.getProperty("file.separator", "/") 
				+ inputMessage.getMetadata().getProperty(CoreAnnotations.DocIDAnnotation.class.getName()) 
				+".json", 
			json.get("OOP")
		);
		
		//write pipeline
		writeFile(
				inputMessage.getOutputDirectory()
				+ System.getProperty("file.separator", "/") 
				+ inputMessage.getMetadata().getProperty(CoreAnnotations.DocTypeAnnotation.class.getName()) 
				+ System.getProperty("file.separator", "/") 
				+ "Annotations" 
				+ System.getProperty("file.separator", "/") 
				+ "PIPELINE" 
				+ System.getProperty("file.separator", "/") 
				+ inputMessage.getMetadata().getProperty(CoreAnnotations.DocIDAnnotation.class.getName()) 
				+".json", 
			json.get("PIPELINE")
		);
	}
	
	
	public void writeFile(String fileName, ObjectNode output) throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
		mapper.configure(com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
		mapper.writeValue(new File(fileName), output);		
	}
		
	public Map<String, ObjectNode> analyze(Properties metadata, String text) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
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
