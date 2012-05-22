package eu.europeana.api2.web.security;

import javax.annotation.Resource;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import eu.europeana.api2.web.security.model.Api2UserDetails;
import eu.europeana.corelib.db.service.UserService;
import eu.europeana.corelib.definitions.db.entity.relational.User;

public class UserDetailsServiceImpl implements UserDetailsService {
	
	@Resource
	private UserService userService;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userService.findByEmail(username);
		if (user != null) {
			return new Api2UserDetails(user);
		}
		throw new UsernameNotFoundException(username);
	}

}
