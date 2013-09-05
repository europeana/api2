package eu.europeana.api2.web.security.oauth2;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;

import eu.europeana.corelib.db.entity.nosql.AccessToken;
import eu.europeana.corelib.db.entity.nosql.RefreshToken;
import eu.europeana.corelib.db.service.OAuth2TokenService;

@Service
/**
 * Implemetation of oAuth TokenStore. Manages the persistency of access tokens
 */
public class ApiTokenStore implements TokenStore {

	@Resource
	private OAuth2TokenService oAuth2TokenService;

	private AuthenticationKeyGenerator authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();

	@Scheduled(cron="@daily")
	public void cleanExpiredTokens() {
		oAuth2TokenService.cleanExpiredTokens();
	}

	@Override
	public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
		return readAuthentication(token.getValue());
	}

	@Override
	public OAuth2Authentication readAuthentication(String token) {
		// in SQL it would be "SELECT token_id, authentication FROM oauth_access_token WHERE token_id = ?";
		OAuth2Authentication authentication = null;
		AccessToken entity = oAuth2TokenService.findAccessTokenByID(extractTokenKey(token));
		if (entity != null) {
			authentication = deserializeAuthentication(entity.getAuthentication());
		}
		return authentication;
	}

	@Override
	public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
		// "insert into oauth_access_token (token_id, token, authentication_id, user_name, client_id, authentication, refresh_token) values (?, ?, ?, ?, ?, ?, ?)";
		String refreshToken = null;
		if (token.getRefreshToken() != null) {
			refreshToken = token.getRefreshToken().getValue();
		}
		AccessToken entity = new AccessToken(extractTokenKey(token.getValue()), token.getExpiration());
		entity.setToken(serializeAccessToken(token));
		entity.setAuthenticationId(authenticationKeyGenerator.extractKey(authentication));
		entity.setUserName(authentication.isClientOnly() ? null : authentication.getName());
		entity.setClientId(authentication.getAuthorizationRequest().getClientId());
		entity.setAuthentication(serializeAuthentication(authentication));
		entity.setRefreshToken(extractTokenKey(refreshToken));
		oAuth2TokenService.store(entity);
	}

	@Override
	public OAuth2AccessToken readAccessToken(String tokenValue) {
		// "select token_id, token from oauth_access_token where token_id = ?";
		OAuth2AccessToken accessToken = null;
		AccessToken entity = oAuth2TokenService.findAccessTokenByID(extractTokenKey(tokenValue));
		if (entity != null) {
			accessToken = deserializeAccessToken(entity.getToken());
		}
		return accessToken;
	}

	@Override
	public void removeAccessToken(OAuth2AccessToken token) {
		removeAccessToken(token.getValue());
	}

	public void removeAccessToken(String tokenValue) {
		// "delete from oauth_access_token where token_id = ?";
		oAuth2TokenService.removeAccessToken(extractTokenKey(tokenValue));
	}

	@Override
	public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {
		// "insert into oauth_refresh_token (token_id, token, authentication) values (?, ?, ?)";
		RefreshToken entity = new RefreshToken(extractTokenKey(extractTokenKey(refreshToken.getValue())));
		entity.setToken(serializeRefreshToken(refreshToken));
		entity.setAuthentication(serializeAuthentication(authentication));
		oAuth2TokenService.store(entity);
	}

	@Override
	public OAuth2RefreshToken readRefreshToken(String tokenValue) {
		// "select token_id, token from oauth_refresh_token where token_id = ?";
		OAuth2RefreshToken refreshToken = null;
		RefreshToken entity = oAuth2TokenService.findRefreshTokenByID(extractTokenKey(tokenValue));
		if (entity != null) {
			refreshToken = deserializeRefreshToken(entity.getToken());
		}
		return refreshToken;
	}

	@Override
	public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken token) {
		return readAuthenticationForRefreshToken(token.getValue());
	}

	public OAuth2Authentication readAuthenticationForRefreshToken(String tokenValue) {
		//"select token_id, authentication from oauth_refresh_token where token_id = ?";
		OAuth2Authentication authentication = null;
		RefreshToken entity = oAuth2TokenService.findRefreshTokenByID(extractTokenKey(tokenValue));
		if (entity != null) {
			authentication = deserializeAuthentication(entity.getAuthentication());
		}
		return authentication;
	}
	
	@Override
	public void removeRefreshToken(OAuth2RefreshToken token) {
		removeRefreshToken(token.getValue());
	}

	public void removeRefreshToken(String tokenValue) {
		// "delete from oauth_refresh_token where token_id = ?";
		oAuth2TokenService.removeRefreshToken(extractTokenKey(tokenValue));
	}

	@Override
	public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
		removeAccessTokenUsingRefreshToken(refreshToken.getValue());
	}

	public void removeAccessTokenUsingRefreshToken(String refreshToken) {
		// "delete from oauth_access_token where refresh_token = ?"
		oAuth2TokenService.removeAccessTokenByRefreshTokenId(extractTokenKey(refreshToken));
	}

	@Override
	public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
		// "select token_id, token from oauth_access_token where authentication_id = ?";
		OAuth2AccessToken accessToken = null;

		String key = authenticationKeyGenerator.extractKey(authentication);
		AccessToken entity = oAuth2TokenService.findAccessTokenByAuthenticationId(key);
		if (entity != null) {
			accessToken = deserializeAccessToken(entity.getToken());
			if (accessToken != null
					&& !key.equals(authenticationKeyGenerator.extractKey(readAuthentication(accessToken.getValue())))) {
				removeAccessToken(accessToken.getValue());
				// Keep the store consistent (maybe the same user is represented by this authentication but the details
				// have changed)
				storeAccessToken(accessToken, authentication);
			}
		}
		return accessToken;
	}

	@Override
	public Collection<OAuth2AccessToken> findTokensByUserName(String userName) {
		// "select token_id, token from oauth_access_token where user_name = ?";
		List<OAuth2AccessToken> accessTokens = new ArrayList<OAuth2AccessToken>();
		List<AccessToken> entities = oAuth2TokenService.findByUserName(userName);
		if (entities != null) {
			for (RefreshToken entity : entities) {
				accessTokens.add(deserializeAccessToken(entity.getToken()));
			}
		}
		return removeNulls(accessTokens);
	}

	@Override
	public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
		// "select token_id, token from oauth_access_token where client_id = ?";
		List<OAuth2AccessToken> accessTokens = new ArrayList<OAuth2AccessToken>();
		List<AccessToken> entities = oAuth2TokenService.findByClientId(clientId);
		if (entities != null) {
			for (RefreshToken entity : entities) {
				accessTokens.add(deserializeAccessToken(entity.getToken()));
			}
		}
		return removeNulls(accessTokens);
	}

	private List<OAuth2AccessToken> removeNulls(List<OAuth2AccessToken> accessTokens) {
		List<OAuth2AccessToken> tokens = new ArrayList<OAuth2AccessToken>();
		for (OAuth2AccessToken token : accessTokens) {
			if (token != null) {
				tokens.add(token);
			}
		}
		return tokens;
	}

	private byte[] serializeAccessToken(OAuth2AccessToken token) {
		return SerializationUtils.serialize(token);
	}

	private byte[] serializeRefreshToken(OAuth2RefreshToken token) {
		return SerializationUtils.serialize(token);
	}

	private byte[] serializeAuthentication(OAuth2Authentication authentication) {
		return SerializationUtils.serialize(authentication);
	}

	private OAuth2AccessToken deserializeAccessToken(byte[] token) {
		return SerializationUtils.deserialize(token);
	}

	private OAuth2RefreshToken deserializeRefreshToken(byte[] token) {
		return SerializationUtils.deserialize(token);
	}

	private OAuth2Authentication deserializeAuthentication(byte[] authentication) {
		return SerializationUtils.deserialize(authentication);
	}

	private String extractTokenKey(String value) {
		if (value == null) {
			return null;
		}
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).");
		}

		try {
			byte[] bytes = digest.digest(value.getBytes("UTF-8"));
			return String.format("%032x", new BigInteger(1, bytes));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("UTF-8 encoding not available.  Fatal (should be in the JDK).");
		}
	}

}
