package it.eng.idsa.businesslogic.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.service.user.LoginAttemptService;

@Component
public class AuthenticationFailureListener implements ApplicationListener<AbstractAuthenticationFailureEvent> {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationFailureListener.class);

	@Autowired
	private LoginAttemptService loginAttemptService;

	@Override
	public void onApplicationEvent(AbstractAuthenticationFailureEvent event) {

		UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) event.getSource();

		logger.error("Failed login for user: " + token.getPrincipal() + ", reason: "
				+ event.getException().getLocalizedMessage());
		loginAttemptService.loginFailed(token.getPrincipal().toString());

	}
}
