package eu.europeana.api2.v2.model.xml.rss;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import eu.europeana.corelib.definitions.EuropeanaStaticUrl;
import org.apache.commons.lang3.StringUtils;

import eu.europeana.api2.v2.model.xml.definitions.Namespaces;
import eu.europeana.api2.v2.model.xml.rss.atom.AtomLink;
import eu.europeana.api2.v2.model.xml.rss.opensearch.Query;
import eu.europeana.api2.v2.model.xml.rss.opensearch.Statistic;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@SuppressWarnings("unused")
public class Channel {

	private static final String DESCRIPTION_SUFFIX = " - Europeana Open Search";

	@XmlElement
	private String title = "Europeana Open Search";

	@XmlElement
	private String link = EuropeanaStaticUrl.EUROPEANA_PORTAL_URL;

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
