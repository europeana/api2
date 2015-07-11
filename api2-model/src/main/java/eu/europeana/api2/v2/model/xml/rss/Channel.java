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

package eu.europeana.api2.v2.model.xml.rss;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang.StringUtils;

import eu.europeana.api2.v2.model.xml.definitions.Namespaces;
import eu.europeana.api2.v2.model.xml.rss.atom.AtomLink;
import eu.europeana.api2.v2.model.xml.rss.opensearch.Query;
import eu.europeana.api2.v2.model.xml.rss.opensearch.Statistic;
import eu.europeana.corelib.web.service.EuropeanaUrlService;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@SuppressWarnings("unused")
public class Channel {

	private static final String DESCRIPTION_SUFFIX = " - Europeana Open Search";

	@XmlElement
	private String title = "Europeana Open Search";

	@XmlElement
	private String link = EuropeanaUrlService.URL_EUROPEANA;

	@XmlElement
	private String description = "Europeana Open Search results";

	@XmlElement(name = "totalResults", namespace = Namespaces.NS_OPENSEARCH)
	public Statistic totalResults = new Statistic();

	@XmlElement(name = "startIndex", namespace = Namespaces.NS_OPENSEARCH)
	public Statistic startIndex = new Statistic();

	@XmlElement(name = "itemsPerPage", namespace = Namespaces.NS_OPENSEARCH)
	public Statistic itemsPerPage = new Statistic();

	@XmlElement(name = "link", namespace = Namespaces.NS_ATOM)
	public AtomLink atomLink = new AtomLink();

	@XmlElement(name = "Query", namespace = Namespaces.NS_OPENSEARCH)
	public Query query = new Query();

	@XmlElement(name = "image")
	public ChannelImage image = new ChannelImage();

	@XmlElement(name = "item")
	public List<Item> items = new ArrayList<>();

	public void setTitle(String title) {
		this.title = title;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public void setAtomLink(AtomLink atomLink) {
		this.atomLink = atomLink;
	}

	public void updateDescription() {
		description = StringUtils.trim(query.searchTerms) + DESCRIPTION_SUFFIX;
	}
}
