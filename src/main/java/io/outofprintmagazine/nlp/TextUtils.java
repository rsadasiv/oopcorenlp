package io.outofprintmagazine.nlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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

import edu.stanford.nlp.util.StringUtils;
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
}
