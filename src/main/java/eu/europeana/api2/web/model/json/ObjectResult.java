package eu.europeana.api2.web.model.json;

import eu.europeana.api2.web.model.json.abstracts.ApiResponse;
import eu.europeana.corelib.definitions.solr.beans.FullBean;

public class ObjectResult extends ApiResponse {
	
	public FullBean object;
	
	public ObjectResult(String apikey, String action) {
		super(apikey, action);
	}

}
