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

package eu.europeana.api2.web.security.oauth2;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.approval.TokenStoreUserApprovalHandler;

import java.util.Collection;
import java.util.HashSet;

public class ApiUserApprovalHandler extends TokenStoreUserApprovalHandler {

    private Collection<String> autoApproveClients = new HashSet<>();

    public void setAutoApproveClients(Collection<String> autoApproveClients) {
        this.autoApproveClients = autoApproveClients;
    }

    @Override
    public boolean isApproved(AuthorizationRequest authorizationRequest,
                              Authentication userAuthentication) {
        return super.isApproved(authorizationRequest, userAuthentication)
                || userAuthentication.isAuthenticated()
                && (authorizationRequest.isApproved() || (
                    authorizationRequest.getResponseTypes().contains("token")
                        && autoApproveClients.contains(authorizationRequest.getClientId())));
    }

}
