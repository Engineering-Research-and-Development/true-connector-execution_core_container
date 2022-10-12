package it.eng.idsa.businesslogic.listener;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
	// UUIDv4, matching either case, but depends on what format you want to use
	private static final Pattern UUID_PATTERN = Pattern.compile("([a-fA-F0-9]{8}(-[a-fA-F0-9]{4}){4}[a-fA-F0-9]{8})");

	private final ApplicationEventPublisher publisher;

	public CorrelationIdFilter(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (request.getRequestURI().contains("/api/")) {
			String correlationId = request.getHeader("correlation-id");
			if (null == correlationId || !UUID_PATTERN.matcher(correlationId).matches()) {
				correlationId = UUID.randomUUID().toString();
			}
			// make sure that the Mapped Diagnostic Context (MDC) has the 'correlationId' so
			// it can then
			// be populated in the logs
			try (MDC.MDCCloseable ignored = MDC.putCloseable("correlationId", correlationId)) {
				publisher.publishEvent(new TrueConnectordEvent(request, TrueConnectorEventType.HTTP_REQUEST_RECEIVED));
//				response.addHeader("correlation-id", correlationId);
				filterChain.doFilter(request, response);
			}
		} else {
			filterChain.doFilter(request, response);
		}
	}
}
