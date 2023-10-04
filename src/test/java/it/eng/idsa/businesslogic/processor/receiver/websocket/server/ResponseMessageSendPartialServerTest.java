package it.eng.idsa.businesslogic.processor.receiver.websocket.server;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.eq;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.eng.idsa.businesslogic.configuration.WebSocketServerConfiguration;
import it.eng.idsa.multipart.util.UtilMessageService;

public class ResponseMessageSendPartialServerTest {

	@InjectMocks
	private ResponseMessageSendPartialServer responseMessageSendPartialServer; 
	
	@Mock
	private WebSocketServerConfiguration webSocketServerConfiguration;
	@Mock
	private ResponseMessageBufferBean responseMessageBuffer;
	
	@Mock
	private Session session;
	@Mock
	private RemoteEndpoint remote;
	
	byte[] responseMessage = "MESAGE".getBytes(StandardCharsets.UTF_8);
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		when(webSocketServerConfiguration.responseMessageBufferWebSocket()).thenReturn(responseMessageBuffer);
		when(session.getRemote()).thenReturn(remote);
	}
	
	@Test
	public void run() throws IOException {
		when(responseMessageBuffer.remove()).thenReturn(responseMessage);
		responseMessageSendPartialServer.run();
		
		verify(remote).sendPartialBytes(any(ByteBuffer.class), eq(true));
	}
	
	@Test
	public void run_extended() throws IOException {
		byte[] responseMessageLong = UtilMessageService.getMessageAsString(UtilMessageService.getArtifactRequestMessage()).getBytes(StandardCharsets.UTF_8);
		when(responseMessageBuffer.remove()).thenReturn(responseMessageLong);
		responseMessageSendPartialServer.run();
		
		verify(remote, atLeast(3)).sendPartialBytes(any(ByteBuffer.class), eq(false));
	}
}
