package it.eng.idsa.businesslogic.service.user;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class TokenAuthenticationFilterTest {

	private TokenAuthenticationFilter filter;

	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;
	@Mock
	private AuthenticationManager authenticationManager;
	@Mock
	Authentication auth;

	private String authorization;
	
	private RequestMatcher PROTECTED_URLS = new OrRequestMatcher(
			    new AntPathRequestMatcher("/api/**")
			  );
	
	@BeforeEach
	public void setup() {
		authorization = "Basic " + Base64.getEncoder().encodeToString("username:password".getBytes());
		MockitoAnnotations.initMocks(this);
		filter = new TokenAuthenticationFilter(PROTECTED_URLS);
		filter.setAuthenticationManager(authenticationManager);
		
		when(request.getHeader(AUTHORIZATION)).thenReturn(authorization);
	}
	
	@Test
	public void attemptAuthentication() throws AuthenticationException, IOException, ServletException {
		when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(auth);
		Authentication authenticated = filter.attemptAuthentication(request, response);
		assertNotNull(authenticated);
	}
}
