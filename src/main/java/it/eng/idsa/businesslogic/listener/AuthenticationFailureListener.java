package it.eng.idsa.businesslogic.listener;

import javax.servlet.http.HttpServletRequest;

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
	private HttpServletRequest request;
	@Autowired
    private LoginAttemptService loginAttemptService;

	@Override
	public void onApplicationEvent(AbstractAuthenticationFailureEvent event) {
		logger.error(
				"Failed login for user: " + ((UsernamePasswordAuthenticationToken) event.getSource()).getPrincipal()
						+ ", reason: " + event.getException().getLocalizedMessage());
		final String xfHeader = request.getHeader("X-Forwarded-For");
		if (xfHeader == null) {
            loginAttemptService.loginFailed(request.getRemoteAddr());
        } else {
            loginAttemptService.loginFailed(xfHeader.split(",")[0]);
        }
	}

}
