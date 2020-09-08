/*******************************************************************************
 * Copyright (C) 2020 Ram Sadasiv
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package io.outofprintmagazine.nlp.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.outofprintmagazine.util.IParameterStore;

public class WikimediaUtils {
	
	private static final Logger logger = LogManager.getLogger(WikimediaUtils.class);
	
	@SuppressWarnings("unused")
	private Logger getLogger() {
		return logger;
	}
	
	private static final int BATCH_SIZE = 20;
	private IParameterStore parameterStore = null;
	
	private WikimediaUtils(IParameterStore parameterStore) throws IOException {
		this.parameterStore = parameterStore;
	}
	
	private static Map<IParameterStore, WikimediaUtils> instances = new HashMap<IParameterStore, WikimediaUtils>();
	
    public static WikimediaUtils getInstance(IParameterStore parameterStore) throws IOException { 
        if (instances.get(parameterStore) == null) {
        	WikimediaUtils instance = new WikimediaUtils(parameterStore);
            instances.put(parameterStore, instance);
        }
        return instances.get(parameterStore); 
    }
    
    public List<String> getImagesByText(String text) throws IOException, URISyntaxException {
    	//logger.info("byText: " + text);
    	List<String> retval = getImages(text);
    	for (String imgurl : retval) {
    		//logger.info(imgurl);
    	}
    	return retval;
    }
    
    public List<String> getImagesByTag(String text) throws IOException, URISyntaxException {
    	//logger.debug("byTag: " + text);
    	List<String> retval = getImages(text);
    	for (String imgurl : retval) {
    		//logger.debug(imgurl);
    	}
    	return retval;
    }
    
    //https://en.wikipedia.org/w/api.php?action=query&generator=images&titles=Coimbatore&prop=info
    //https://en.wikipedia.org/w/api.php?action=query&titles=File:Coimbatore-TNSTC-JnNURM-Bus.JPG&prop=imageinfo&iiprop=timestamp|user|userid|comment|canonicaltitle|url|size|dimensions|sha1|mime|thumbmime|mediatype|bitdepth
/*
https://en.wikipedia.org/w/api.php?format=json&action=query&prop=imageinfo&iiprop=timestamp|user|userid|comment|canonicaltitle|url|size|dimensions|sha1|mime|thumbmime|mediatype|bitdepth&titles=File%3A2009-3-14_ManUtd_vs_LFC_Red_Card_Vidic.JPG|File%3AAU_Fire_Danger_Indicator.jpg|File%3AAlfa_Romeo_33_SC_12_Sovralimentata_1977_red_vr_TCE.jpg|File%3AAlizarin-sample.jpg|File%3AAmsterdam_red_light_district_24-7-2003.JPG|File%3AAt_the_Devil%27s_Ball_1.jpg|File%3AAztecheaddress.jpg|File%3ABoutet_1708_color_circles.jpg|File%3AAgarplate_redbloodcells_edit.jpg|File%3ABoschTheCrucifixionOfStJulia.jpg|File%3ABritish_home_and_distant_railway_semaphore_RYG_signals.svg|File%3AByzantine_imperial_flag%2C_14th_century%2C_square.svg|File%3ACan_Setter_dog_GFDL.jpg|File%3ACardinal.jpg|File%3ACardinal_Th%C3%A9odore_Adrien_Sarr_2.JPG|File%3ACherry_blossoms_in_the_Tsutsujigaoka_Park.jpg|File%3AChinese_honor_guard_in_column_070322-F-0193C-014.JPEG|File%3ACrimson_sunset.jpg|File%3ACommons-logo.svg|File%3AElizabeth_I_Steven_Van_Der_Meulen.jpg
https://en.wikipedia.org/w/api.php?format=json&action=query&prop=imageinfo&iiprop=timestamp|user|userid|comment|canonicaltitle|url|size|dimensions|sha1|mime|thumbmime|mediatype|bitdepth&titles=File%3AA_Badge_Pinning.jpg|File%3ABadge_1012.jpg|File%3AChevalier_l%C3%A9gion_d%27honneur_2.png|File%3ADima-rs1.jpg|File%3ADispositif_d%27une_%C3%A9pingle_de_s%C3%BBret%C3%A9_sur_une_%C3%A9pinglette.JPG|File%3AGeneseeDABadge.jpg|File%3AGreenville%2C_North_Carolina_Police_Badge.jpg|File%3ACommons-logo.svg|File%3AFolder_Hexagonal_Icon.svg|File%3ANobel_Prize.png|File%3APeople_icon.svg|File%3AStar_of_the_Garter.png|File%3AQuestion_book-new.svg
https://en.wikipedia.org/w/api.php?format=json&action=query&prop=imageinfo&iiprop=timestamp|user|userid|comment|canonicaltitle|url|size|dimensions|sha1|mime|thumbmime|mediatype|bitdepth&titles=File:Disambig_gray.svg
 */
    
    protected class ImagePageHandler implements IJsonResponseHandler {
    	List<String> retval = new ArrayList<String>();
    	
    	public ImagePageHandler() {
    		super();
    	}
    	
    	public List<String> getValues() {
    		return retval;
    	}
    	
    	@Override
		public void onPage(JsonNode page) {
    		if (page != null && page.has("query") && page.get("query").has("pages")) {
				JsonNode pagesNode = page.get("query").get("pages");
				Iterator<Entry<String, JsonNode>> pagesIter = pagesNode.fields();
				while (pagesIter.hasNext()) {
					String x = pagesIter.next().getValue().get("title").asText();
					if (StringUtils.isAsciiPrintable(x)) {
						retval.add(x);
					}
				}
			}   		
    	}
    }
    
    protected class ImageInfoPageHandler implements IJsonResponseHandler {
    	List<String> retval = new ArrayList<String>();
    	
    	public ImageInfoPageHandler() {
    		super();
    	}
    	
    	public List<String> getValues() {
    		return retval;
    	}
    	
    	@Override
		public void onPage(JsonNode page) {
    		if (page != null && page.has("query") && page.get("query").has("pages")) {
    			JsonNode pagesNode = page.get("query").get("pages");
    			Iterator<Entry<String, JsonNode>> pagesIter = pagesNode.fields();
    			while (pagesIter.hasNext()) {
    				JsonNode pageNode = pagesIter.next().getValue().get("imageinfo").get(0);
    				if (pageNode.get("mediatype").asText().equalsIgnoreCase("BITMAP")) {
    					retval.add(pageNode.get("url").asText());
    				}
    			}
			}   		
    	}
    }
	
    public List<String> getImages(String title) throws IOException, URISyntaxException {
    	if (!StringUtils.isAsciiPrintable(title)) {
    		return new ArrayList<String>();
    	}
    	ImagePageHandler imageHandler = new ImagePageHandler();
		List<NameValuePair> imageParams = HttpUtils.getInstance(parameterStore).getWikimediaImageParameters();
		title = title.replace(' ', '_');
		imageParams.add(new BasicNameValuePair("titles", title));
		List<Header> headers = new ArrayList<Header>();
		headers.add(new BasicHeader("User-Agent", parameterStore.getProperty("wikipedia_apikey")));
		HttpUtils.getInstance(parameterStore).httpGetJsonPaginated(
				HttpUtils.getInstance(parameterStore).getWikipediaApi(), 
				imageParams, 
				headers, 
				"gimcontinue",
				imageHandler
		);

    	ImageInfoPageHandler imageInfoHandler = new ImageInfoPageHandler();
		Iterator<String> imageTitlesIter = imageHandler.getValues().iterator();
		while (imageTitlesIter.hasNext()) {
			List<String> queries = new ArrayList<String>();
			for (int i=0;i<BATCH_SIZE&&imageTitlesIter.hasNext();i++) {
				String imageTitle = imageTitlesIter.next();
				imageTitle = imageTitle.replace(' ', '_');
				queries.add(imageTitle);
			}
			List<NameValuePair> infoParams = HttpUtils.getInstance(parameterStore).getWikimediaImageInfoParameters();
			infoParams.add(new BasicNameValuePair("titles", String.join("|", queries)));			
			HttpUtils.getInstance(parameterStore).httpGetJsonCB(
					HttpUtils.getInstance(parameterStore).buildUri(
							HttpUtils.getInstance(parameterStore).getWikipediaApi(), 
							infoParams
					),
					headers, 
					imageInfoHandler
			);
			
		}
		return imageInfoHandler.getValues();
    }
}
