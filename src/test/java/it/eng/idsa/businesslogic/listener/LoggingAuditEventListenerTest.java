package it.eng.idsa.businesslogic.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.event.AuthorizationFailureEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

import it.eng.idsa.businesslogic.audit.EventTypeHandler;
import it.eng.idsa.businesslogic.audit.TrueConnectorEvent;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;
import it.eng.idsa.businesslogic.service.AuditEventService;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.UtilMessageService;

public class LoggingAuditEventListenerTest {

	private LoggingAuditEventListener listener;

	private MultipartMessage multipartMessage;

	private Authentication authentication;

	private FilterInvocation filterInvocation;

	private EventTypeHandler eventTypeHandler;

	private AuditEventService auditEventService;

	@BeforeEach
	public void init() {
		eventTypeHandler = mock(EventTypeHandler.class);
		authentication = mock(Authentication.class);
		auditEventService = mock(AuditEventService.class);
		filterInvocation = mock(FilterInvocation.class);
		listener = new LoggingAuditEventListener(eventTypeHandler, auditEventService);
		when(eventTypeHandler.shouldAuditEvent(any())).thenReturn(true);
	}

	@Test
	public void onTrueConnectorEvent() {
		multipartMessage = new MultipartMessage(null, null, UtilMessageService.getArtifactRequestMessage(), null, null,
				null, null, null);
		TrueConnectorEvent tcEvent = new TrueConnectorEvent(TrueConnectorEventType.CONNECTOR, multipartMessage);
		listener.on(tcEvent);
	}

	@Test
	public void onAbstractAuthorizationEventEvent() {
		Collection<ConfigAttribute> attributes = new HashSet<>();

		AuthorizationFailureEvent event = new AuthorizationFailureEvent(filterInvocation, attributes, authentication,
				new AccessDeniedException("Access denied"));
		when(authentication.getName()).thenReturn("user");
		when(filterInvocation.getRequestUrl()).thenReturn("/some/url/test");

		listener.on(event);
	}
}
