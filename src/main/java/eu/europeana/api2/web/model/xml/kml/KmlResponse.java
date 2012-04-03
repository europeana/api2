package eu.europeana.api2.web.model.xml.kml;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.europeana.corelib.definitions.solr.beans.BriefBean;

@XmlRootElement(name="kml")
public class KmlResponse {
	
	@XmlElement(name="Document")
	public Document document = new Document();

	public void setItems(List<BriefBean> results) {
		for (BriefBean bean : results) {
			Placemark placemark = new Placemark();
			placemark.name = bean.getTitle()[0];
			StringBuilder sb = new StringBuilder();
			sb.append(bean.getEdmPlaceLongitude()[0]);
			sb.append(",");
			sb.append(bean.getEdmPlaceLatitude()[0]);
			sb.append(",0");
			placemark.point.coordinates = sb.toString();
			document.placemarks.add(placemark);
		}
	}
	
}
