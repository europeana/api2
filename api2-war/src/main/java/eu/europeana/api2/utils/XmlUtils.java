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

package eu.europeana.api2.utils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

@Service
public class XmlUtils {

	@Resource(name = "api2_mvc_views_jaxbmarshaller")
	private Jaxb2Marshaller marshaller;

	public String toString(Object rss) {
		String xml = null;
		try {
			OutputStream baos = new ByteArrayOutputStream();
			marshaller.marshal(rss, new StreamResult(baos));
			xml = new String(((ByteArrayOutputStream) baos).toByteArray(), "UTF-8");
		} catch (XmlMappingException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return xml;
	}
}
