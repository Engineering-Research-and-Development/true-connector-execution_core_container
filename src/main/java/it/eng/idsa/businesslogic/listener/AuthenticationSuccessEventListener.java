package it.eng.idsa.businesslogic.listener;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.service.user.LoginAttemptService;
import it.eng.idsa.businesslogic.service.user.User;

@Component
public class AuthenticationSuccessEventListener implements ApplicationListener<AuthenticationSuccessEvent> {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationSuccessEventListener.class);
	
	@Autowired
    private HttpServletRequest request;
	 @Autowired
    private LoginAttemptService loginAttemptService;

	@Override
	public void onApplicationEvent(AuthenticationSuccessEvent event) {
		UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) event.getSource();
		User user = (User) token.getPrincipal();
		logger.info("Successful login for user: " + user.getUsername());		
		final String xfHeader = request.getHeader("X-Forwarded-For");
		if (xfHeader == null) {
            loginAttemptService.loginSucceeded(request.getRemoteAddr());
        } else {
            loginAttemptService.loginSucceeded(xfHeader.split(",")[0]);
        }
	}

}
