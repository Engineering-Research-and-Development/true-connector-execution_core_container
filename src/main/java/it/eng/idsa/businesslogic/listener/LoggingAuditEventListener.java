package it.eng.idsa.businesslogic.listener;

import org.apache.commons.lang3.StringUtils;
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
import it.eng.idsa.businesslogic.entity.AuditLog;
import it.eng.idsa.businesslogic.service.AuditEventService;

@Component
public class LoggingAuditEventListener {

	private EventTypeHandler eventTypeHandler;
	private AuditEventService auditEventService;

	public LoggingAuditEventListener(EventTypeHandler eventTypeHandler, AuditEventService auditEventService) {
		this.eventTypeHandler = eventTypeHandler;
		this.auditEventService = auditEventService;
	}

//	@EventListener
//	@Async
	public void on(AuditApplicationEvent event) {
		if (StringUtils.isNotBlank(event.getAuditEvent().getType()) && !eventTypeHandler
				.shouldAuditEvent(TrueConnectorEventType.valueOf(event.getAuditEvent().getType()))) {
			return;
		}

		auditEventService.saveAuditEvent(new AuditLog(event.getAuditEvent().toString()));
	}

	@EventListener
	@Async
	public void on(TrueConnectorEvent event) {
		if (StringUtils.isNotBlank(event.getAuditEvent().getType()) && !eventTypeHandler
				.shouldAuditEvent(TrueConnectorEventType.valueOf(event.getAuditEvent().getType()))) {
			return;
		}

		auditEventService.saveAuditEvent(new AuditLog(event.getAuditEvent().toString()));
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
			auditEventService.saveAuditEvent(new AuditLog(event.getSource().toString()));
		}
		if (abstractEvent instanceof AuthorizedEvent) {
			AuthorizedEvent event = (AuthorizedEvent) abstractEvent;

			auditEventService.saveAuditEvent(new AuditLog(event.getSource().toString()));
		}
		if (abstractEvent.getSource() instanceof FilterInvocation) {
			FilterInvocation filterInvocation = (FilterInvocation) abstractEvent.getSource();
			String auditEventDetails = createAuditLogEntryWithFiler(abstractEvent.getSource(), filterInvocation);

			auditEventService.saveAuditEvent(new AuditLog(auditEventDetails));
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

			auditEventService.saveAuditEvent(new AuditLog(event.getSource().toString()));
		}
		if (abstractEvent instanceof AuthenticationSuccessEvent) {
			AuthenticationSuccessEvent event = (AuthenticationSuccessEvent) abstractEvent;

			auditEventService.saveAuditEvent(new AuditLog(event.getSource().toString()));
		}
		if (abstractEvent.getSource() instanceof FilterInvocation) {
			FilterInvocation filterInvocation = (FilterInvocation) abstractEvent.getSource();
			String auditEventDetails = createAuditLogEntryWithFiler(abstractEvent.getSource(), filterInvocation);

			auditEventService.saveAuditEvent(new AuditLog(auditEventDetails));
		}
	}

	private String createAuditLogEntryWithFiler(Object eventSource, FilterInvocation filterInvocation) {
		String eventDetails = eventSource.toString();
		String requestUrl = filterInvocation.getRequestUrl();

		return "Event: " + eventDetails + ", Filter: Requested URL=[" + requestUrl + "]";
	}
}
