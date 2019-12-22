package io.outofprintmagazine.util.resource;

import java.util.ArrayList;

public class OOPStory {

	public OOPStory() {
		super();
	}
	
	private String author = new String();
	private String title = new String();
	private String body = new String();
	private String bio = new String();
	private ArrayList<String> credits;
	private String url = new String();
	private int fileSize = 0;

	
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getBio() {
		return bio;
	}
	public void setBio(String bio) {
		this.bio = bio;
	}
	public ArrayList<String> getCredits() {
		return credits;
	}
	public void setCredits(ArrayList<String> credits) {
		this.credits = credits;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String toString() {
		StringBuffer retval = new StringBuffer();
		retval.append("url: " + url + '\n');
		retval.append("title: " + title + '\n');
		retval.append("author: " + author + '\n');
		retval.append("bio: " + bio + '\n');
		return retval.toString();
	}

}
