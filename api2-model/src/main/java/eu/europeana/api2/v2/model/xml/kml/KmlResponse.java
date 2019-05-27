package eu.europeana.api2.v2.model.xml.kml;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.europeana.corelib.definitions.edm.beans.BriefBean;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 * @deprecated 2018-01-09 search with coordinates functionality
 */
@XmlRootElement(name = "kml")
@Deprecated
public class KmlResponse {

	@XmlElement(name = "Document")
	public Document document = new Document();

	public void setItems(List<BriefBean> results) {
		for (BriefBean bean : results) {
			Placemark placemark = new Placemark();
			placemark.name = bean.getTitle()[0];
			placemark.point.coordinates = String.valueOf(bean.getEdmPlaceLongitude()) + "," + bean.getEdmPlaceLatitude() + ",0";
			document.placemarks.add(placemark);
		}
	}
}
