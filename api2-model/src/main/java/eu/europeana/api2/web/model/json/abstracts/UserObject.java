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

package eu.europeana.api2.web.model.json.abstracts;

import java.util.Date;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import eu.europeana.corelib.definitions.solr.DocType;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@JsonSerialize(include = Inclusion.NON_EMPTY)
public abstract class UserObject {

	public Long id;

	public String europeanaUri;

	public String title;

	public String europeanaObject;

	public DocType docType = DocType.IMAGE;

	public Date dateSaved;

	public Long getId() {
		return id;
	}

	public String getEuropeanaUri() {
		return europeanaUri;
	}

	public String getTitle() {
		return title;
	}

	public String getEuropeanaObject() {
		return europeanaObject;
	}

	public DocType getDocType() {
		return docType;
	}

	public Date getDateSaved() {
		return dateSaved;
	}
}
