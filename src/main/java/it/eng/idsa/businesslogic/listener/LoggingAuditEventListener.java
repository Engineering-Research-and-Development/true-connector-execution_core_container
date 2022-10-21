package it.eng.idsa.businesslogic.listener;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.event.AbstractAuthorizationEvent;
import org.springframework.security.access.event.AuthorizationFailureEvent;
import org.springframework.security.access.event.AuthorizedEvent;
import org.springframework.security.web.FilterInvocation;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.audit.EventTypeHandler;
import it.eng.idsa.businesslogic.audit.TrueConnectorEvent;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;

@Component
public class LoggingAuditEventListener {
	private static final Logger LOGGER = LoggerFactory.getLogger("JSON");
	
	private EventTypeHandler eventTypeHandler;
	
	private boolean isConnectorReceiver;
	
	public LoggingAuditEventListener(EventTypeHandler eventTypeHandler, @Value("${application.isReceiver}") boolean isConnectorReceiver) {
		this.eventTypeHandler = eventTypeHandler;
		this.isConnectorReceiver = isConnectorReceiver;
	}

//	@EventListener
//	@Async
	public void on(AuditApplicationEvent event) {
		if (StringUtils.isNotBlank(event.getAuditEvent().getType()) && !eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.valueOf(event.getAuditEvent().getType()))) {
			return;
		}
		Map<String, String> backup = MDC.getCopyOfContextMap();
		MDC.put("event.type", event.getAuditEvent().getType());
		MDC.put("event.principal", event.getAuditEvent().getPrincipal());
		MDC.put("connectorRole", isConnectorReceiver());

		LOGGER.info("An Audit Event was received", keyValue("event", event.getAuditEvent()));

		if (backup != null) {
			MDC.setContextMap(backup);
		}
	}
	
	private String isConnectorReceiver() {
		return isConnectorReceiver ? "Receiver" : "Sender";
	}

	@EventListener
	@Async
	public void on(TrueConnectorEvent event) {
		if (StringUtils.isNotBlank(event.getAuditEvent().getType()) && !eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.valueOf(event.getAuditEvent().getType()))) {
			return;
		}
		Map<String, String> backup = MDC.getCopyOfContextMap();
		MDC.put("event.type", event.getAuditEvent().getType());
		MDC.put("event.principal", event.getAuditEvent().getPrincipal());
//		MDC.put("correlationId", event.getCorrelationId());
		MDC.put("connectorRole", isConnectorReceiver());

		LOGGER.info("TrueConnector Audit Event was received", keyValue("event", event.getAuditEvent()));

		if (backup != null) {
			MDC.setContextMap(backup);
		}
	}

	@EventListener
	@Async
	public void on(AbstractAuthorizationEvent abstractEvent) {
		if (!eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.USER_AUTHORIZATION_SUCCESS, TrueConnectorEventType.USER_AUTHORIZATION_FAILURE)) {
			return;
		}
		Map<String, String> backup = MDC.getCopyOfContextMap();
		if (abstractEvent instanceof AuthorizationFailureEvent) {
			AuthorizationFailureEvent event = (AuthorizationFailureEvent) abstractEvent;
			MDC.put("event.principal", event.getAuthentication().getName());
			MDC.put("event.type", TrueConnectorEventType.USER_AUTHORIZATION_FAILURE.name());
		}
		if (abstractEvent instanceof AuthorizedEvent){
			AuthorizedEvent event = (AuthorizedEvent) abstractEvent;
			MDC.put("event.principal", event.getAuthentication().getName());
			MDC.put("event.type", TrueConnectorEventType.USER_AUTHORIZATION_SUCCESS.name());
		}
		FilterInvocation filterInvocation = (FilterInvocation) abstractEvent.getSource();
		MDC.put("source.requestUrl", filterInvocation.getRequestUrl());
		MDC.put("connectorRole", isConnectorReceiver());
		// and other checks for other subclasses
		LOGGER.info("An AuthorizationFailureEvent was received: {}", keyValue("event", abstractEvent.getSource()));

		if (backup != null) {
			MDC.setContextMap(backup);
		}
	}

}
