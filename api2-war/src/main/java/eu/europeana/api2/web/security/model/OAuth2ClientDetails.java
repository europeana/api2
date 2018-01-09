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

package eu.europeana.api2.web.security.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.utils.StringArrayUtils;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

import java.util.Set;

/**
 * The client details
 * @deprecated 2018-01-09 old MyEuropeana functionality
 */
@Deprecated
public class OAuth2ClientDetails extends BaseClientDetails {
    private static final long serialVersionUID = -5687602758230210358L;

    /**
     * The grant types for which this client is authorized.
     * See http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-1.3
     */
    private Set<String> authGrantTypes = StringArrayUtils.toSet("authorization_code", "implicit");

    /**
     * The scope of this client.
     */
    private Set<String> scope = StringArrayUtils.toSet("read", "write");

    public OAuth2ClientDetails(ApiKey apiKey) {
        super();
        setClientId(apiKey.getId());
        setClientSecret(apiKey.getPrivateKey());
        setAuthorities(AuthorityUtils.commaSeparatedStringToAuthorityList(apiKey.getLevel().getRoles()));
    }

    @Override
    public boolean isSecretRequired() {
        return true;
    }

    @Override
    @JsonIgnore
    public Set<String> getAuthorizedGrantTypes() {
        return authGrantTypes;
    }

    @Override
    public Set<String> getScope() {
        return scope;
    }
}
