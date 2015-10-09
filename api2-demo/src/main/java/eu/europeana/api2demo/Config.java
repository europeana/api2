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

package eu.europeana.api2demo;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

public class Config {

    @Value("${api2.url}")
    private String apiUrl;

    private static final String URI_PROFILE = "/v2/user/profile.json";

    private static final String URI_SAVEDITEM_GET = "/v2/user/saveditem.json";

    private static final String URI_SAVEDITEM_CREATE = "/v2/user/saveditem.json?action=CREATE&europeanaid=";

    private static final String URI_SAVEDITEM_DELETE = "/v2/user/saveditem.json?action=DELETE&itemid=";

    private static final String URI_TAGS_GET = "/v2/user/tag.json";

    private static final String URI_TAGS_TAGCLOUD = "/v2/user/tag.json?action=TAGCLOUD";

    private static final String URI_TAGS_CREATE = "/v2/user/tag.json?action=CREATE&europeanaid=";

    private static final String URI_TAGS_DELETE = "/v2/user/tag.json?action=DELETE&tagid=";

    private static final String URI_SEARCHES_GET = "/v2/user/savedsearch.json";

    private static final String URI_SEARCHES_DELETE = "/v2/user/savedsearch.json?action=DELETE&searchid=";

    public String getApiUrl() {
        return StringUtils.stripEnd(apiUrl, "/");
    }

    public String getUriProfile() {
        return getApiUrl() + URI_PROFILE;
    }

    public String getUriSavedItemGet() {
        return getApiUrl() + URI_SAVEDITEM_GET;
    }

    public String getUriSavedItemCreate() {
        return getApiUrl() + URI_SAVEDITEM_CREATE;
    }

    public String getUriSavedItemDelete() {
        return getApiUrl() + URI_SAVEDITEM_DELETE;
    }

    public String getUriTagsGet() {
        return getApiUrl() + URI_TAGS_GET;
    }

    public String getUriTagsTagcloud() {
        return getApiUrl() + URI_TAGS_TAGCLOUD;
    }

    public String getUriTagsCreate() {
        return getApiUrl() + URI_TAGS_CREATE;
    }

    public String getUriTagsDelete() {
        return getApiUrl() + URI_TAGS_DELETE;
    }

    public String getUriSearchesGet() {
        return getApiUrl() + URI_SEARCHES_GET;
    }

    public String getUriSearchesDelete() {
        return getApiUrl() + URI_SEARCHES_DELETE;
    }
}
