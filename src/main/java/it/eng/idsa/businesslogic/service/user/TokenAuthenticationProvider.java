package it.eng.idsa.businesslogic.service.user;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class TokenAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

	private static final Logger logger = LoggerFactory.getLogger(TokenAuthenticationProvider.class);

	@Autowired
	private UserCrudService userService;
	
	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
		// nothing to do
	}

	@Override
	protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) {
	 	final Object token = authentication.getCredentials();
	 	Optional<User> user = userService.findByUsernameAndPassword(username, (String)token);
	 	if(user.isPresent()) {
	 		logger.debug("User '{}' found", username);
	 		return user.get();
	 	} 
	    throw new UsernameNotFoundException("Cannot find user with authentication token=" + token);
	}

}
