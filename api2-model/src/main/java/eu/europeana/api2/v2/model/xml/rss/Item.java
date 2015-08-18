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

package eu.europeana.api2.v2.model.xml.rss;

import javax.xml.bind.annotation.XmlElement;

import eu.europeana.api2.v2.model.xml.definitions.Namespaces;

@SuppressWarnings("unused")
public class Item {

	@XmlElement
	public String guid;

	@XmlElement
	public String title;

	@XmlElement
	public String link;

	@XmlElement
	public String description;

	@XmlElement(name = "enclosure")
	public Enclosure enclosure;

	@XmlElement(name = "creator", namespace = Namespaces.NS_DC)
	public String dcCreator;

	@XmlElement(name = "hasPart", namespace = Namespaces.NS_DCTERM)
	public String[] dcTermsHasPart;

	@XmlElement(name = "isPartOf", namespace = Namespaces.NS_DCTERM)
	public String[] dcTermsIsPartOf;

	@XmlElement(name = "year", namespace = Namespaces.NS_EUROPEANA)
	public String europeanaYear;

	@XmlElement(name = "language", namespace = Namespaces.NS_EUROPEANA)
	public String europeanaLanguage;

	@XmlElement(name = "type", namespace = Namespaces.NS_EUROPEANA)
	public String europeanaType;

	@XmlElement(name = "provider", namespace = Namespaces.NS_EUROPEANA)
	public String europeanaProvider;

	@XmlElement(name = "dataProvider", namespace = Namespaces.NS_EUROPEANA)
	public String europeanaDataProvider;

	@XmlElement(name = "rights", namespace = Namespaces.NS_EUROPEANA)
	public String[] europeanaRights;

	@XmlElement(name = "place_latitude", namespace = Namespaces.NS_ENRICHMENT)
	public Float enrichmentPlaceLatitude;

	@XmlElement(name = "place_longitude", namespace = Namespaces.NS_ENRICHMENT)
	public Float enrichmentPlaceLongitude;

	@XmlElement(name = "place_term", namespace = Namespaces.NS_ENRICHMENT)
	public String[] enrichmentPlaceTerm;

	@XmlElement(name = "place_label", namespace = Namespaces.NS_ENRICHMENT)
	public String[] enrichmentPlaceLabel;

	@XmlElement(name = "period_term", namespace = Namespaces.NS_ENRICHMENT)
	public String[] enrichmentPeriodTerm;

	@XmlElement(name = "period_label", namespace = Namespaces.NS_ENRICHMENT)
	public String[] enrichmentPeriodLabel;

	@XmlElement(name = "period_begin", namespace = Namespaces.NS_ENRICHMENT)
	public String enrichmentPeriodBegin;

	@XmlElement(name = "period_end", namespace = Namespaces.NS_ENRICHMENT)
	public String enrichmentPeriodEnd;

	@XmlElement(name = "agent_term", namespace = Namespaces.NS_ENRICHMENT)
	public String[] enrichmentAgentTerm;

	@XmlElement(name = "agent_label", namespace = Namespaces.NS_ENRICHMENT)
	public String[] enrichmentAgentLabel;

	@XmlElement(name = "concept_term", namespace = Namespaces.NS_ENRICHMENT)
	public String[] enrichmentConceptTerm;

	@XmlElement(name = "concept_label", namespace = Namespaces.NS_ENRICHMENT)
	public String[] enrichmentConceptLabel;
}
