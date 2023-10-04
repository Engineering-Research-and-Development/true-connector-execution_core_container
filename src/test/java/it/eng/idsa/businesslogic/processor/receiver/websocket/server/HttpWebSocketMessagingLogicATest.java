package it.eng.idsa.businesslogic.processor.receiver.websocket.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.eng.idsa.businesslogic.configuration.WebSocketServerConfigurationA;

public class HttpWebSocketMessagingLogicATest {

	@InjectMocks
	private HttpWebSocketMessagingLogicA httpWebSocketMessagingLogicA;
	
	@Mock
	private WebSocketServerConfigurationA webSocketServerConfigurationA;
	
	@Mock
	private FrameBufferBean frameBufferBean;
	
	@Mock
	private ResponseMessageSendPartialServer responseMessageSendPartialServer;
	
	@Mock
	private Session session;
	
	@BeforeEach
	public void init () {
		MockitoAnnotations.openMocks(this);
		HttpWebSocketMessagingLogicA.getInstance().setWebSocketServerConfiguration(webSocketServerConfigurationA);
	}
	
	@AfterEach
	public void reset() {
		HttpWebSocketMessagingLogicA.getInstance().setWebSocketServerConfiguration(null);
		
	}
	
	@Test
	public void testOnMessage_setForwardTo() {
		HttpWebSocketMessagingLogicA.getInstance().onMessage(session, "Forward-To:example.com".getBytes());
		assertEquals( "example.com", HttpWebSocketMessagingLogicA.getInstance().getForwardTo());
	}
	
	@Test
	public void testOnMessage_lastFrame() throws UnsupportedEncodingException {
		when(webSocketServerConfigurationA.frameBufferWebSocket()).thenReturn(frameBufferBean);
		HttpWebSocketMessagingLogicA.getInstance().onMessage(session, InputStreamSocketListenerServer.CLOSURE_FRAME.getBytes("UTF-8"));
		verify(webSocketServerConfigurationA.frameBufferWebSocket(), times(0)).add(any());
	}
	
	@Test
	public void testOnMessage_receivedFrame() {
		when(webSocketServerConfigurationA.frameBufferWebSocket()).thenReturn(frameBufferBean);
		HttpWebSocketMessagingLogicA.getInstance().onMessage(session, "someFrame".getBytes());
		verify(webSocketServerConfigurationA.frameBufferWebSocket()).add(any());
	}
	
	@Test
	public void testOnMessage_endBinaryFrame() throws UnsupportedEncodingException {
		when(webSocketServerConfigurationA.frameBufferWebSocket()).thenReturn(frameBufferBean);
		responseMessageSendPartialServer.setWebSocketServerConfiguration(webSocketServerConfigurationA);
		ArgumentCaptor<Session> valueCapture = ArgumentCaptor.forClass(Session.class);
		doNothing().when(responseMessageSendPartialServer).setup(valueCapture.capture());
		when(webSocketServerConfigurationA.responseMessageSendPartialWebSocket()).thenReturn(responseMessageSendPartialServer);
		HttpWebSocketMessagingLogicA.getInstance().onMessage(session, InputStreamSocketListenerServer.END_BINARY_FRAME_SEPARATOR.getBytes("UTF-8"));
		verify(responseMessageSendPartialServer).setup(valueCapture.capture());
		assertEquals(session, valueCapture.getValue());
	}
}
