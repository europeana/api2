package eu.europeana.api2.v2.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
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
    private static final String JSONLD_AGGREGATION_RDF_TYPE    = "ore:Aggregation";
    private static final String JSONLD_EDM_HAS_VIEW    = "edm:hasView";
    private static final String JSONLD_RDF_ID    = "@id";

    private static final String unOrderedJsonLd = "{\"@graph\":[{\"@id\":\"http://data.europeana.eu/aggregation/europeana/xyz/testing\",\"@type\":\"edm:EuropeanaAggregation\",\"dcterms:created\":\"2019-10-07T08:41:56.951Z\",\"dcterms:modified\":\"2020-04-24T08:44:59.533Z\",\"edm:aggregatedCHO\":{\"@id\":\"http://data.europeana.eu/item/xyz/testing\"},\"edm:completeness\":\"10\",\"edm:country\":\"Europe\",\"edm:datasetName\":\"142_Europeana_TestRecord\",\"edm:landingPage\":{\"@id\":\"https://www.europeana.eu/item/xyz/testing\"},\"edm:language\":\"mul\",\"edm:preview\":{\"@id\":\"https://api.europeana.eu/thumbnail/v2/url.json?uri=http%3A%2F%2Fwww.mimo-db.eu%2Fmedia%2FUEDIN%2FIMAGE%2F0032195c.jpg&type=IMAGE\"},\"dqv:hasQualityAnnotation\":[{\"@id\":\"http://data.europeana.eu/item/xyz/testing#contentTier\"},{\"@id\":\"http://data.europeana.eu/item/xyz/testing#metadataTier\"}]},{\"@id\":\"http://data.europeana.eu/aggregation/provider/xyz/testing\",\"@type\":\"ore:Aggregation\",\"dc:rights\":[{\"@language\":\"fr\",\"@value\":\"Droit d'auteur de l'Université d'Edimbourg\"},\"Copyright by the University of Edinburgh\"],\"edm:aggregatedCHO\":{\"@id\":\"http://data.europeana.eu/item/xyz/testing\"},\"edm:dataProvider\":{\"@id\":\"http://data.europeana.eu/organization/1482250000000366875\"},\"edm:hasView\":[{\"@id\":\"https://www.dropbox.com/s/kyp9fez3bk63vsp/video_2.mp3?raw=1\"},{\"@id\":\"https://www.dropbox.com/s/37rizaac03nun92/image_1.jpg?raw=1\"},{\"@id\":\"https://www.dropbox.com/s/70azamahxn5xvg4/text_1.pdf?raw=1\"},{\"@id\":\"https://www.dropbox.com/s/hcgjtystcx8428g/image_2.jpg?raw=1\"},{\"@id\":\"https://www.dropbox.com/s/vybleydthqq4j02/image_4.jpg?raw=1\"},{\"@id\":\"https://www.dropbox.com/s/hahuekxp46wrb24/image_3.jpg?raw=1\"},{\"@id\":\"https://www.dropbox.com/s/tv4ndrnqgxki29q/video_1.mpg?raw=1\"}],\"edm:intermediateProvider\":[{\"@id\":\"http://data.europeana.eu/organisations/12345\"},{\"@id\":\"http://data.europeana.eu/organization/1482250000046783052\"}],\"edm:isShownAt\":{\"@id\":\"http://www.mimo-db.eu/UEDIN/214\"},\"edm:isShownBy\":{\"@id\":\"https://www.dropbox.com/s/7y26z6gbloiy1oc/image_5.jpg?raw=1\"},\"edm:object\":{\"@id\":\"http://www.mimo-db.eu/media/UEDIN/IMAGE/0032195c.jpg\"},\"edm:provider\":{\"@id\":\"http://data.europeana.eu/organization/1482250000004671140\"},\"edm:rights\":{\"@id\":\"http://rightsstatements.org/vocab/InC-OW-EU/1.0/\"},\"edm:ugc\":\"true\"},{\"@id\":\"http://data.europeana.eu/aggregation/europeana/xyz/testing\",\"@type\":\"edm:EuropeanaAggregation\",\"dcterms:created\":\"2019-10-07T08:41:56.951Z\",\"dcterms:modified\":\"2020-04-24T08:44:59.533Z\",\"edm:aggregatedCHO\":{\"@id\":\"http://data.europeana.eu/item/xyz/testing\"},\"edm:completeness\":\"10\",\"edm:country\":\"Europe\",\"edm:datasetName\":\"142_Europeana_TestRecord\",\"edm:landingPage\":{\"@id\":\"https://www.europeana.eu/item/xyz/testing\"},\"edm:language\":\"mul\",\"edm:preview\":{\"@id\":\"https://api.europeana.eu/thumbnail/v2/url.json?uri=http%3A%2F%2Fwww.mimo-db.eu%2Fmedia%2FUEDIN%2FIMAGE%2F0032195c.jpg&type=IMAGE\"},\"dqv:hasQualityAnnotation\":[{\"@id\":\"http://data.europeana.eu/item/xyz/testing#contentTier\"},{\"@id\":\"http://data.europeana.eu/item/xyz/testing#metadataTier\"}]}],\"@context\":{\"rdf\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\",\"dc\":\"http://purl.org/dc/elements/1.1/\",\"dcterms\":\"http://purl.org/dc/terms/\",\"edm\":\"http://www.europeana.eu/schemas/edm/\",\"owl\":\"http://www.w3.org/2002/07/owl#\",\"wgs84_pos\":\"http://www.w3.org/2003/01/geo/wgs84_pos#\",\"skos\":\"http://www.w3.org/2004/02/skos/core#\",\"rdaGr2\":\"http://rdvocab.info/ElementsGr2/\",\"foaf\":\"http://xmlns.com/foaf/0.1/\",\"ebucore\":\"http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#\",\"doap\":\"http://usefulinc.com/ns/doap#\",\"odrl\":\"http://www.w3.org/ns/odrl/2/\",\"cc\":\"http://creativecommons.org/ns#\",\"ore\":\"http://www.openarchives.org/ore/terms/\",\"svcs\":\"http://rdfs.org/sioc/services#\",\"oa\":\"http://www.w3.org/ns/oa#\",\"dqv\":\"http://www.w3.org/ns/dqv#\"}}";
    private static final String jsonLdWithNoHasView = "{\"@graph\":[{\"@id\":\"http://data.europeana.eu/aggregation/europeana/abc/testing\",\"@type\":\"edm:EuropeanaAggregation\",\"dcterms:created\":\"2017-06-06T19:40:18.082Z\",\"dcterms:modified\":\"2022-06-14T10:47:16.116Z\",\"edm:aggregatedCHO\":{\"@id\":\"http://data.europeana.eu/item/abc/testing\"},\"edm:completeness\":\"10\",\"edm:country\":\"Spain\",\"edm:datasetName\":\"2022711_Ag_ES_Hispana_bdmadrid\",\"edm:landingPage\":{\"@id\":\"https://www.europeana.eu/item/abc/testing\"},\"edm:language\":\"es\",\"edm:preview\":{\"@id\":\"https://api.europeana.eu/thumbnail/v2/url.json?uri=http%3A%2F%2Fwww.memoriademadrid.es%2Fdoc_anexos%2FWorkflow%2F0%2F9982%2Fmadf2006901.jpg&type=IMAGE\"},\"dqv:hasQualityAnnotation\":[{\"@id\":\"http://data.europeana.eu/item/abc/testing#contentTier\"},{\"@id\":\"http://data.europeana.eu/item/abc/testing#metadataTier\"}]},{\"@id\":\"http://data.europeana.eu/aggregation/provider/abc/testing\",\"@type\":\"ore:Aggregation\",\"edm:aggregatedCHO\":{\"@id\":\"http://data.europeana.eu/item/abc/testing\"},\"edm:dataProvider\":\"Biblioteca Digital Memoriademadrid\",\"edm:isShownAt\":{\"@id\":\"http://www.memoriademadrid.es/buscador.php?accion=VerFicha&id=9982\"},\"edm:isShownBy\":{\"@id\":\"http://www.memoriademadrid.es/doc_anexos/Workflow/0/9982/madf2006901.jpg\"},\"edm:object\":{\"@id\":\"http://www.memoriademadrid.es/doc_anexos/Workflow/0/9982/madf2006901.jpg\"},\"edm:provider\":{\"@id\":\"http://data.europeana.eu/organization/1482250000004671126\"},\"edm:rights\":{\"@id\":\"http://creativecommons.org/licenses/by-nc/4.0/\"}},{\"@id\":\"http://data.europeana.eu/item/abc/testing\",\"@type\":\"edm:ProvidedCHO\"},{\"@id\":\"http://data.europeana.eu/item/abc/testing#contentTier\",\"@type\":\"dqv:QualityAnnotation\",\"dcterms:created\":\"2022-06-14T13:52:24.726566Z\",\"oa:hasBody\":{\"@id\":\"http://www.europeana.eu/schemas/epf/contentTier2\"},\"oa:hasTarget\":{\"@id\":\"http://data.europeana.eu/aggregation/provider/abc/testing\"}},{\"@id\":\"http://data.europeana.eu/item/abc/testing#metadataTier\",\"@type\":\"dqv:QualityAnnotation\",\"dcterms:created\":\"2022-06-14T13:52:24.726979Z\",\"oa:hasBody\":{\"@id\":\"http://www.europeana.eu/schemas/epf/metadataTier0\"},\"oa:hasTarget\":{\"@id\":\"http://data.europeana.eu/aggregation/provider/abc/testing\"}},{\"@id\":\"http://data.europeana.eu/organization/1482250000004671126\",\"@type\":\"foaf:Organization\",\"skos:prefLabel\":{\"@language\":\"en\",\"@value\":\"Hispana\"}},{\"@id\":\"http://data.europeana.eu/proxy/europeana/abc/testing\",\"@type\":\"ore:Proxy\",\"dc:identifier\":\"urn:repox.ist.utl.pt:Otros:9982\",\"dc:language\":\"spa\",\"edm:europeanaProxy\":\"true\",\"edm:type\":\"IMAGE\",\"ore:lineage\":{\"@id\":\"http://data.europeana.eu/proxy/provider/abc/testing\"},\"ore:proxyFor\":{\"@id\":\"http://data.europeana.eu/item/abc/testing\"},\"ore:proxyIn\":{\"@id\":\"http://data.europeana.eu/aggregation/europeana/abc/testing\"}},{\"@id\":\"http://data.europeana.eu/proxy/provider/abc/testing\",\"@type\":\"ore:Proxy\",\"dc:contributor\":\"Patrimonio urbano\",\"dc:creator\":[\"Aníbal GONZÁLEZ ÁLVAREZ-OSSORIO\",\"José LÓPEZ SALLABERRY\",\"Francisco del VILLAR CARMONA\"],\"dc:description\":[\"Se trata en realidad de un conjunto formado por varios edificios: el construido por López Sallaberry en 1899 para Blanco y Negro, con fachada neoplateresca a la calle de Serrano; la ampliación del mismo mediante una nave de imprentas que llegaba hasta el paseo de la Castellana, efectuada en 1904 -un año antes del lanzamiento del diario ABC- por Francisco del Villar Carmona, que aplica un interesante sistema de bóvedas tabicadas patentado por Madurell Hermanos; el cuerpo de fachada que da al propio paseo, que proyectó Aníbal González en clave regionalista sevillana y construyó Anasagasti en 1926, y el nuevo edificio de talleres y oficinas en el interior de la manzana, proyectado por este último hacia 1932, según modelo racionalista en ladrillo que recuerda a los edificios de la escuela de Chicago. La cerámica exterior proviene del taller de Daniel Zuloaga y el conjunto de vidrieras se realizaron por Maumejean. Tras el cambio de sede de la editorial Prensa Española se abandonan los edificios y se convierte todo el conjunto en un centro comercial proyectado por Mariano Bayón, que abre circulaciones entre Serrano y la Castellana y sustituye la bóveda de la nave de imprentas por otra de hormigón atirantado con azotea superior transitable, transformando además las medianeras de esta misma nave en dos fachadas de vidrio de composición horizontal.\",\"Calle Serrano, 61 Actual Centro Comercial ABC Serrano Es importante la fachada al paseo de la Castellana de estilo regionalista sevillano, en la que destacan el empleo de ladrillo combinado con cerámica en forma de azulejo y el escudo principal que recuerda el antiguo uso del edifico como sede del diario ABC.\"],\"dc:identifier\":\"urn:repox.ist.utl.pt:Otros:9982\",\"dc:language\":\"spa\",\"dc:publisher\":\"Ayuntamiento de Madrid\",\"dc:source\":\"s/sig\",\"dc:subject\":[\"Calle de Serrano\",\"Francisco del Villar Carmona\",\"José López Sallaberry\",\"Aníbal González Álvarez-Ossorio\",\"Edificios\",\"Paseo de la Castellana\",\"Edificio ABC-Blanco y Negro\",\"Daniel Zuloaga Boneta\"],\"dc:title\":\"Edificio ABC-Blanco y Negro\",\"dc:type\":\"IMAGE\",\"dcterms:created\":\"1896/01/01\",\"dcterms:issued\":\"2009/04/21\",\"edm:europeanaProxy\":\"false\",\"edm:type\":\"IMAGE\",\"ore:proxyFor\":{\"@id\":\"http://data.europeana.eu/item/abc/testing\"},\"ore:proxyIn\":{\"@id\":\"http://data.europeana.eu/aggregation/provider/abc/testing\"}},{\"@id\":\"http://www.memoriademadrid.es/buscador.php?accion=VerFicha&id=9982\",\"@type\":\"edm:WebResource\",\"dc:format\":\"image/jpeg\",\"dc:rights\":\"Creative Commons - Reconocimiento-NoComercial 4.0 Internacional (CC BY-NC 4.0) - https://creativecommons.org/licenses/by-nc/4.0/deed.es_ES\",\"ebucore:fileByteSize\":{\"@type\":\"http://www.w3.org/2001/XMLSchema#long\",\"@value\":\"0\"},\"ebucore:hasMimeType\":\"application/xhtml+xml\",\"edm:rights\":{\"@id\":\"http://creativecommons.org/licenses/by-nc/4.0/\"}},{\"@id\":\"http://www.memoriademadrid.es/doc_anexos/Workflow/0/9982/madf2006901.jpg\",\"@type\":\"edm:WebResource\",\"dcterms:isReferencedBy\":{\"@id\":\"https://iiif.europeana.eu/presentation/abc/testing/manifest\"},\"ebucore:fileByteSize\":{\"@type\":\"http://www.w3.org/2001/XMLSchema#long\",\"@value\":\"118252\"},\"ebucore:hasMimeType\":\"image/jpeg\",\"ebucore:height\":800,\"ebucore:orientation\":\"portrait\",\"ebucore:width\":600,\"edm:componentColor\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#hexBinary\",\"@value\":\"#FFFFE0\"},{\"@type\":\"http://www.w3.org/2001/XMLSchema#hexBinary\",\"@value\":\"#191970\"},{\"@type\":\"http://www.w3.org/2001/XMLSchema#hexBinary\",\"@value\":\"#9370DB\"},{\"@type\":\"http://www.w3.org/2001/XMLSchema#hexBinary\",\"@value\":\"#FAEBD7\"},{\"@type\":\"http://www.w3.org/2001/XMLSchema#hexBinary\",\"@value\":\"#FFB6C1\"},{\"@type\":\"http://www.w3.org/2001/XMLSchema#hexBinary\",\"@value\":\"#8B4513\"}],\"edm:hasColorSpace\":\"sRGB\"}],\"@context\":{\"rdf\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\",\"dc\":\"http://purl.org/dc/elements/1.1/\",\"dcterms\":\"http://purl.org/dc/terms/\",\"edm\":\"http://www.europeana.eu/schemas/edm/\",\"owl\":\"http://www.w3.org/2002/07/owl#\",\"wgs84_pos\":\"http://www.w3.org/2003/01/geo/wgs84_pos#\",\"skos\":\"http://www.w3.org/2004/02/skos/core#\",\"rdaGr2\":\"http://rdvocab.info/ElementsGr2/\",\"foaf\":\"http://xmlns.com/foaf/0.1/\",\"ebucore\":\"http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#\",\"doap\":\"http://usefulinc.com/ns/doap#\",\"odrl\":\"http://www.w3.org/ns/odrl/2/\",\"cc\":\"http://creativecommons.org/ns#\",\"ore\":\"http://www.openarchives.org/ore/terms/\",\"svcs\":\"http://rdfs.org/sioc/services#\",\"oa\":\"http://www.w3.org/ns/oa#\",\"dqv\":\"http://www.w3.org/ns/dqv#\"}}";
    private static final String jsonLdWithOneHasView = "{\"@graph\":[{\"@id\":\"http://data.europeana.eu/aggregation/europeana/1234/testing\",\"@type\":\"edm:EuropeanaAggregation\",\"dcterms:created\":\"2014-09-25T21:23:29.326Z\",\"dcterms:modified\":\"2018-06-25T14:34:18.877Z\",\"edm:aggregatedCHO\":{\"@id\":\"http://data.europeana.eu/item/1234/testing\"},\"edm:completeness\":\"7\",\"edm:country\":\"France\",\"edm:datasetName\":\"9200365_Ag_EU_TEL_a0142_Gallica\",\"edm:landingPage\":{\"@id\":\"https://www.europeana.eu/item/1234/testing\"},\"edm:language\":\"fr\",\"edm:preview\":{\"@id\":\"https://api.europeana.eu/thumbnail/v2/url.json?uri=http%3A%2F%2Fgallica.bnf.fr%2Fark%3A%2F12148%2Fbpt6k5559719w.thumbnail.jpg&type=TEXT\"},\"edm:rights\":{\"@id\":\"http://rightsstatements.org/vocab/NoC-OKLR/1.0/\"},\"dqv:hasQualityAnnotation\":[{\"@id\":\"http://data.europeana.eu/item/1234/testing#contentTier\"},{\"@id\":\"http://data.europeana.eu/item/1234/testing#metadataTier\"}]},{\"@id\":\"http://data.europeana.eu/aggregation/provider/1234/testing\",\"@type\":\"ore:Aggregation\",\"edm:aggregatedCHO\":{\"@id\":\"http://data.europeana.eu/item/1234/testing\"},\"edm:dataProvider\":{\"@id\":\"http://data.europeana.eu/organization/1482250000002112001\"},\"edm:hasView\":{\"@id\":\"http://gallica.bnf.fr/ark:/12148/bpt6k5559719w/f1.zoom\"},\"edm:isShownAt\":{\"@id\":\"http://gallica.bnf.fr/ark:/12148/bpt6k5559719w\"},\"edm:object\":{\"@id\":\"http://gallica.bnf.fr/ark:/12148/bpt6k5559719w.thumbnail.jpg\"},\"edm:provider\":{\"@id\":\"http://data.europeana.eu/organization/1482250000004516062\"},\"edm:rights\":{\"@id\":\"http://rightsstatements.org/vocab/NoC-OKLR/1.0/\"}},{\"@id\":\"http://data.europeana.eu/item/1234/testing\",\"@type\":\"edm:ProvidedCHO\"},{\"@id\":\"http://data.europeana.eu/item/1234/testing#contentTier\",\"@type\":\"dqv:QualityAnnotation\",\"dcterms:created\":\"2019-07-16T11:46:27.863Z\",\"oa:hasBody\":{\"@id\":\"http://www.europeana.eu/schemas/epf/contentTier1\"},\"oa:hasTarget\":{\"@id\":\"http://data.europeana.eu/aggregation/provider/1234/testing\"}},{\"@id\":\"http://data.europeana.eu/item/1234/testing#metadataTier\",\"@type\":\"dqv:QualityAnnotation\",\"dcterms:created\":\"2019-07-16T11:46:27.864Z\",\"oa:hasBody\":{\"@id\":\"http://www.europeana.eu/schemas/epf/metadataTier0\"},\"oa:hasTarget\":{\"@id\":\"http://data.europeana.eu/aggregation/provider/1234/testing\"}},{\"@id\":\"http://data.europeana.eu/organization/1482250000002112001\",\"@type\":\"foaf:Organization\",\"skos:prefLabel\":[{\"@language\":\"ca\",\"@value\":\"Biblioteca Nacional de França\"},{\"@language\":\"pl\",\"@value\":\"Francuska Biblioteka Narodowa\"},{\"@language\":\"fi\",\"@value\":\"Ranskan kansalliskirjasto\"},{\"@language\":\"hu\",\"@value\":\"Francia Nemzeti Könyvtár\"},{\"@language\":\"sl\",\"@value\":\"Francoska narodna knjižnica\"},{\"@language\":\"ga\",\"@value\":\"Leabharlann Náisiúnta na Fraince\"},{\"@language\":\"cs\",\"@value\":\"Francouzská národní knihovna\"},{\"@language\":\"lv\",\"@value\":\"Francijas Nacionālā bibliotēka\"},{\"@language\":\"el\",\"@value\":\"Εθνική Βιβλιοθήκη της Γαλλίας\"},{\"@language\":\"pt\",\"@value\":\"Biblioteca Nacional da França\"},{\"@language\":\"bg\",\"@value\":\"Национална библиотека на Франция\"},{\"@language\":\"nl\",\"@value\":\"Bibliothèque nationale de France\"},{\"@language\":\"ro\",\"@value\":\"Bibliothèque nationale de France\"},{\"@language\":\"da\",\"@value\":\"Bibliothèque nationale de France\"},{\"@language\":\"sk\",\"@value\":\"Bibliothèque nationale de France\"},{\"@language\":\"es\",\"@value\":\"Biblioteca Nacional de Francia\"},{\"@language\":\"fr\",\"@value\":\"Bibliothèque nationale de France\"},{\"@language\":\"it\",\"@value\":\"Bibliothèque nationale de France\"},{\"@language\":\"en\",\"@value\":\"National Library of France\"},{\"@language\":\"hr\",\"@value\":\"Nacionalna knjižnica Francuske\"},{\"@language\":\"sv\",\"@value\":\"Bibliothèque nationale de France\"},{\"@language\":\"ru\",\"@value\":\"Национальная библиотека Франции\"},{\"@language\":\"de\",\"@value\":\"Bibliothèque nationale de France\"}]},{\"@id\":\"http://data.europeana.eu/organization/1482250000004516062\",\"@type\":\"foaf:Organization\",\"skos:prefLabel\":{\"@language\":\"en\",\"@value\":\"The European Library\"}},{\"@id\":\"http://data.europeana.eu/proxy/europeana/1234/testing\",\"@type\":\"ore:Proxy\",\"edm:europeanaProxy\":\"true\",\"edm:type\":\"TEXT\",\"ore:proxyFor\":{\"@id\":\"http://data.europeana.eu/item/1234/testing\"},\"ore:proxyIn\":{\"@id\":\"http://data.europeana.eu/aggregation/europeana/1234/testing\"}},{\"@id\":\"http://data.europeana.eu/proxy/provider/1234/testing\",\"@type\":\"ore:Proxy\",\"dc:date\":\"1900-07-14\",\"dc:description\":\"1900/07/14 (N13).\",\"dc:identifier\":\"http://gallica.bnf.fr/ark:/12148/bpt6k5559719w\",\"dc:language\":\"fr\",\"dc:publisher\":\"[s.n.] (Paris)\",\"dc:relation\":[\"Notice du catalogue : http://catalogue.bnf.fr/ark:/12148/cb32680338v\",{\"@id\":\"http://gallica.bnf.fr/ark:/12148/cb32680338v/date\"}],\"dc:rights\":{\"@id\":\"http://rightsstatements.org/page/NoC-OKLR/1.0/?relatedURL=http://gallica.bnf.fr/html/conditions-dutilisation-des-contenus-de-gallica\"},\"dc:source\":\"Bibliothèque nationale de France, département Philosophie, histoire, sciences de l'homme, FOL-LC2-5906\",\"dc:title\":\"A bas les tyrans : journal antimaçonnique / dir. Copin-Albancelli ; dir. Louis Dasté\",\"dc:type\":[{\"@language\":\"fr\",\"@value\":\"publication en série imprimée\"},{\"@language\":\"fr\",\"@value\":\"texte\"},{\"@language\":\"en\",\"@value\":\"text\"},{\"@language\":\"en\",\"@value\":\"printed serial\"}],\"dcterms:isPartOf\":{\"@id\":\"http://data.theeuropeanlibrary.org/Collection/a0142\"},\"edm:europeanaProxy\":\"false\",\"edm:type\":\"TEXT\",\"ore:proxyFor\":{\"@id\":\"http://data.europeana.eu/item/1234/testing\"},\"ore:proxyIn\":{\"@id\":\"http://data.europeana.eu/aggregation/provider/1234/testing\"}},{\"@id\":\"http://gallica.bnf.fr/ark:/12148/bpt6k5559719w\",\"@type\":[\"edm:FullTextResource\",\"edm:WebResource\"],\"ebucore:fileByteSize\":{\"@type\":\"http://www.w3.org/2001/XMLSchema#long\",\"@value\":\"186368\"},\"ebucore:hasMimeType\":\"text/html\"},{\"@id\":\"http://gallica.bnf.fr/ark:/12148/bpt6k5559719w.thumbnail.jpg\",\"@type\":\"edm:WebResource\",\"ebucore:fileByteSize\":{\"@type\":\"http://www.w3.org/2001/XMLSchema#long\",\"@value\":\"17227\"},\"ebucore:hasMimeType\":\"image/png\",\"ebucore:height\":192,\"ebucore:orientation\":\"portrait\",\"ebucore:width\":127,\"edm:componentColor\":[{\"@type\":\"http://www.w3.org/2001/XMLSchema#hexBinary\",\"@value\":\"#000000\"},{\"@type\":\"http://www.w3.org/2001/XMLSchema#hexBinary\",\"@value\":\"#696969\"},{\"@type\":\"http://www.w3.org/2001/XMLSchema#hexBinary\",\"@value\":\"#A9A9A9\"},{\"@type\":\"http://www.w3.org/2001/XMLSchema#hexBinary\",\"@value\":\"#FFFFFF\"},{\"@type\":\"http://www.w3.org/2001/XMLSchema#hexBinary\",\"@value\":\"#2F4F4F\"},{\"@type\":\"http://www.w3.org/2001/XMLSchema#hexBinary\",\"@value\":\"#F5F5F5\"}]},{\"@id\":\"http://gallica.bnf.fr/ark:/12148/bpt6k5559719w/f1.zoom\",\"@type\":[\"edm:FullTextResource\",\"edm:WebResource\"],\"dcterms:isReferencedBy\":{\"@id\":\"https://iiif.europeana.eu/presentation/1234/testing/manifest\"},\"ebucore:fileByteSize\":{\"@type\":\"http://www.w3.org/2001/XMLSchema#long\",\"@value\":\"187694\"},\"ebucore:hasMimeType\":\"text/html\"}],\"@context\":{\"rdf\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\",\"dc\":\"http://purl.org/dc/elements/1.1/\",\"dcterms\":\"http://purl.org/dc/terms/\",\"edm\":\"http://www.europeana.eu/schemas/edm/\",\"owl\":\"http://www.w3.org/2002/07/owl#\",\"wgs84_pos\":\"http://www.w3.org/2003/01/geo/wgs84_pos#\",\"skos\":\"http://www.w3.org/2004/02/skos/core#\",\"rdaGr2\":\"http://rdvocab.info/ElementsGr2/\",\"foaf\":\"http://xmlns.com/foaf/0.1/\",\"ebucore\":\"http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#\",\"doap\":\"http://usefulinc.com/ns/doap#\",\"odrl\":\"http://www.w3.org/ns/odrl/2/\",\"cc\":\"http://creativecommons.org/ns#\",\"ore\":\"http://www.openarchives.org/ore/terms/\",\"svcs\":\"http://rdfs.org/sioc/services#\",\"oa\":\"http://www.w3.org/ns/oa#\",\"dqv\":\"http://www.w3.org/ns/dqv#\"}}";
    private FullBean bean;
    List<String> hasViewOrder = new ArrayList<>();

    @Before
    public void setup() {
        bean = MockFullBean.mock();

        hasViewOrder.add("https://www.dropbox.com/s/tv4ndrnqgxki29q/video_1.mpg?raw=1");
        hasViewOrder.add("https://www.dropbox.com/s/kyp9fez3bk63vsp/video_2.mp3?raw=1");
        hasViewOrder.add("https://www.dropbox.com/s/37rizaac03nun92/image_1.jpg?raw=1");
        hasViewOrder.add("https://www.dropbox.com/s/hahuekxp46wrb24/image_3.jpg?raw=1");
        hasViewOrder.add("https://www.dropbox.com/s/vybleydthqq4j02/image_4.jpg?raw=1");
        hasViewOrder.add("https://www.dropbox.com/s/hcgjtystcx8428g/image_2.jpg?raw=1");
        hasViewOrder.add("https://www.dropbox.com/s/70azamahxn5xvg4/text_1.pdf?raw=1");

        bean.getAggregations().get(0).setHasView(hasViewOrder.toArray(new String[0]));
    }

    @Test
    public void testSortHasView() throws JsonProcessingException {
        String orderedJsonLd = ModelUtils.sortHasViews(bean, unOrderedJsonLd);
        Assert.assertNotNull(orderedJsonLd);

        // check hasView id's order
        ObjectNode node = mapper.readValue(orderedJsonLd, ObjectNode.class);
        if (node.has(JSONLD_GRAPH)) {
            Iterator<JsonNode> iterator = node.get(JSONLD_GRAPH).iterator();
            int i=0;
            while (iterator.hasNext()) {
                JsonNode jsonNode = iterator.next();
                if (StringUtils.contains(jsonNode.get(JSONLD_TYPE).toString(), StringUtils.wrap(JSONLD_AGGREGATION_RDF_TYPE, "\""))) {
                    Iterator<JsonNode> hasViewIterator = jsonNode.get(JSONLD_EDM_HAS_VIEW).iterator();
                    while (hasViewIterator.hasNext()) {
                        Assert.assertEquals(StringUtils.wrap(hasViewOrder.get(i), "\""), hasViewIterator.next().get(JSONLD_RDF_ID).toString());
                        i++;
                    }
                    break;
                }
            }
        }
    }

    @Test
    public void testSortHasView_withNoHasView() {
        bean.getAggregations().get(0).setHasView(null);
        String orderedJsonLd = ModelUtils.sortHasViews(bean, jsonLdWithNoHasView);
        Assert.assertNotNull(orderedJsonLd);
        Assert.assertEquals(jsonLdWithNoHasView, orderedJsonLd);
    }

    @Test
    public void testSortHasView_withOneHasView() {
        bean.getAggregations().get(0).setHasView(new String[] {"http://gallica.bnf.fr/ark:/12148/bpt6k5559719w/f1.zoom"});
        String orderedJsonLd = ModelUtils.sortHasViews(bean, jsonLdWithOneHasView);
        Assert.assertNotNull(orderedJsonLd);
        Assert.assertEquals(jsonLdWithOneHasView, orderedJsonLd);
    }

    @Test
    public void testSortWebResourcesFailScenario() {
        String orderedJsonLd = ModelUtils.sortHasViews(bean, "");
        Assert.assertNotNull(orderedJsonLd);

    }
}