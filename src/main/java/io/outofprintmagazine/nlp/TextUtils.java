package io.outofprintmagazine.nlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.util.StringUtils;
import io.outofprintmagazine.util.InputMessage;
import io.outofprintmagazine.util.resource.OOPIssue;
import io.outofprintmagazine.util.resource.OOPStory;

public class TextUtils {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(TextUtils.class);
	
	private static TextUtils single_instance = null;
	private Tika tika = null;

    public static TextUtils getInstance() throws IOException { 
        if (single_instance == null) 
            single_instance = new TextUtils(); 
  
        return single_instance; 
    }
	
	private TextUtils() {
		super();
	}
	
	public Tika getTika() {
		if (tika == null) {
			tika = new Tika();
		}
		return tika;
	}

	//need to convert single angle quote to double vertical quote
	//if the single quote starts the line - yes
	// ^\u2018
	//if the single quote ends the line - yes
	// \u2019$
	//if the single quote has printing characters on both sides - no
	//else - yes
	// \s\u2018
	// \u2019\s
	//
	//still having a problem with plural possessive. The Smiths' house.
	
	public String processPlainUnicode(String input) {
		input = Pattern.compile("^\\u2018", Pattern.MULTILINE).matcher(input).replaceAll("\"");
		input = Pattern.compile("\\s\\u2018(\\S)", Pattern.MULTILINE).matcher(input).replaceAll(" \"$1");
		input = Pattern.compile("\\u2019$", Pattern.MULTILINE).matcher(input).replaceAll("\"");
		input = Pattern.compile("(\\S)\\u2019\\s", Pattern.MULTILINE).matcher(input).replaceAll("$1\" ");
		input = Pattern.compile("\\u2019(\\.)", Pattern.MULTILINE).matcher(input).replaceAll("\"$1");
		return StringUtils.toAscii(StringUtils.normalize(input)).trim();
	}
	
	public String fileToPlainUnicode(File file) throws IOException, TikaException {
	    FileInputStream fis = null;
	    try {
	    	fis = new FileInputStream(file);
	    	return getTika().parseToString(fis);
	    }
	    catch (Exception e) {
	    	throw e;
	    }
	    finally {
	    	if (fis != null) {
	    		fis.close();
	    		fis = null;
	    	}
	    }
	}
	
	public String fileToPlainUnicode(File file, Metadata metadata) throws IOException, TikaException, SAXException {
	    FileInputStream fis = null;
	    try {
	    	fis = new FileInputStream(file);
	        AutoDetectParser parser = new AutoDetectParser();
	        BodyContentHandler handler = new BodyContentHandler();
	        parser.parse(fis, handler, metadata);
	        return handler.toString();
	    }
	    catch (Exception e) {
	    	throw e;
	    }
	    finally {
	    	if (fis != null) {
	    		fis.close();
	    		fis = null;
	    	}
	    }
	}
	
	
	public List<OOPIssue> extractAllIssues() throws IOException, ParseException {
		ArrayList<OOPIssue> retval = new ArrayList<OOPIssue>();
		Document doc = Jsoup.connect("http://www.outofprintmagazine.co.in/archives.html").get();
		Elements links = doc.select("a[href^=archive]");
		for (Element link : links) {
			String ref = link.parent().childNode(0).attr("href").trim();
			if (ref.length()>0 && ref.startsWith("archive")) {
				OOPIssue issue = new OOPIssue();
				issue.setUrl("http://www.outofprintmagazine.co.in/" + ref);
				issue.setIssueDate(ref.substring("archive/".length(), ref.length()-"_issue/index.html".length()));
				extractIssueData(issue);
				retval.add(issue);
			}
		}
		return retval;
	}
	
