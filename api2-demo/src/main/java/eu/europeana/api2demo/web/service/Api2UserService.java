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

package eu.europeana.api2demo.web.service;

import eu.europeana.api2demo.web.model.*;

public interface Api2UserService {

    // PROFILE
    UserProfile getProfile();

    // SAVED ITEMS
    UserSavedItems getSavedItems();

    boolean createSavedItem(String id);

    void deleteSavedItem(Long id);

    // TAGS
    UserTags getTags(String tag);

    TagCloud createTagCloud();

    boolean createTag(String id, String tag);

    void deleteTag(Long id);

    // SAVED SEARCHES
    UserSearches getSavedSearches();

    void deleteSavedSearch(Long id);

}
