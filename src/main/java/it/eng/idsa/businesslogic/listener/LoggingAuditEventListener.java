package it.eng.idsa.businesslogic.listener;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.event.AbstractAuthorizationEvent;
import org.springframework.security.access.event.AuthorizationFailureEvent;
import org.springframework.security.access.event.AuthorizedEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.web.FilterInvocation;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.audit.EventTypeHandler;
import it.eng.idsa.businesslogic.audit.TrueConnectorEvent;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;

@Component
public class LoggingAuditEventListener {
	private static final Logger LOGGER = LoggerFactory.getLogger("JSON");

	private EventTypeHandler eventTypeHandler;

	public LoggingAuditEventListener(EventTypeHandler eventTypeHandler,
			@Value("${application.isReceiver}") boolean isConnectorReceiver) {
		this.eventTypeHandler = eventTypeHandler;
	}

//	@EventListener
//	@Async
	public void on(AuditApplicationEvent event) {
		if (StringUtils.isNotBlank(event.getAuditEvent().getType()) && !eventTypeHandler
				.shouldAuditEvent(TrueConnectorEventType.valueOf(event.getAuditEvent().getType()))) {
			return;
		}

		LOGGER.info("An Audit Event was received", keyValue("event", event.getAuditEvent()));

	}

	@EventListener
	@Async
	public void on(TrueConnectorEvent event) {
		if (StringUtils.isNotBlank(event.getAuditEvent().getType()) && !eventTypeHandler
				.shouldAuditEvent(TrueConnectorEventType.valueOf(event.getAuditEvent().getType()))) {
			return;
		}

		LOGGER.info("TrueConnector Audit Event was received", keyValue("event", event.getAuditEvent()));

	}

	@EventListener
	@Async
	public void on(AbstractAuthorizationEvent abstractEvent) {
		if (!eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.USER_AUTHORIZATION_SUCCESS,
				TrueConnectorEventType.USER_AUTHORIZATION_FAILURE)) {
			return;
		}
		if (abstractEvent instanceof AuthorizationFailureEvent) {
			AuthorizationFailureEvent event = (AuthorizationFailureEvent) abstractEvent;
			LOGGER.error("Failure authorization event was received: {}", keyValue("event", event.getSource()));

		}
		if (abstractEvent instanceof AuthorizedEvent) {
			AuthorizedEvent event = (AuthorizedEvent) abstractEvent;
			LOGGER.info("Succesfull autorization event was received: {}", keyValue("event", event.getSource()));

		}
		if (abstractEvent.getSource() instanceof FilterInvocation) {
			FilterInvocation filterInvocation = (FilterInvocation) abstractEvent.getSource();
			LOGGER.info("Filter invocation event was received: Filter: {}, Event:  {}",
					filterInvocation.getRequestUrl(), keyValue("event", abstractEvent.getSource()));
		}
	}

	@EventListener
	@Async
	public void on(AbstractAuthenticationEvent abstractEvent) {
		if (!eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.USER_AUTHENTICATION_SUCCESS,
				TrueConnectorEventType.USER_AUTHENTICATION_FAILURE)) {
			return;
		}
		if (abstractEvent instanceof AuthenticationFailureBadCredentialsEvent) {
			AuthenticationFailureBadCredentialsEvent event = (AuthenticationFailureBadCredentialsEvent) abstractEvent;
			LOGGER.error("Failure login event was received: {}", keyValue("event", event.getSource()));

		}
		if (abstractEvent instanceof AuthenticationSuccessEvent) {
			AuthenticationSuccessEvent event = (AuthenticationSuccessEvent) abstractEvent;
			LOGGER.info("Succesfull login event was received: {}", keyValue("event", event.getSource()));

		}
		if (abstractEvent.getSource() instanceof FilterInvocation) {
			FilterInvocation filterInvocation = (FilterInvocation) abstractEvent.getSource();
			LOGGER.info("Filter invocation event was received: Filter: {}, Event:  {}",
					filterInvocation.getRequestUrl(), keyValue("event", abstractEvent.getSource()));
		}
	}
}
