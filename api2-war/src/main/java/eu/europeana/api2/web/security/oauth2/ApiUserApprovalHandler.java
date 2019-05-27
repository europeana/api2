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
