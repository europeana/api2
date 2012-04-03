package eu.europeana.api2.web.model.xml.rss;

import javax.xml.bind.annotation.XmlElement;

import eu.europeana.api2.web.model.xml.rss.atom.AtomLink;


@SuppressWarnings("unused")
public class Channel {

	@XmlElement
	private String title = "Europeana Open Search result";
	
	@XmlElement
	private String link = "http://www.europeana.eu";
	
	@XmlElement(name="link", namespace = "http://www.w3.org/2005/Atom")
	private AtomLink atomLink = new AtomLink();

	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setLink(String link) {
		this.link = link;
	}

	public void setAtomLink(AtomLink atomLink) {
		this.atomLink = atomLink;
	}

}
