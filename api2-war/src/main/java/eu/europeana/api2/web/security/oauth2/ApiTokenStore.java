package eu.europeana.api2.web.security.oauth2;

import java.util.Collection;

import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;

public class ApiTokenStore implements TokenStore {

	@Override
	public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OAuth2Authentication readAuthentication(String token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void storeAccessToken(OAuth2AccessToken token,
			OAuth2Authentication authentication) {
		// TODO Auto-generated method stub

	}

	@Override
	public OAuth2AccessToken readAccessToken(String tokenValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeAccessToken(OAuth2AccessToken token) {
		// TODO Auto-generated method stub

	}

	@Override
	public void storeRefreshToken(OAuth2RefreshToken refreshToken,
			OAuth2Authentication authentication) {
		// TODO Auto-generated method stub

	}

	@Override
	public OAuth2RefreshToken readRefreshToken(String tokenValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OAuth2Authentication readAuthenticationForRefreshToken(
			OAuth2RefreshToken token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeRefreshToken(OAuth2RefreshToken token) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAccessTokenUsingRefreshToken(
			OAuth2RefreshToken refreshToken) {
		// TODO Auto-generated method stub

	}

	@Override
	public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<OAuth2AccessToken> findTokensByUserName(String userName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
		// TODO Auto-generated method stub
		return null;
	}

}
