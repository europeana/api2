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

package eu.europeana.api2.v2.web.controller;

import static org.junit.Assert.*;

import eu.europeana.api2.v2.utils.ModelUtils;
import eu.europeana.corelib.definitions.solr.SolrFacetType;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import eu.europeana.corelib.utils.StringArrayUtils;

import java.util.Map;

public class SearchControllerTest {

	@Test
	public void testLimitFacets() {
        String                facetParamString = "ag_dc_date,ag_dc_identifier,ag_edm_begin,ag_edm_end,ag_edm_hasMet,ag_edm_isRelatedTo," +
                "ag_edm_wasPresentAt,ag_foaf_name,ag_owl_sameAs,ag_rdagr2_biographicalInformation,ag_rdagr2_dateOfBirth,ag_rdagr2_dateOfDeath," +
                "ag_rdagr2_dateOfEstablishment,ag_rdagr2_dateOfTermination,ag_rdagr2_gender,ag_rdagr2_professionOrOccupation,ag_skos_altLabel," +
                "ag_skos_hiddenLabel,ag_skos_note,ag_skos_prefLabel,cc_skos_altLabel,cc_skos_broader,cc_skos_broaderLabel,cc_skos_broadMatch," +
                "cc_skos_closeMatch,cc_skos_exactMatch,cc_skos_hiddenLabel,cc_skos_inScheme,cc_skos_narrower,cc_skos_narrowMatch,cc_skos_notation," +
                "cc_skos_note,cc_skos_prefLabel,cc_skos_related,cc_skos_relatedMatch,COMPLETENESS,CONTRIBUTOR,country,COUNTRY,DATA_PROVIDER,date," +
                "description,edm_agent,edm_europeana_aggregation,edm_europeana_proxy,edm_place,edm_previewNoDistribute,edm_timespan,edm_UGC," +
                "edm_webResource,europeana_aggregation_dc_creator,europeana_aggregation_edm_country,europeana_aggregation_edm_hasView," +
                "europeana_aggregation_edm_isShownBy,europeana_aggregation_edm_landingPage,europeana_aggregation_edm_language," +
                "europeana_aggregation_edm_rights,europeana_aggregation_ore_aggregatedCHO,europeana_aggregation_ore_aggregates," +
                "europeana_collectionName,europeana_completeness,europeana_id,europeana_previewNoDistribute,format,identifier,language,LANGUAGE," +
                "location,LOCATION,pl_dcterms_hasPart,pl_dcterms_isPartOf,pl_dcterms_isPartOf_label,pl_owl_sameAs,pl_skos_altLabel,pl_skos_hiddenLabel," +
                "pl_skos_note,pl_skos_prefLabel,pl_wgs84_pos_alt,pl_wgs84_pos_lat,pl_wgs84_pos_lat_long,pl_wgs84_pos_long,PROVIDER," +
                "provider_aggregation_dc_rights,provider_aggregation_edm_aggregatedCHO,provider_aggregation_edm_dataProvider," +
                "provider_aggregation_edm_hasView,provider_aggregation_edm_isShownAt,provider_aggregation_edm_isShownBy,provider_aggregation_edm_object," +
                "provider_aggregation_edm_provider,provider_aggregation_edm_rights,provider_aggregation_edm_unstored,provider_aggregation_ore_aggregates," +
                "provider_aggregation_ore_aggregation,proxy_dc_contributor,proxy_dc_coverage,proxy_dc_creator,proxy_dc_date,proxy_dc_description," +
                "proxy_dc_format,proxy_dc_identifier,proxy_dc_language,proxy_dc_publisher,proxy_dc_relation,proxy_dc_rights,proxy_dc_source," +
                "proxy_dc_subject,proxy_dc_title,proxy_dc_type,proxy_dcterms_alternative,proxy_dcterms_conformsTo,proxy_dcterms_created," +
                "proxy_dcterms_extent,proxy_dcterms_hasFormat,proxy_dcterms_hasPart,proxy_dcterms_hasVersion,proxy_dcterms_isFormatOf," +
                "proxy_dcterms_isPartOf,proxy_dcterms_isReferencedBy,proxy_dcterms_isReplacedBy,proxy_dcterms_isRequiredBy,proxy_dcterms_issued," +
                "proxy_dcterms_isVersionOf,proxy_dcterms_medium,proxy_dcterms_provenance,proxy_dcterms_references,proxy_dcterms_replaces," +
                "proxy_dcterms_requires,proxy_dcterms_spatial,proxy_dcterms_tableOfContents,proxy_dcterms_temporal,proxy_edm_currentLocation," +
                "proxy_edm_currentLocation_lat,proxy_edm_currentLocation_lon,proxy_edm_hasMet,proxy_edm_hasType,proxy_edm_incorporates," +
                "proxy_edm_isDerivativeOf,proxy_edm_isNextInSequence,proxy_edm_isRelatedTo,proxy_edm_isRepresentationOf,proxy_edm_isSimilarTo," +
                "proxy_edm_isSuccessorOf,proxy_edm_realizes,proxy_edm_rights,proxy_edm_type,proxy_edm_unstored,proxy_edm_userTags," +
                "proxy_edm_wasPresentAt,proxy_edm_year,proxy_ore_proxy,proxy_ore_proxyFor,proxy_ore_proxyIn,proxy_owl_sameAs,publisher," +
                "relation,rights,RIGHTS,skos_concept,source,subject,SUBJECT,text,timestamp,title,ts_dcterms_hasPart,ts_dcterms_isPartOf," +
                "ts_dcterms_isPartOf,ts_dcterms_isPartOf_label,ts_edm_begin,ts_edm_end,ts_owl_sameAs,ts_skos_altLabel,ts_skos_hiddenLabel," +
                "ts_skos_note,ts_skos_prefLabel,TYPE,UGC,USERTAGS,what,when,where,who,wr_dc_description,wr_dc_format,wr_dc_rights,wr_dc_source," +
                "wr_dcterms_conformsTo,wr_dcterms_created,wr_dcterms_extent,wr_dcterms_hasPart,wr_dcterms_isFormatOf,wr_dcterms_issued," +
                "wr_edm_isNextInSequence,wr_edm_rights,YEAR";
        String[]              facets           = StringArrayUtils.splitWebParameter(new String[]{facetParamString});
        Map<String, String[]> separatedFacets  = ModelUtils.separateAndLimitFacets(facets, false);
        assertEquals(8, separatedFacets.get("solrfacets").length);
        assertEquals(142, separatedFacets.get("customfacets").length);
    }

