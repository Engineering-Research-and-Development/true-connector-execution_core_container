package it.eng.idsa.businesslogic.service.user;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.removeStart;

import java.io.IOException;
import java.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class TokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
	
	private static final String BASIC = "Basic";

	public TokenAuthenticationFilter(final RequestMatcher requiresAuth) {
		super(requiresAuth);
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException, ServletException {
		final String param = ofNullable(request.getHeader(AUTHORIZATION))
				.orElse(request.getParameter("t"));

		final String token = ofNullable(param)
				.map(value -> removeStart(value, BASIC))
				.map(String::trim)
				.orElseThrow(() -> new BadCredentialsException("Missing Authentication Token"));
		String credentials = new String(Base64.getDecoder().decode(token));
		final Authentication auth = new UsernamePasswordAuthenticationToken(credentials.split(":")[0], credentials.split(":")[1]);
		return getAuthenticationManager().authenticate(auth);
	}
}
