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

import java.util.Map;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.annotations.NotSaved;

/**
 * @author Georgios Markakis (gwarkx@hotmail.com)
 *
 * @since Sep 24, 2013
 */
@Entity
@JsonSerialize(include = Inclusion.NON_EMPTY)
public class DataSet{

	/**
	 * Default constructor used by Jackson & Morphia (do not remove)
	 */
	public DataSet(){
		//Used by Jackson
	}

	/**
	 * 
	 */
	@Id
	@Indexed
	public String identifier;

	/**
	 * 
	 */
	@Indexed
	public String provIdentifier;

	/**
	 * 
	 */
	public String providerName;

	/**
	 * 
	 */
	@NotSaved
	public String edmDatasetName;

	/**
	 * 
	 */
	@NotSaved
	public String status;

	/**
	 * 
	 */
	@NotSaved
	public long publishedRecords;

	/**
	 * 
	 */
	@NotSaved
	public long deletedRecords;

	/**
	 * 
	 */
	@NotSaved
	public String creationDate;

	/**
	 * 
	 */
	@NotSaved
	public String publicationDate;

	/**
	 * 
	 */
	@JsonIgnore
	public Map<String,String> savedsugarcrmFields;
}
