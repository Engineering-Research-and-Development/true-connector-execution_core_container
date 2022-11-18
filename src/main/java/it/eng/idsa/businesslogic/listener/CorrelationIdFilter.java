package it.eng.idsa.businesslogic.listener;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import it.eng.idsa.businesslogic.util.TrueConnectorConstants;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
	// UUIDv4, matching either case, but depends on what format you want to use
	private static final Pattern UUID_PATTERN = Pattern.compile("([a-fA-F0-9]{8}(-[a-fA-F0-9]{4}){4}[a-fA-F0-9]{8})");

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (request.getRequestURI().contains("/api/")) {
			String correlationId = request.getHeader(TrueConnectorConstants.CORRELATION_ID);
			if (null == correlationId || !UUID_PATTERN.matcher(correlationId).matches()) {
				correlationId = UUID.randomUUID().toString();
			}
			try (MDC.MDCCloseable ignored = MDC.putCloseable("correlationId", correlationId)) {
				 response.addHeader(TrueConnectorConstants.CORRELATION_ID, correlationId);
				filterChain.doFilter(request, response);
			}
		} else {
			filterChain.doFilter(request, response);
		}
	}
}