	public void extractIssueData(OOPIssue issue) throws IOException, ParseException {
		if (issue.getIssueDate() == null) {
			issue.setIssueDate(issue.getUrl().substring("http://www.outofprintmagazine.co.in/archive/".length(), issue.getUrl().length()-"_issue/index.html".length()));
		}
		Document homepage = Jsoup.connect(issue.getUrl()).get();
		Elements imgs = homepage.select("img[src^=images/cover_pic]");
		for (Element img : imgs) {
			issue.setCoverArtUrl(img.attr("src").trim());
		}
		Elements issueInfo = homepage.select("div[class=issue]");
		for (Element elem : issueInfo) {
			issue.setIssueNumber(elem.text());
		}
		if ("".equals(issue.getIssueNumber())) {
			issue.setIssueNumber(issue.getIssueDate());
		}
		Elements storylinks = homepage.select("area");
		if (storylinks.isEmpty()) {
			storylinks = homepage.select("a");
		}
		for (Element storylink : storylinks) {
			String storyHref = storylink.attr("href").trim();
			if (!storyHref.startsWith(".") && !storyHref.startsWith("index.html")) {
				if (storyHref.equals("editors-note.html")) {
					issue.setEditorsNoteUrl(storyHref);
				}
				else {
					OOPStory story = new OOPStory();
					story.setUrl(issue.getHrefBase() + storyHref);
					logger.debug(story.getUrl());
					//september_2016-issue, july_2019_issue, march-2013-issue
					//<h5><span>TITLE</span> by AUTHOR <br>description</h5>
					//sept_2017_issue
					//<h5><a><strong><span>TITLE</span> by AUTHOR description</strong></a></h5>
					try {
						Document storytext = Jsoup.connect(story.getUrl()).get();
						Elements titles = storytext.select("h5");
						for (Element title : titles) {
							if (title.childNode(0).nodeName() == "a") {
								Node aNode = title.childNode(0);
								if (aNode.childNodes().size() > 0 && aNode.childNode(0).nodeName() == "strong") {
									Node strongNode = aNode.childNode(0);
									if (strongNode.childNodes().size() > 0 && strongNode.childNode(0).nodeName() == "span") {
										story.setTitle(strongNode.childNode(0).childNode(0).outerHtml());
										story.setAuthor(title.select("strong").get(0).textNodes().get(0).getWholeText().trim().substring("by ".length()));
									}
								}
							}
							if (title.childNode(0).nodeName() == "span") {
								story.setTitle(title.childNode(0).childNode(0).outerHtml());
								if (title.textNodes().get(0).getWholeText().trim().contains("by ")) {
									story.setAuthor(title.textNodes().get(0).getWholeText().trim().substring("by ".length()));
								}
							}
							
						}
						Elements mainText = storytext.select("div#main-text-cont2");
						for (Element body : mainText ) {
							//TODO
							Elements paragraphs = body.select("p");
							for (Element paragraph: paragraphs) {
								if (paragraph.attr("class").startsWith("writersintro")) {
									story.setBio(story.getBio() + '\n' + paragraph.text());
								}
								else {
									story.setBody(story.getBody() + '\n' + paragraph.text());
								}
							}
						}
						issue.getStories().add(story);
					}
					catch (Exception e) {
						logger.error(e);
					}
				}
			}
		}		
		
	}
	public List<InputMessage> readOutOfPrintIssues(String outputDirectory) throws IOException, ParseException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		List<InputMessage> retval = new ArrayList<InputMessage>();
		List<OOPIssue> allIssues = TextUtils.getInstance().extractAllIssues();
		for (OOPIssue issue : allIssues) {
			for (OOPStory story : issue.getStories()) {
				//String storyId = java.util.UUID.randomUUID().toString();
				String storyId = URLEncoder.encode(story.getUrl(), "UTF-8");
				Properties metadata = new Properties();
    			metadata.put(CoreAnnotations.DocIDAnnotation.class.getName(), storyId);
    			metadata.put(CoreAnnotations.DocTypeAnnotation.class.getName(), "Published");
    			metadata.put(CoreAnnotations.AuthorAnnotation.class.getName(), story.getAuthor());
    			metadata.put(CoreAnnotations.DocDateAnnotation.class.getName(), issue.getIssueDate());
    			metadata.put(CoreAnnotations.DocTitleAnnotation.class.getName(), story.getTitle());
    			metadata.put(CoreAnnotations.DocSourceTypeAnnotation.class.getName(), issue.getIssueNumber());
    			retval.add(new InputMessage(outputDirectory, metadata, story.getBody()));
			}
		}
		return retval;
	}
	
	public List<InputMessage> readEmailDirectory(String inputDirectory, String outputDirectory) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		List<InputMessage> retval = new ArrayList<InputMessage>();
		File inputDir = new File(inputDirectory);
		if (inputDir.isDirectory()) {
			for (final File inputFile : inputDir.listFiles()) {
				if (inputFile.getName().startsWith("14") && inputFile.getName().endsWith(".txt")) {
					logger.info(inputFile.getName());
					Properties metadata = new Properties();
				    StringBuilder contentBuilder = new StringBuilder();
					loadEmailSubmission(inputFile.getPath(), metadata, contentBuilder);
					retval.add(new InputMessage(outputDirectory, metadata, contentBuilder.toString()));
				}
			}
		}
		return retval;
	}
	
	public void loadEmailSubmission(String fileName, Properties metadata, StringBuilder contentBuilder) throws IOException {
		File inputFile = new File(fileName);
		metadata.put(CoreAnnotations.DocIDAnnotation.class.getName(), inputFile.getName());
		metadata.put(CoreAnnotations.DocTypeAnnotation.class.getName(), "Submissions");
	    BufferedReader br = new BufferedReader(new FileReader(fileName));

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
	}
	
	/*
	 * http://outofprintmagazine.blogspot.com/feeds/posts/default?max-results=1000
	 * http://angarai.blogspot.com/feeds/posts/default?max-results=1000
	 */
	public List<InputMessage> readRssFeed(String outputDirectory, String url) throws IllegalArgumentException, FeedException, IOException {
		List<InputMessage> retval = new ArrayList<InputMessage>();
		try (CloseableHttpClient client = HttpClients.createMinimal()) {
		  HttpUriRequest request = new HttpGet(url);
		  try (CloseableHttpResponse response = client.execute(request);
		       InputStream stream = response.getEntity().getContent()) {
		    SyndFeedInput input = new SyndFeedInput();
		    SyndFeed feed = input.build(new XmlReader(stream));
		    for (SyndEntry entry : feed.getEntries()) {
				Properties metadata = new Properties();
    			metadata.put(CoreAnnotations.DocIDAnnotation.class.getName(), URLEncoder.encode(entry.getUri(), "UTF-8"));
    			metadata.put(CoreAnnotations.DocTypeAnnotation.class.getName(), "Blog");
    			metadata.put(CoreAnnotations.AuthorAnnotation.class.getName(), entry.getAuthor());
    			metadata.put(CoreAnnotations.DocDateAnnotation.class.getName(), entry.getPublishedDate());
    			metadata.put(CoreAnnotations.DocTitleAnnotation.class.getName(), entry.getTitle());
    			metadata.put(CoreAnnotations.DocSourceTypeAnnotation.class.getName(), new URL(url).getHost());

		    	StringBuffer buf = new StringBuffer();
		    	for (SyndContent content : entry.getContents()) {
		    		//System.out.println(content.getValue());
		    		Document doc = Jsoup.parse(content.getValue());
		    		Elements paragraphs = doc.select("div.MsoNormal");
					for (Element paragraph: paragraphs) {
						buf.append(paragraph.text());
						buf.append('\n');
						buf.append('\n');
					}
		    	}
		    	retval.add(new InputMessage(outputDirectory, metadata, buf.toString()));
		    }
		  }
		}
		return retval;
	}
	
	/*
	 * https://en.wikipedia.org/wiki/List_of_authors_by_name:_A
	 */
	public List<InputMessage> readWikipediaList(String outputDirectory, String listPage) throws IOException  {
		List<InputMessage> retval = new ArrayList<InputMessage>();
		for (String topic : WikipediaUtils.getInstance().getWikipediaPagesForList(listPage)) {
			Properties metadata = new Properties();
			metadata.put(CoreAnnotations.DocIDAnnotation.class.getName(), URLEncoder.encode("https://en.wikipedia.org/wiki/" + topic, "UTF-8"));
			metadata.put(CoreAnnotations.DocTypeAnnotation.class.getName(), "Wikipedia");
			metadata.put(CoreAnnotations.AuthorAnnotation.class.getName(), "wikipedians");
			metadata.put(CoreAnnotations.DocDateAnnotation.class.getName(), new Date());
			metadata.put(CoreAnnotations.DocTitleAnnotation.class.getName(), topic);
			metadata.put(CoreAnnotations.DocSourceTypeAnnotation.class.getName(), "Wikipedia");
			retval.add(new InputMessage(outputDirectory, metadata, WikipediaUtils.getInstance().getWikipediaPageText(topic)));
		}		
		return retval;
	}
	
	/*
	 * business
	 * entertainment
	 * india
	 * lifestyle
	 * technology
	 */
	public List<InputMessage> readDnaSection(String outputDirectory, String sectionName) throws IOException {
		List<InputMessage> retval = new ArrayList<InputMessage>();
		for (int i=1;i<11;i++) {
			Document doc = Jsoup.connect("https://www.dnaindia.com/" + sectionName + "?page="+i).get();
			Elements links = doc.select("div.mrebolynwsrgtbx > div.bolyveralign > h3 > a");
			for (Element element : links) {
				Properties metadata = new Properties();
    			metadata.put(CoreAnnotations.DocIDAnnotation.class.getName(), URLEncoder.encode(element.attr("href"), "UTF-8"));
    			metadata.put(CoreAnnotations.DocTypeAnnotation.class.getName(), sectionName);
    			metadata.put(CoreAnnotations.AuthorAnnotation.class.getName(), "dnaIndia");
    			metadata.put(CoreAnnotations.DocDateAnnotation.class.getName(), new Date().toString());
    			metadata.put(CoreAnnotations.DocTitleAnnotation.class.getName(), element.attr("href").substring(element.attr("href").lastIndexOf('/'), element.attr("href").length()));
    			metadata.put(CoreAnnotations.DocSourceTypeAnnotation.class.getName(), "www.dnaindia.com");
				Document article = Jsoup.connect("https://www.dnaindia.com"+element.attr("href")).get();
				Elements articleboxes = article.select("div.articllftpbx");
		    	StringBuffer buf = new StringBuffer();
				for (Element articlebox : articleboxes) {
					Elements paras = articlebox.select("p");
					for (Element para : paras) {
						buf.append(para.wholeText());
						buf.append('\n');
						buf.append('\n');
					}
				}
		    	retval.add(new InputMessage(outputDirectory, metadata, buf.toString()));
			}
		}
		return retval;
	}
	
	/*
	 * death
	 * environment
	 * literature
	 * marriage
	 * mental-illness
	 * violence
	 */
	public List<InputMessage> readDnaTopic(String outputDirectory, String topicName) throws IOException {
		List<InputMessage> retval = new ArrayList<InputMessage>();
		for (int i=1;i<11;i++) {
			Document doc = Jsoup.connect("https://www.dnaindia.com/topic/" + topicName + "?page="+i).get();
			Elements links = doc.select("div.mrebolynwsrgtbx > div > span > div.bolyveralign > h3 > a");
			for (Element element : links) {
				Properties metadata = new Properties();
    			metadata.put(CoreAnnotations.DocIDAnnotation.class.getName(), URLEncoder.encode(element.attr("href"), "UTF-8"));
    			metadata.put(CoreAnnotations.DocTypeAnnotation.class.getName(), topicName);
    			metadata.put(CoreAnnotations.AuthorAnnotation.class.getName(), "dnaIndia");
    			metadata.put(CoreAnnotations.DocDateAnnotation.class.getName(), new Date().toString());
    			metadata.put(CoreAnnotations.DocTitleAnnotation.class.getName(), element.attr("href").substring(element.attr("href").lastIndexOf('/'), element.attr("href").length()));
    			metadata.put(CoreAnnotations.DocSourceTypeAnnotation.class.getName(), "www.dnaindia.com");
				Document article = Jsoup.connect("https://www.dnaindia.com"+element.attr("href")).get();
				Elements articleboxes = article.select("div.articllftpbx");
		    	StringBuffer buf = new StringBuffer();
				for (Element articlebox : articleboxes) {
					Elements paras = articlebox.select("p");
					for (Element para : paras) {
						buf.append(para.wholeText());
						buf.append('\n');
						buf.append('\n');
					}
				}
		    	retval.add(new InputMessage(outputDirectory, metadata, buf.toString()));
			}
		}
		return retval;
	}
	
	/*
	 * http://www.gutenberg.org/cache/epub/42671/pg42671.txt
	 * 
	 * Properties metadata = new Properties();
	 * metadata.put(CoreAnnotations.DocIDAnnotation.class.getName(), "42671");
	 * metadata.put(CoreAnnotations.DocTypeAnnotation.class.getName(), "Gutenberg");
	 * metadata.put(CoreAnnotations.AuthorAnnotation.class.getName(), "Jane Austen");
	 * metadata.put(CoreAnnotations.DocDateAnnotation.class.getName(), new Date().toString());
	 * metadata.put(CoreAnnotations.DocTitleAnnotation.class.getName(), "Pride and Prejudice"))
	 * metadata.put(CoreAnnotations.DocSourceTypeAnnotation.class.getName(), "Chapter");
	 */
	public List<InputMessage> readGutenbergChapterBook(String outputDirectory, Properties metadata, String ebookUrl) throws IOException {
		List<InputMessage> retval = new ArrayList<InputMessage>();
		try (CloseableHttpClient client = HttpClients.createMinimal()) {
			  HttpUriRequest request = new HttpGet(ebookUrl);
			  try (CloseableHttpResponse response = client.execute(request);
					  BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
				    String line;
				    int chapterCount = 0;
				    int bookCount = 0;
				    StringWriter fout = null;
				    while ((line = br.readLine()) != null) {
					   if (line.startsWith("BOOK")) {
						   bookCount++;
						   chapterCount = 0;
					   }
				       if (line.startsWith("CHAPTER")) {
				    	   chapterCount++;
				    	   Properties chapterMetadata = (Properties) SerializationUtils.clone(metadata);
				    	   chapterMetadata.put(CoreAnnotations.DocSourceTypeAnnotation.class.getName(), "CHAPTER_"+(new Integer(chapterCount).toString()));
				    	   if (fout != null) {
				    		   fout.flush();
				    		   retval.add(new InputMessage(outputDirectory, chapterMetadata, fout.toString()));
				    	   };
				    	   fout = new StringWriter();   
				    	   fout.write(line);
				    	   fout.write('\n');
				       }
				       else {
				    	   if (fout != null) {
					    	   fout.write(line);
					    	   fout.write('\n');
				    	   }
				       }
				    }
					br.close();

		    	   chapterCount++;
		    	   Properties chapterMetadata = (Properties) SerializationUtils.clone(metadata);
		    	   chapterMetadata.put(CoreAnnotations.DocSourceTypeAnnotation.class.getName(), "CHAPTER_"+(new Integer(chapterCount).toString()));
	    		   fout.flush();
	    		   retval.add(new InputMessage(outputDirectory, chapterMetadata, fout.toString()));

			  }
		}
		return retval;
	}
	
	/*
	 * https://archiveofourown.org/works/21949621
	 * 
	 * Properties metadata = new Properties();
	 * metadata.put(CoreAnnotations.DocIDAnnotation.class.getName(), "21949621");
	 * metadata.put(CoreAnnotations.DocTypeAnnotation.class.getName(), "Fanfic");
	 * metadata.put(CoreAnnotations.AuthorAnnotation.class.getName(), "faithfulhope");
	 * metadata.put(CoreAnnotations.DocDateAnnotation.class.getName(), new Date().toString());
	 * metadata.put(CoreAnnotations.DocTitleAnnotation.class.getName(), "merry christmas, huckleberry finn"))
	 * metadata.put(CoreAnnotations.DocSourceTypeAnnotation.class.getName(), "AO3");
	 */
	
	public List<InputMessage> readAO3(String outputDirectory, Properties metadata, String page) throws IOException  {
		List<InputMessage> retval = new ArrayList<InputMessage>();

		StringWriter buf = new StringWriter();
		Document storyPage = Jsoup.connect(page).get();
		Elements titles = storyPage.select("div#workskin > div.preface.group > h2.title.heading");
		for (Element title : titles) {
			metadata.put(CoreAnnotations.AuthorAnnotation.class.getName(), title.text());
		}
		
		Elements authors = storyPage.select("div#workskin > div.preface.group > h3.byline.heading");
		for (Element author : authors) {
			metadata.put(CoreAnnotations.AuthorAnnotation.class.getName(), author.text());
		}
		
		Elements paras = storyPage.select("div#chapters > div.userstuff > p");
		for (Element para : paras) {
			buf.write(para.wholeText());
			buf.write('\n');
			buf.write('\n');
		}
		retval.add(new InputMessage(outputDirectory, metadata, buf.toString()));
		return retval;

    }
	
	 public static void main(String[] argv) throws IllegalArgumentException, MalformedURLException, FeedException, IOException {
		 //TextUtils.getInstance().readRssFeed("https://www.blogger.com/feeds/5672570534782963438/posts/default?max-results=1000");
	 }
}
