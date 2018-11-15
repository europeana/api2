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

package eu.europeana.api2.model.utils;

/**
 * @deprecated 2018-01-09 Seems like we don't use campaigncodes anymore, even though they are still present in some
 * links (e.g. search results item.guid)
 */
@Deprecated
public class LinkUtils {

	public static String addCampaignCodes(String url, String wskey) {
		StringBuilder s = new StringBuilder(url);
		s.append("?utm_source=api");
		s.append("&utm_medium=api");
        s.append("&utm_campaign=");
		s.append(wskey);
		return s.toString();
	}
}
