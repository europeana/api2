package eu.europeana.api2.web.security.model;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;

public class Api2MyDataDetails implements UserDetails {
	private static final long serialVersionUID = -925096405395777537L;

	private ApiKey apiKey;

	public Api2MyDataDetails(ApiKey apiKey) {
		this.apiKey = apiKey;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return AuthorityUtils
				.commaSeparatedStringToAuthorityList("ROLE_MYDATA");
	}

	@Override
	public String getPassword() {
		return hashPassword(apiKey.getPrivateKey());
	}

	@Override
	public String getUsername() {
		return apiKey.getId();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	/**
	 * Hashing password using ShaPasswordEncoder.
	 * 
	 * @param password
	 *            The password in initial form.
	 * @return Hashed password as to be stored in database
	 */
	private String hashPassword(String password) {
		if (StringUtils.isNotBlank(password)) {
			return new ShaPasswordEncoder().encodePassword(password, null);
		}
		return null;
	}

}
