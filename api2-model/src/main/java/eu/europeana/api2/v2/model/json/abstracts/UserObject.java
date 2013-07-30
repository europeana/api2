/*
 * Copyright 2007-2012 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved 
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *  
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under 
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of 
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under 
 *  the Licence.
 */

package eu.europeana.api2.v2.model.json.abstracts;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import eu.europeana.corelib.definitions.model.ThumbSize;
import eu.europeana.corelib.definitions.solr.DocType;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@JsonSerialize(include = Inclusion.NON_EMPTY)
public abstract class UserObject {

	protected static final String IMAGE_SITE = "http://europeanastatic.eu/api/image";
	protected static final String URI_PARAM = "?uri=";
	protected static final String SIZE_PARAM = "&size=";
	protected static final String TYPE_PARAM = "&type=";
	
	public Long id;

	public String europeanaId;

	public String title;

	public String europeanaObject;

	public DocType docType = DocType.IMAGE;

	public Date dateSaved;

	public Long getId() {
		return id;
	}

	public String getEuropeanaId() {
		return europeanaId;
	}

	public String getTitle() {
		return title;
	}

	public String getEuropeanaObject() {
		if (StringUtils.isNotBlank(europeanaObject)) {
			StringBuilder url = new StringBuilder(IMAGE_SITE);
			try {
				url.append(URI_PARAM).append(URLEncoder.encode(europeanaObject, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			url.append(SIZE_PARAM).append(ThumbSize.LARGE);
			url.append(TYPE_PARAM).append(getDocType().toString());
			return url.toString();
		}
		return null;
	}

	public DocType getDocType() {
		return docType;
	}

	public Date getDateSaved() {
		return dateSaved;
	}
}
