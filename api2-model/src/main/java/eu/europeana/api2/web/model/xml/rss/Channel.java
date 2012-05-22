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

package eu.europeana.api2.web.model.xml.rss;

import javax.xml.bind.annotation.XmlElement;

import eu.europeana.api2.web.model.xml.rss.atom.AtomLink;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
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
