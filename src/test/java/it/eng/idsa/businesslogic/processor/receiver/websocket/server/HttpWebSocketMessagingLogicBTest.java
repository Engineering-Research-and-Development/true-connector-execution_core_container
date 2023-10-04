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

import it.eng.idsa.businesslogic.configuration.WebSocketServerConfigurationB;

public class HttpWebSocketMessagingLogicBTest {

	@InjectMocks
	private HttpWebSocketMessagingLogicB httpWebSocketMessagingLogicB;
	
	@Mock
	private WebSocketServerConfigurationB webSocketServerConfigurationB;
	
	@Mock
	private FrameBufferBean frameBufferBean;
	
	@Mock
	private ResponseMessageSendPartialServer responseMessageSendPartialServer;
	
	@Mock
	private Session session;
	
	@BeforeEach
	public void init () {
		MockitoAnnotations.openMocks(this);
		HttpWebSocketMessagingLogicB.getInstance().setWebSocketServerConfiguration(webSocketServerConfigurationB);
	}
	
	@AfterEach
	public void reset() {
		HttpWebSocketMessagingLogicB.getInstance().setWebSocketServerConfiguration(null);
		
	}
	
	@Test
	public void testOnMessage_lastFrame() throws UnsupportedEncodingException {
		when(webSocketServerConfigurationB.frameBufferWebSocket()).thenReturn(frameBufferBean);
		HttpWebSocketMessagingLogicB.getInstance().onMessage(session, InputStreamSocketListenerServer.CLOSURE_FRAME.getBytes("UTF-8"));
		verify(webSocketServerConfigurationB.frameBufferWebSocket(), times(0)).add(any());
	}
	
	@Test
	public void testOnMessage_endBinaryFrame() throws UnsupportedEncodingException {
		when(webSocketServerConfigurationB.frameBufferWebSocket()).thenReturn(frameBufferBean);
		responseMessageSendPartialServer.setWebSocketServerConfiguration(webSocketServerConfigurationB);
		ArgumentCaptor<Session> valueCapture = ArgumentCaptor.forClass(Session.class);
		doNothing().when(responseMessageSendPartialServer).setup(valueCapture.capture());
		when(webSocketServerConfigurationB.responseMessageSendPartialWebSocket()).thenReturn(responseMessageSendPartialServer);
		HttpWebSocketMessagingLogicB.getInstance().onMessage(session, InputStreamSocketListenerServer.END_BINARY_FRAME_SEPARATOR.getBytes("UTF-8"));
		verify(responseMessageSendPartialServer).setup(valueCapture.capture());
		assertEquals(session, valueCapture.getValue());
	}
}