    @Test
    public void testLimitFacetsDefaultRequested() {
        String                facetParamString = "ag_dc_date,ag_dc_identifier,ag_edm_begin,ag_edm_end,ag_edm_hasMet,ag_edm_isRelatedTo," +
                "ag_edm_wasPresentAt,ag_foaf_name,ag_owl_sameAs,ag_rdagr2_biographicalInformation,ag_rdagr2_dateOfBirth,ag_rdagr2_dateOfDeath," +
                "ag_rdagr2_dateOfEstablishment,ag_rdagr2_dateOfTermination,ag_rdagr2_gender,ag_rdagr2_professionOrOccupation,ag_skos_altLabel," +
                "ag_skos_hiddenLabel,ag_skos_note,ag_skos_prefLabel,cc_skos_altLabel,cc_skos_broader,cc_skos_broaderLabel,cc_skos_broadMatch," +
                "cc_skos_closeMatch,cc_skos_exactMatch,cc_skos_hiddenLabel,cc_skos_inScheme,cc_skos_narrower,cc_skos_narrowMatch,cc_skos_notation," +
                "cc_skos_note,cc_skos_prefLabel,cc_skos_related,cc_skos_relatedMatch,COMPLETENESS,CONTRIBUTOR,country,COUNTRY,DATA_PROVIDER,date," +
                "description,edm_agent,edm_europeana_aggregation,edm_europeana_proxy,edm_place,edm_previewNoDistribute,edm_timespan,edm_UGC," +
                "edm_webResource,europeana_aggregation_dc_creator,europeana_aggregation_edm_country,europeana_aggregation_edm_hasView," +
                "europeana_aggregation_edm_isShownBy,europeana_aggregation_edm_landingPage,europeana_aggregation_edm_language," +
                "europeana_aggregation_edm_rights,europeana_aggregation_ore_aggregatedCHO,europeana_aggregation_ore_aggregates,europeana_collectionName," +
                "europeana_completeness,europeana_id,europeana_previewNoDistribute,format,identifier,language,LANGUAGE,location,LOCATION," +
                "pl_dcterms_hasPart,pl_dcterms_isPartOf,pl_dcterms_isPartOf_label,pl_owl_sameAs,pl_skos_altLabel,pl_skos_hiddenLabel,pl_skos_note," +
                "pl_skos_prefLabel,pl_wgs84_pos_alt,pl_wgs84_pos_lat,pl_wgs84_pos_lat_long,pl_wgs84_pos_long,PROVIDER,provider_aggregation_dc_rights," +
                "provider_aggregation_edm_aggregatedCHO,provider_aggregation_edm_dataProvider,provider_aggregation_edm_hasView," +
                "provider_aggregation_edm_isShownAt,provider_aggregation_edm_isShownBy,provider_aggregation_edm_object,provider_aggregation_edm_provider," +
                "provider_aggregation_edm_rights,provider_aggregation_edm_unstored,provider_aggregation_ore_aggregates," +
                "provider_aggregation_ore_aggregation,proxy_dc_contributor,proxy_dc_coverage,proxy_dc_creator,proxy_dc_date,proxy_dc_description," +
                "proxy_dc_format,proxy_dc_identifier,proxy_dc_language,proxy_dc_publisher,proxy_dc_relation,proxy_dc_rights,proxy_dc_source," +
                "proxy_dc_subject,proxy_dc_title,proxy_dc_type,proxy_dcterms_alternative,proxy_dcterms_conformsTo,proxy_dcterms_created," +
                "proxy_dcterms_extent,proxy_dcterms_hasFormat,proxy_dcterms_hasPart,proxy_dcterms_hasVersion,proxy_dcterms_isFormatOf," +
                "proxy_dcterms_isPartOf,proxy_dcterms_isReferencedBy,proxy_dcterms_isReplacedBy,proxy_dcterms_isRequiredBy,proxy_dcterms_issued," +
                "proxy_dcterms_isVersionOf,proxy_dcterms_medium,proxy_dcterms_provenance,proxy_dcterms_references,proxy_dcterms_replaces," +
                "proxy_dcterms_requires,proxy_dcterms_spatial,proxy_dcterms_tableOfContents,proxy_dcterms_temporal,proxy_edm_currentLocation," +
                "proxy_edm_currentLocation_lat,proxy_edm_currentLocation_lon,proxy_edm_hasMet,proxy_edm_hasType,proxy_edm_incorporates," +
                "proxy_edm_isDerivativeOf,proxy_edm_isNextInSequence,proxy_edm_isRelatedTo,proxy_edm_isRepresentationOf,proxy_edm_isSimilarTo," +
                "proxy_edm_isSuccessorOf,proxy_edm_realizes,proxy_edm_rights,proxy_edm_type,proxy_edm_unstored,proxy_edm_userTags," +
                "proxy_edm_wasPresentAt,proxy_edm_year,proxy_ore_proxy,proxy_ore_proxyFor,proxy_ore_proxyIn,proxy_owl_sameAs,publisher,relation," +
                "rights,RIGHTS,skos_concept,source,subject,SUBJECT,text,timestamp,title,ts_dcterms_hasPart,ts_dcterms_isPartOf,ts_dcterms_isPartOf," +
                "ts_dcterms_isPartOf_label,ts_edm_begin,ts_edm_end,ts_owl_sameAs,ts_skos_altLabel,ts_skos_hiddenLabel,ts_skos_note,ts_skos_prefLabel," +
                "TYPE,UGC,USERTAGS,what,when,where,who,wr_dc_description,wr_dc_format,wr_dc_rights,wr_dc_source,wr_dcterms_conformsTo," +
                "wr_dcterms_created,wr_dcterms_extent,wr_dcterms_hasPart,wr_dcterms_isFormatOf,wr_dcterms_issued,wr_edm_isNextInSequence," +
                "wr_edm_rights,YEAR";
        String[]              facets           = StringArrayUtils.splitWebParameter(new String[]{facetParamString});
        Map<String, String[]> separatedFacets  = ModelUtils.separateAndLimitFacets(facets, true);
        assertEquals(9, separatedFacets.get("technicalfacets").length);
        assertEquals(13, separatedFacets.get("solrfacets").length);
        assertEquals(128, separatedFacets.get("customfacets").length);
        System.out.println("Technical facets:");
        System.out.println(StringUtils.join(separatedFacets.get("technicalfacets"), ", "));
        System.out.println("SOLR facets:");
        System.out.println(StringUtils.join(separatedFacets.get("solrfacets"), ", "));
        System.out.println("custom SOLR facets:");
        System.out.println(StringUtils.join(separatedFacets.get("customfacets"), ", "));
    }

