package it.eng.idsa.businesslogic.service.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface TrueConnectorUserDetailsService extends UserDetailsService {

	UserDetails loadCamelUserByUsername(String username);
}
