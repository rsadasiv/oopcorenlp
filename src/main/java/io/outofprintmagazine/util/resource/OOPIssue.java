package io.outofprintmagazine.util.resource;
import java.text.Format;
import java.text.ParseException;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;

public class OOPIssue {
	
	public OOPIssue() {
		super();
	}

	private String url;
	private String issueDate;
	private String issueNumber;
	private String coverArtUrl;
	private String editorsNoteUrl;
	private ArrayList<String> credits = new ArrayList<String>();
	private ArrayList<OOPStory> stories = new ArrayList<OOPStory>();
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getHrefBase() {
		return getUrl().substring(0, getUrl().length()-"index.html".length());
	}

	
	public String getIssueDate() {
		return issueDate;
	}
	
	public void setIssueDate(String issueDate) {
		this.issueDate = issueDate;
	}
	
	public String getIssueNumber() {
		return issueNumber;
	}
	public void setIssueNumber(String issueNumber) {
		this.issueNumber = issueNumber;
	}
	
//	public void setIssueDate(String issueDate) throws ParseException {
//		//march_2018
//		Format dtf = 
//			    new DateTimeFormatterBuilder().parseCaseInsensitive()
//			                                  .appendPattern("MMM_yyyy")
//			                                  .toFormatter().toFormat(); 
//		this.issueDate = (java.util.Date)dtf.parseObject(issueDate);
//	}
	public String getCoverArtUrl() {
		return coverArtUrl;
	}
	public void setCoverArtUrl(String coverArtUrl) {
		this.coverArtUrl = coverArtUrl;
	}
	public String getEditorsNoteUrl() {
		return editorsNoteUrl;
	}
	public void setEditorsNoteUrl(String editorsNoteUrl) {
		this.editorsNoteUrl = editorsNoteUrl;
	}
	public ArrayList<String> getCredits() {
		return credits;
	}
	public void setCredits(ArrayList<String> credits) {
		this.credits = credits;
	}
	public ArrayList<OOPStory> getStories() {
		return stories;
	}
	public void setStories(ArrayList<OOPStory> stories) {
		this.stories = stories;
	}
	
	public String toString() {
		StringBuffer retval = new StringBuffer();
		retval.append("url: " + url + '\n');
		retval.append("issueDate: " + issueDate + '\n');
		retval.append("issueNumber: " + issueNumber + '\n');
		retval.append("coverArtUrl: " + coverArtUrl + '\n');
		retval.append("editorsNoteUrl: " + editorsNoteUrl + '\n');		
		for (OOPStory story : stories) {
			retval.append("story: " + story.getUrl() + '\n');
		}
		return retval.toString();
		
	}

}
