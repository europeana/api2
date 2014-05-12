package eu.europeana.api2.utils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.stream.StreamResult;

import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.servlet.ModelAndView;

public class XmlUtils {

	private Jaxb2Marshaller marshaller;

	public XmlUtils(Jaxb2Marshaller marshaller) {
		this.marshaller = marshaller;
	}

	public ModelAndView buildModelAndView(Object rss) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("rss", toString(rss));
		return new ModelAndView("rss", model);
	}

	public String toString(Object rss) {
		String xml = null;
		try {
			OutputStream baos = new ByteArrayOutputStream();
			marshaller.marshal(rss, new StreamResult(baos));
			xml = new String(((ByteArrayOutputStream) baos).toByteArray(), "UTF-8");
		} catch (XmlMappingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return xml;
	}
}
