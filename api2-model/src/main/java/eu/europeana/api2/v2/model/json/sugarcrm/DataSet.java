/*
 * Copyright 2007-2015 The Europeana Foundation
 *
 * Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 * by the European Commission;
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 * any kind, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package eu.europeana.api2.v2.model.json.sugarcrm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.NotSaved;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * @author Georgios Markakis (gwarkx@hotmail.com)
 * @since Sep 24, 2013
 */
@Entity
@JsonInclude(NON_EMPTY)
public class DataSet {

    /**
     * Default constructor used by Jackson & Morphia (do not remove)
     */
    public DataSet() {
        // Used by Jackson
    }

    /**
     * The identifier of the dataset
     */
    @Id
    @Indexed
    public String identifier;

    /**
     * The identifier of the provider
     */
    @Indexed
    public String provIdentifier;

    /**
     * The name of provider
     */
    public String providerName;

    /**
     * edm:datasetName (same as europeana_collectionName)
     */
    @NotSaved
    public String edmDatasetName;

    /**
     * Status
     * <p/>
     * Possible values: 'Ingestion complete', 'Ready for Harvesting', 'Disabled and Replaced',
     * 'Ongoing scheduled updates', 'OAI-PHM testing', 'Ready for Replication',
     * 'Mapping and Normalization'
     */
    @NotSaved
    public String status;

    /**
     * The number of published records
     */
    @NotSaved
    public long publishedRecords;

    /**
     * The number of deleted records
     */
    @NotSaved
    public long deletedRecords;

    /**
     * Date of dataset creation
     */
    @NotSaved
    public String creationDate;

    /**
     * Date of dataset publication
     */
    @NotSaved
    public String publicationDate;

    /**
     *
     */
    @JsonIgnore
    public Map<String, String> savedsugarcrmFields;
}
