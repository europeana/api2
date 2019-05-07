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

import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

import static eu.europeana.corelib.db.util.UserUtils.hashPassword;

/**
 * @deprecated 2018-01-09 old MyEuropeana functionality
 */
@Deprecated
public class ClientDetails {//implements UserDetails {
//    private static final long serialVersionUID = -925096405395777537L;
//
//    private ApiKey apiKey;
//
//    public ClientDetails(ApiKey apiKey) {
//        this.apiKey = apiKey;
//    }
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return AuthorityUtils.commaSeparatedStringToAuthorityList(apiKey.getLevel().getRoles());
//    }
//
//    @Override
//    public String getPassword() {
//        return hashPassword(apiKey.getPrivateKey());
//    }
//
//    @Override
//    public String getUsername() {
//        return apiKey.getId();
//    }
//
//    @Override
//    public boolean isAccountNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isAccountNonLocked() {
//        return true;
//    }
//
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return true;
//    }

}
