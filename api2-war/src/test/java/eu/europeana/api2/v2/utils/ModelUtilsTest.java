package eu.europeana.api2.v2.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ModelUtilsTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String JSONLD_GRAPH    = "@graph";
    private static final String JSONLD_TYPE   = "@type";
    private static final String JSONLD_WR_RDF_TYPE    = "edm:WebResource";
    private static final String JSONLD_WR_RDF_ID    = "@id";

    private static final String unOrderedJsonLd = "{\"@graph\":[{\"@id\":\"http://data.europeana.eu/aggregation/europeana/1234/testing_order\",\"@type\":\"edm:EuropeanaAggregation\",\"dcterms:created\":\"2017-06-06T19:40:18.082Z\",\"dcterms:modified\":\"2022-06-14T10:47:16.116Z\",\"edm:aggregatedCHO\":{\"@id\":\"http://data.europeana.eu/item/1234/testing_order\"},\"edm:completeness\":\"10\",\"edm:country\":\"Spain\",\"edm:datasetName\":\"2022711_Ag_ES_Hispana_bdmadrid\",\"edm:landingPage\":{\"@id\":\"https://www.europeana.eu/item/1234/testing_order\"},\"edm:language\":\"es\",\"edm:preview\":{\"@id\":\"https://api-test.eanadev.org/thumbnail/v2/url.json?uri=http%3A%2F%2Fwww.memoriademadrid.es%2Fdoc_anexos%2FWorkflow%2F0%2F9982%2Fmadf2006901.jpg&type=IMAGE\"},\"dqv:hasQualityAnnotation\":[{\"@id\":\"http://data.europeana.eu/item/1234/testing_order#contentTier\"},{\"@id\":\"http://data.europeana.eu/item/1234/testing_order#metadataTier\"}]},{\"@id\":\"http://data.europeana.eu/aggregation/provider/1234/testing_order\",\"@type\":\"ore:Aggregation\",\"edm:aggregatedCHO\":{\"@id\":\"http://data.europeana.eu/item/1234/testing_order\"},\"edm:dataProvider\":\"Biblioteca Digital Memoriademadrid\",\"edm:isShownAt\":{\"@id\":\"http://www.memoriademadrid.es/buscador.php?accion=VerFicha&id=9982\"},\"edm:isShownBy\":{\"@id\":\"http://www.testing/webresource/1.jpg\"},\"edm:object\":{\"@id\":\"http://www.testing/webresource/1.jpg\"},\"edm:provider\":{\"@id\":\"http://data.europeana.eu/organization/1482250000004671126\"},\"edm:rights\":{\"@id\":\"http://creativecommons.org/licenses/by-nc/4.0/\"}},{\"@id\":\"http://data.europeana.eu/item/1234/testing_order\",\"@type\":\"edm:ProvidedCHO\"},{\"@id\":\"http://data.europeana.eu/item/1234/testing_order#contentTier\",\"@type\":\"dqv:QualityAnnotation\",\"dcterms:created\":\"2022-06-14T13:52:24.726566Z\",\"oa:hasBody\":{\"@id\":\"http://www.europeana.eu/schemas/epf/contentTier2\"},\"oa:hasTarget\":{\"@id\":\"http://data.europeana.eu/aggregation/provider/1234/testing_order\"}},{\"@id\":\"http://data.europeana.eu/item/1234/testing_order#metadataTier\",\"@type\":\"dqv:QualityAnnotation\",\"dcterms:created\":\"2022-06-14T13:52:24.726979Z\",\"oa:hasBody\":{\"@id\":\"http://www.europeana.eu/schemas/epf/metadataTier0\"},\"oa:hasTarget\":{\"@id\":\"http://data.europeana.eu/aggregation/provider/1234/testing_order\"}},{\"@id\":\"http://data.europeana.eu/organization/1482250000004671126\",\"@type\":\"foaf:Organization\",\"skos:prefLabel\":{\"@language\":\"en\",\"@value\":\"Hispana\"}},{\"@id\":\"http://data.europeana.eu/proxy/europeana/1234/testing_order\",\"@type\":\"ore:Proxy\",\"dc:identifier\":\"urn:repox.ist.utl.pt:Otros:9982\",\"dc:language\":\"spa\",\"edm:europeanaProxy\":\"true\",\"edm:type\":\"IMAGE\",\"ore:lineage\":{\"@id\":\"http://data.europeana.eu/proxy/provider/1234/testing_order\"},\"ore:proxyFor\":{\"@id\":\"http://data.europeana.eu/item/1234/testing_order\"},\"ore:proxyIn\":{\"@id\":\"http://data.europeana.eu/aggregation/europeana/1234/testing_order\"}},{\"@id\":\"http://data.europeana.eu/proxy/provider/1234/testing_order\",\"@type\":\"ore:Proxy\",\"dc:contributor\":\"Patrimonio urbano\",\"dc:creator\":[\"An��bal GONZ��LEZ ��LVAREZ-OSSORIO\",\"Jos�� L��PEZ SALLABERRY\",\"Francisco del VILLAR CARMONA\"],\"dc:description\":[\"testing\"],\"dc:title\":\"Edificio ABC-Blanco y Negro\",\"dc:type\":\"IMAGE\",\"dcterms:created\":\"1896/01/01\",\"dcterms:issued\":\"2009/04/21\",\"edm:europeanaProxy\":\"false\",\"edm:type\":\"IMAGE\",\"ore:proxyFor\":{\"@id\":\"http://data.europeana.eu/item/1234/testing_order\"},\"ore:proxyIn\":{\"@id\":\"http://data.europeana.eu/aggregation/provider/1234/testing_order\"}},{\"@id\":\"http://www.testing/webresource/3.jpg\",\"@type\":[\"edm:FullTextResource\",\"edm:WebResource\"],\"ebucore:fileByteSize\":{\"@type\":\"http://www.w3.org/2001/XMLSchema#long\",\"@value\":\"14186814\"},\"ebucore:hasMimeType\":\"application/pdf\"},{\"@id\":\"http://www.testing/webresource/1.jpg\",\"@type\":\"edm:WebResource\",\"dcterms:isReferencedBy\":{\"@id\":\"https://iiif-acceptance.eanadev.org/presentation/1234/testing_order/manifest\"},\"ebucore:fileByteSize\":{\"@type\":\"http://www.w3.org/2001/XMLSchema#long\",\"@value\":\"118252\"},\"ebucore:hasMimeType\":\"image/jpeg\",\"ebucore:height\":800,\"ebucore:orientation\":\"portrait\",\"ebucore:width\":600,\"edm:componentColor\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#hexBinary\",\"@value\":\"#FFFFE0\"},{\"@type\":\"http://www.w3.org/2001/XMLSchema#hexBinary\",\"@value\":\"#191970\"},{\"@type\":\"http://www.w3.org/2001/XMLSchema#hexBinary\",\"@value\":\"#9370DB\"},{\"@type\":\"http://www.w3.org/2001/XMLSchema#hexBinary\",\"@value\":\"#FAEBD7\"},{\"@type\":\"http://www.w3.org/2001/XMLSchema#hexBinary\",\"@value\":\"#FFB6C1\"},{\"@type\":\"http://www.w3.org/2001/XMLSchema#hexBinary\",\"@value\":\"#8B4513\"}],\"edm:hasColorSpace\":\"sRGB\"},{\"@id\":\"http://www.testing/webresource/2.jpg\",\"@type\":\"edm:WebResource\",\"dc:format\":\"image/jpeg\",\"dc:rights\":\"Creative Commons - Reconocimiento-NoComercial 4.0 Internacional (CC BY-NC 4.0) - https://creativecommons.org/licenses/by-nc/4.0/deed.es_ES\",\"ebucore:fileByteSize\":{\"@type\":\"http://www.w3.org/2001/XMLSchema#long\",\"@value\":\"0\"},\"ebucore:hasMimeType\":\"application/xhtml+xml\",\"edm:rights\":{\"@id\":\"http://creativecommons.org/licenses/by-nc/4.0/\"}}],\"@context\":{\"rdf\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\",\"dc\":\"http://purl.org/dc/elements/1.1/\",\"dcterms\":\"http://purl.org/dc/terms/\",\"edm\":\"http://www.europeana.eu/schemas/edm/\",\"owl\":\"http://www.w3.org/2002/07/owl#\",\"wgs84_pos\":\"http://www.w3.org/2003/01/geo/wgs84_pos#\",\"skos\":\"http://www.w3.org/2004/02/skos/core#\",\"rdaGr2\":\"http://rdvocab.info/ElementsGr2/\",\"foaf\":\"http://xmlns.com/foaf/0.1/\",\"ebucore\":\"http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#\",\"doap\":\"http://usefulinc.com/ns/doap#\",\"odrl\":\"http://www.w3.org/ns/odrl/2/\",\"cc\":\"http://creativecommons.org/ns#\",\"ore\":\"http://www.openarchives.org/ore/terms/\",\"svcs\":\"http://rdfs.org/sioc/services#\",\"oa\":\"http://www.w3.org/ns/oa#\",\"dqv\":\"http://www.w3.org/ns/dqv#\"}}";

    List<String> orderOfWebresources = new ArrayList<>();

    @Before
    public void setup() {
        orderOfWebresources.add("http://www.testing/webresource/1.jpg");
        orderOfWebresources.add("http://www.testing/webresource/2.jpg");
        orderOfWebresources.add("http://www.testing/webresource/3.jpg"); // fulltext resource, multiple @type value
    }

    @Test
    public void testSortWebResources() throws JsonProcessingException {
        String orderedJsonLd = ModelUtils.sortWebResources(orderOfWebresources, unOrderedJsonLd);
        Assert.assertNotNull(orderedJsonLd);

        ObjectNode node = mapper.readValue(orderedJsonLd, ObjectNode.class);
        if (node.has(JSONLD_GRAPH)) {
            Iterator<JsonNode> iterator = node.get(JSONLD_GRAPH).iterator();
            int i=0;
            while (iterator.hasNext()) {
                JsonNode object = iterator.next();
                // check web resources id's order
                if (StringUtils.contains(object.get(JSONLD_TYPE).toString(), StringUtils.wrap(JSONLD_WR_RDF_TYPE, "\""))) {
                    Assert.assertEquals(StringUtils.wrap(orderOfWebresources.get(i), "\""), object.get(JSONLD_WR_RDF_ID).toString());
                    i++;
                }
            }
        }
    }

    @Test
    public void testSortWebResourcesFailScenario() {
        String orderedJsonLd = ModelUtils.sortWebResources(orderOfWebresources, "");
        Assert.assertNotNull(orderedJsonLd);

    }
}
