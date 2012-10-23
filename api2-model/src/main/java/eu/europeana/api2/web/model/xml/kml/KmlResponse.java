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

package eu.europeana.api2.web.model.xml.kml;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.europeana.corelib.definitions.solr.beans.BriefBean;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@XmlRootElement(name="kml")
public class KmlResponse {

	@XmlElement(name="Document")
	public Document document = new Document();

	public void setItems(List<BriefBean> results) {
		for (BriefBean bean : results) {
			Placemark placemark = new Placemark();
			placemark.name = bean.getTitle()[0];
			StringBuilder sb = new StringBuilder();
			sb.append(bean.getEdmPlaceLongitude());
			sb.append(",");
			sb.append(bean.getEdmPlaceLatitude());
			sb.append(",0");
			placemark.point.coordinates = sb.toString();
			document.placemarks.add(placemark);
		}
	}
}