    @Test
    public void testLimitFacetsWithTechnicalOnes() {
        String                facetParamString = "ag_dc_date,ag_dc_identifier,DEFAULT,ag_edm_begin,ag_edm_end,ag_edm_hasMet,ag_edm_isRelatedTo," +
                "ag_edm_wasPresentAt,ag_foaf_name,ag_owl_sameAs,ag_rdagr2_biographicalInformation,ag_rdagr2_dateOfBirth,ag_rdagr2_dateOfDeath," +
                "ag_rdagr2_dateOfEstablishment,ag_rdagr2_dateOfTermination,ag_rdagr2_gender,ag_rdagr2_professionOrOccupation,ag_skos_altLabel," +
                "ag_skos_hiddenLabel,ag_skos_note,ag_skos_prefLabel,cc_skos_altLabel,cc_skos_broader,cc_skos_broaderLabel,cc_skos_broadMatch," +
                "cc_skos_closeMatch,cc_skos_exactMatch,cc_skos_hiddenLabel,cc_skos_inScheme,cc_skos_narrower,cc_skos_narrowMatch,cc_skos_notation," +
                "cc_skos_note,cc_skos_prefLabel,cc_skos_related,cc_skos_relatedMatch,COLOURPALETTE,COMPLETENESS,CONTRIBUTOR,country,COUNTRY," +
                "DATA_PROVIDER,date,description,edm_agent,edm_europeana_aggregation,edm_europeana_proxy,edm_place,edm_previewNoDistribute,edm_timespan," +
                "edm_UGC,edm_webResource,europeana_aggregation_dc_creator,europeana_aggregation_edm_country,europeana_aggregation_edm_hasView," +
                "europeana_aggregation_edm_isShownBy,europeana_aggregation_edm_landingPage,europeana_aggregation_edm_language," +
                "europeana_aggregation_edm_rights,europeana_aggregation_ore_aggregatedCHO,europeana_aggregation_ore_aggregates,europeana_collectionName," +
                "europeana_completeness,europeana_id,europeana_previewNoDistribute,format,identifier,IMAGE_COLOUR,language,LANGUAGE,location," +
                "LOCATION,pl_dcterms_hasPart,pl_dcterms_isPartOf,pl_dcterms_isPartOf_label,pl_owl_sameAs,pl_skos_altLabel,pl_skos_hiddenLabel," +
                "pl_skos_note,pl_skos_prefLabel,pl_wgs84_pos_alt,pl_wgs84_pos_lat,pl_wgs84_pos_lat_long,pl_wgs84_pos_long,PROVIDER," +
                "provider_aggregation_dc_rights,provider_aggregation_edm_aggregatedCHO,provider_aggregation_edm_dataProvider," +
                "provider_aggregation_edm_hasView,provider_aggregation_edm_isShownAt,provider_aggregation_edm_isShownBy,provider_aggregation_edm_object," +
                "provider_aggregation_edm_provider,provider_aggregation_edm_rights,provider_aggregation_edm_unstored,provider_aggregation_ore_aggregates," +
                "provider_aggregation_ore_aggregation,proxy_dc_contributor,proxy_dc_coverage,proxy_dc_creator,proxy_dc_date,proxy_dc_description," +
                "proxy_dc_format,proxy_dc_identifier,proxy_dc_language,proxy_dc_publisher,proxy_dc_relation,proxy_dc_rights,proxy_dc_source," +
                "proxy_dc_subject,proxy_dc_title,proxy_dc_type,proxy_dcterms_alternative,proxy_dcterms_conformsTo,proxy_dcterms_created," +
                "proxy_dcterms_extent,proxy_dcterms_hasFormat,proxy_dcterms_hasPart,proxy_dcterms_hasVersion,proxy_dcterms_isFormatOf," +
                "proxy_dcterms_isPartOf,proxy_dcterms_isReferencedBy,proxy_dcterms_isReplacedBy,proxy_dcterms_isRequiredBy,proxy_dcterms_issued," +
                "proxy_dcterms_isVersionOf,proxy_dcterms_medium,proxy_dcterms_provenance,proxy_dcterms_references,proxy_dcterms_replaces," +
                "proxy_dcterms_requires,proxy_dcterms_spatial,proxy_dcterms_tableOfContents,proxy_dcterms_temporal,proxy_edm_currentLocation," +
                "proxy_edm_currentLocation_lat,proxy_edm_currentLocation_lon,proxy_edm_hasMet,proxy_edm_hasType,proxy_edm_incorporates," +
                "proxy_edm_isDerivativeOf,proxy_edm_isNextInSequence,proxy_edm_isRelatedTo,proxy_edm_isRepresentationOf,proxy_edm_isSimilarTo," +
                "proxy_edm_isSuccessorOf,proxy_edm_realizes,proxy_edm_rights,proxy_edm_type,proxy_edm_unstored,proxy_edm_userTags,proxy_edm_wasPresentAt," +
                "proxy_edm_year,proxy_ore_proxy,proxy_ore_proxyFor,proxy_ore_proxyIn,proxy_owl_sameAs,publisher,relation,rights,RIGHTS,skos_concept," +
                "source,subject,SUBJECT,text,timestamp,title,ts_dcterms_hasPart,ts_dcterms_isPartOf,ts_dcterms_isPartOf,ts_dcterms_isPartOf_label," +
                "ts_edm_begin,ts_edm_end,ts_owl_sameAs,ts_skos_altLabel,ts_skos_hiddenLabel,ts_skos_note,ts_skos_prefLabel,TYPE,UGC,USERTAGS,VIDEO_HD," +
                "what,when,where,who,wr_dc_description,wr_dc_format,wr_dc_rights,wr_dc_source,wr_dcterms_conformsTo,wr_dcterms_created,wr_dcterms_extent," +
                "wr_dcterms_hasPart,wr_dcterms_isFormatOf,wr_dcterms_issued,wr_edm_isNextInSequence,wr_edm_rights,YEAR";
        String[]              facets           = StringArrayUtils.splitWebParameter(new String[]{facetParamString});
        Map<String, String[]> separatedFacets  = ModelUtils.separateAndLimitFacets(facets, false);
        assertEquals(3, separatedFacets.get("technicalfacets").length);
        assertEquals(8, separatedFacets.get("solrfacets").length);
        assertEquals(139, separatedFacets.get("customfacets").length);
        System.out.println("Technical facets:");
        System.out.println(StringUtils.join(separatedFacets.get("technicalfacets"), ", "));
        System.out.println("SOLR facets:");
        System.out.println(StringUtils.join(separatedFacets.get("solrfacets"), ", "));
        System.out.println("custom SOLR facets:");
        System.out.println(StringUtils.join(separatedFacets.get("customfacets"), ", "));
    }
}
