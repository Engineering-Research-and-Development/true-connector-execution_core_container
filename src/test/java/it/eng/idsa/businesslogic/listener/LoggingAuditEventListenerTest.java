package it.eng.idsa.businesslogic.listener;

import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.event.AuthorizationFailureEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

import it.eng.idsa.businesslogic.audit.TrueConnectorEvent;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.UtilMessageService;

public class LoggingAuditEventListenerTest {

	@InjectMocks
	private LoggingAuditEventListener listener;
	
	@Mock
	private MultipartMessage multipartMessage;
	@Mock
	private Authentication authentication;
	@Mock
	private FilterInvocation filterInvocation;

	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void onTrueConnectorEvent() {
		when(multipartMessage.getHeaderContent()).thenReturn(UtilMessageService.getArtifactRequestMessage());
		TrueConnectorEvent tcEvent = new TrueConnectorEvent(TrueConnectorEventType.CONNECTOR, multipartMessage);
		listener.on(tcEvent);
	}
	
	@Test
	public void onAbstractAuthorizationEventEvent() {
		Collection<ConfigAttribute> attributes = new HashSet<>();
		
		when(multipartMessage.getHeaderContent()).thenReturn(UtilMessageService.getArtifactRequestMessage());
		AuthorizationFailureEvent event = new AuthorizationFailureEvent(filterInvocation, attributes, authentication,
				new AccessDeniedException("Access denied"));
		when(authentication.getName()).thenReturn("user");
		when(filterInvocation.getRequestUrl()).thenReturn("/some/url/test");
		
		listener.on(event);
	}
}
