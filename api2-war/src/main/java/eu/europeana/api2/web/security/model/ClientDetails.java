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
public class ClientDetails implements UserDetails {
    private static final long serialVersionUID = -925096405395777537L;

    private ApiKey apiKey;

    public ClientDetails(ApiKey apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.commaSeparatedStringToAuthorityList(apiKey.getLevel().getRoles());
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

}
