package eu.europeana.api2.web.security.oauth2;

import java.util.Collection;
import java.util.HashSet;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.approval.TokenStoreUserApprovalHandler;

public class ApiUserApprovalHandler extends TokenStoreUserApprovalHandler {

	private Collection<String> autoApproveClients = new HashSet<String>();

	private boolean useTokenServices = true;

	public void setAutoApproveClients(Collection<String> autoApproveClients) {
		this.autoApproveClients = autoApproveClients;
	}

	@Override
	public boolean isApproved(AuthorizationRequest authorizationRequest,
			Authentication userAuthentication) {
		if (useTokenServices
				&& super.isApproved(authorizationRequest, userAuthentication)) {
			return true;
		}
		if (!userAuthentication.isAuthenticated()) {
			return false;
		}
		return authorizationRequest.isApproved()
				|| (authorizationRequest.getResponseTypes().contains("token") && autoApproveClients
						.contains(authorizationRequest.getClientId()));
	}

}
