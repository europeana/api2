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

    private static final String URI_PROFILE = "/user/profile";

    private static final String URI_SAVEDITEM_GET = "/user/saveditem";

    private static final String URI_SAVEDITEM_CREATE = "/user/saveditem?europeanaid=";

    private static final String URI_SAVEDITEM_DELETE = "/user/saveditem?itemid=";

    private static final String URI_TAGS_GET = "/user/tag";

    private static final String URI_TAGS_TAGCLOUD = "/user/tag/cloud";

    private static final String URI_TAGS_DELETE = "/user/tag?tagid=";

    private static final String URI_SEARCHES_GET = "/user/savedsearch";

    private static final String URI_SEARCHES_DELETE = "/user/savedsearch?searchid=";

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
