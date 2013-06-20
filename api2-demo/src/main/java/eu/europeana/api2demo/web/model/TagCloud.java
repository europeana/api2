package eu.europeana.api2demo.web.model;

import eu.europeana.api2.v2.model.json.UserResults;

public class TagCloud extends UserResults<TagCloudItem> {
	
	public TagCloud() {
		super();
	}
	
	public TagCloud(String apikey, String action) {
		super(apikey, action);
	}
	
}
