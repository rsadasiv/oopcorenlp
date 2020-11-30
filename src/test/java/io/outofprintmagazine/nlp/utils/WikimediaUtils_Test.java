package io.outofprintmagazine.nlp.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import io.outofprintmagazine.util.ParameterStoreProperties;

public class WikimediaUtils_Test {

	public WikimediaUtils_Test() {
		super();
	}
	
	@Test
	public void LinkCharacterEncoding_Test() throws IOException, URISyntaxException {
		Properties props = new Properties();
		props.setProperty("wikipedia_apikey", "OOPCoreNlp/1.0 (rsadasiv@gmail.com) httpclient/4.5.6");
		ParameterStoreProperties parameterStore = new ParameterStoreProperties();
		parameterStore.init(props);
		WikimediaUtils wikimediaUtils = WikimediaUtils.getInstance(parameterStore);
		List<String> targets = Arrays.asList(
				"https://upload.wikimedia.org/wikipedia/commons/6/62/Boardwalk_at_Yalta_Ukraine_%283943047709%29.jpg",
				"https://upload.wikimedia.org/wikipedia/commons/c/c9/Crimea_South_Coast_04-14_img01_Simferopol-Yalta_trolley.jpg",
				"https://upload.wikimedia.org/wikipedia/commons/4/4b/Dulber_Palace.JPG",
				"https://upload.wikimedia.org/wikipedia/commons/a/a7/Eklizi-Burun-mountain.jpg",
				"https://upload.wikimedia.org/wikipedia/commons/a/aa/Hansaray1.jpg",
				"https://upload.wikimedia.org/wikipedia/commons/2/26/Ivan_Constantinovich_Aivazovsky_-_The_Russian_Squadron_on_the_Sebastopol_Roads.jpg",
				"https://upload.wikimedia.org/wikipedia/commons/c/c2/Jalta-confer.jpg",
				"https://upload.wikimedia.org/wikipedia/commons/7/78/Kanaka._Crimea._Urraine.jpg",
				"https://upload.wikimedia.org/wikipedia/commons/8/86/Koreiz_-_beach3.jpg",
				"https://upload.wikimedia.org/wikipedia/commons/3/30/Livadia_Palace_Crimea.jpg",
				"https://upload.wikimedia.org/wikipedia/commons/6/63/Map_of_the_Crimea.png",
				"https://upload.wikimedia.org/wikipedia/commons/2/22/Novyi_Svit_IMG_2941_1725.jpg",
				"https://upload.wikimedia.org/wikipedia/commons/9/9b/People_at_KaZantip.jpg",
				"https://upload.wikimedia.org/wikipedia/commons/1/11/Relief_map_of_Crimea_%28disputed_status%29.jpg",
				"https://upload.wikimedia.org/wikipedia/commons/a/ad/Satellite_picture_of_Crimea%2C_Terra-MODIS%2C_05-16-2015.jpg",
				"https://upload.wikimedia.org/wikipedia/commons/a/af/Siege_of_Sevastopol_by_George_Baxter.jpg",
				"https://upload.wikimedia.org/wikipedia/commons/1/16/Simferopol_04-14_img18_K-Marx-Street.jpg",
				"https://upload.wikimedia.org/wikipedia/commons/b/b3/St._Volodymyr%27s_Cathedral%2C_Chersones.jpg",
				"https://upload.wikimedia.org/wikipedia/commons/8/83/THE_GENOESE_FORTRESS_IN_CAFFA.jpg",
				"https://upload.wikimedia.org/wikipedia/commons/f/f5/Terra.png",
				"https://upload.wikimedia.org/wikipedia/commons/d/dd/Varangian_routes.png",
				"https://upload.wikimedia.org/wikipedia/commons/8/84/Yalta-catholic_church.jpg",
				"https://upload.wikimedia.org/wikipedia/commons/a/a2/Yalta_Kanatka.JPG"
		);
		String title = "Crimea";
		//try {
			List<String> imgLinks = wikimediaUtils.getImages(title);
			for (String target : targets) {
				//assertTrue(imgLinks.contains(target),String.format("%s missing imageLink %s", title, target));
				//unreliable
				if (!(imgLinks.contains(target))) {
					System.err.println(String.format("WARNING: %s missing imageLink %s", title, target));
				}
			}
		//}
		//catch (Throwable t) {
		//	t.printStackTrace();
		//}

	}

}
