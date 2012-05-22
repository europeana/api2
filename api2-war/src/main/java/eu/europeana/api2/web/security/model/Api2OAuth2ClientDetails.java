package eu.europeana.api2.web.security.model;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.BaseClientDetails;

public class Api2OAuth2ClientDetails extends BaseClientDetails {
	private static final long serialVersionUID = -5687602758230210358L;

	public Api2OAuth2ClientDetails(String apikey, String secret) {
		super();
		setClientId(apikey);
		setClientSecret(secret);
		setAuthorities(AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_CLIENT"));
	}
	
	@Override
	public boolean isSecretRequired() {
		return true;
	}

}
