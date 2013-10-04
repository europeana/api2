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
package eu.europeana.api2.v2.model.json.sugarcrm;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;


/**
 * @author Georgios Markakis (gwarkx@hotmail.com)
 *
 * @since Sep 24, 2013
 */ 
@JsonSerialize(include = Inclusion.NON_EMPTY)
public class Provider{

	/**
	 * Default constructor used by Jackson (do not remove)
	 */
	public Provider(){
		//Used by Jackson
	}
		 
	public String identifier;
	
	public String name;

	public String description;
	
	public String website;
	
	public String country;

}
