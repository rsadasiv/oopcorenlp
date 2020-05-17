package io.outofprintmagazine.nlp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.SearchParameters;

public class FlickrUtils {
	
	private static final Logger logger = LogManager.getLogger(FlickrUtils.class);
	private Properties props = null;
	private Flickr flickr = null;
	
	private FlickrUtils() throws IOException {
		InputStream input = new FileInputStream("data/flickr_api_key.txt");
		props = new Properties();
        props.load(input);
		flickr = new Flickr(props.getProperty("apiKey"), props.getProperty("secret"), new REST());
	}
	
	private static FlickrUtils single_instance = null; 

    public static FlickrUtils getInstance() throws IOException { 
        if (single_instance == null) 
            single_instance = new FlickrUtils(); 
  
        return single_instance; 
    }
    
    public List<String> getImagesByText(String text) throws FlickrException {
    	SearchParameters searchParameters = new SearchParameters();
	    searchParameters.setText(text);
	    searchParameters.setSort(SearchParameters.RELEVANCE);
    	searchParameters.setSafeSearch(com.flickr4java.flickr.Flickr.SAFETYLEVEL_SAFE);
	    //searchParameters.setSort(SearchParameters.INTERESTINGNESS_DESC);
    	return getImages(searchParameters);
    }
    
    public List<String> getImagesByTag(String tag) throws FlickrException {
    	SearchParameters searchParameters = new SearchParameters();
	    String[] tags = {tag};
	    searchParameters.setTags(tags);
	    searchParameters.setSort(SearchParameters.RELEVANCE);
    	searchParameters.setSafeSearch(com.flickr4java.flickr.Flickr.SAFETYLEVEL_SAFE);

    	return getImages(searchParameters);

    }
    
    public List<String> getImages(SearchParameters searchParameters) throws FlickrException {
    	List<String> retval = new ArrayList<String>();
    	Set<String> extras = new HashSet<String>();
    	extras.add("count_faves");
    	extras.add("count_comments");
    	extras.add("count_views");
    	searchParameters.setExtras(extras);
	    PhotoList<Photo> list = flickr.getPhotosInterface().search(searchParameters, 25, 1);
	    Collections.sort(list, new Comparator<Photo>() {
	        @Override
	        public int compare(Photo a, Photo b) {
	            return new Integer(b.getStats().getViews()).compareTo(new Integer(a.getStats().getViews()));
	        }
	    });
	    for (Photo p : list) {
	        Photo nfo = flickr.getPhotosInterface().getInfo(p.getId(), null);
	        try {
		        if (nfo.getOriginalSecret().isEmpty()) {
		        	if (FacePlusPlusUtils.getInstance().imageHasOneFace(p.getSmallUrl()) != null) {
		        		retval.add(p.getSmallUrl());
		        	}
		        } 
		        else {
	                p.setOriginalSecret(nfo.getOriginalSecret());
		        	if (FacePlusPlusUtils.getInstance().imageHasOneFace(p.getSmallUrl()) != null) {
		        		retval.add(p.getSmallUrl());
		        	}
		        }
	        }
	    	catch (Exception e) {
	    		logger.error(e);
	    		logger.error(searchParameters);
	    	}
		}
	    return retval;
    }

	public static void main(String[] args) throws FlickrException, FileNotFoundException, IOException {
		Properties api_props = new Properties();
		//api_props.load(new FileReader("C:\\Users\\rsada\\git\\oop_nlp\\similarity\\src\\main\\resources\\flickr_api_key.txt"));
		api_props.load(new FileReader("data\\flickr_api_key.txt"));
		Flickr flickr = new Flickr(api_props.getProperty("apiKey"), api_props.getProperty("secret"), new REST());
//		Properties token_props = new Properties();
//		token_props.load(new FileReader("C:\\Users\\rsada\\git\\oop_nlp\\similarity\\src\\main\\resources\\flickr_credentials.txt"));
//		RequestContext requestContext = RequestContext.getRequestContext();
//        Auth auth = new Auth();
//        auth.setPermission(Permission.READ);
//        auth.setToken(token_props.getProperty("Token"));
//        auth.setTokenSecret(token_props.getProperty("Secret"));
//        requestContext.setAuth(auth);
        Flickr.debugRequest = false;
        Flickr.debugStream = false;
		
	    SearchParameters searchParameters = new SearchParameters();
    	searchParameters.setSafeSearch(com.flickr4java.flickr.Flickr.SAFETYLEVEL_SAFE);
    	String tag = "Risa";
	    String[] tags = {tag};
	    searchParameters.setTags(tags);
	    //searchParameters.setText("Kumar");
	    //searchParameters.setTags(tags);
	    searchParameters.setSort(SearchParameters.RELEVANCE);
    	Set<String> extras = new HashSet<String>();
    	extras.add("count_faves");
    	extras.add("count_comments");
    	extras.add("count_views");
    	searchParameters.setExtras(extras);
    	
	    PhotoList<Photo> list = flickr.getPhotosInterface().search(searchParameters, 25, 1);
	    Collections.sort(list, new Comparator<Photo>() {
	        @Override
	        public int compare(Photo a, Photo b) {
	            return new Integer(b.getStats().getViews()).compareTo(new Integer(a.getStats().getViews()));
	        }
	    });

	    for (Photo p : list) {
	    	System.out.println(p.getStats().getFavorites());
	    	System.out.println(p.getStats().getComments());
	    	System.out.println(p.getStats().getViews());
	        try {
	            Photo nfo = flickr.getPhotosInterface().getInfo(p.getId(), null);
	            System.out.println(p.getStats());
	            System.out.println(nfo.getStats());
	            if (nfo.getOriginalSecret().isEmpty()) {
	                System.out.println(p.getTitle() + "\t" + p.getSmallUrl());
	            } else {
	                p.setOriginalSecret(nfo.getOriginalSecret());
	                System.out.println(p.getTitle() + "\t" + p.getSmallUrl());
	            }
	        } 
	        catch (FlickrException e) {
	            e.printStackTrace();
	        }
	    	System.out.println("-----------------------------------");
		}
	}

}
