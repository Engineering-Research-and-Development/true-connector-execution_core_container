package it.eng.idsa.businesslogic.processor.sender.websocket.client;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

import org.asynchttpclient.ws.WebSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.eng.idsa.businesslogic.configuration.WebSocketClientConfiguration;
import it.eng.idsa.businesslogic.util.MultipartMessageUtil;

public class FileStreamingBeanTest {

	private static final String START_BINARY_FRAME_SEPARATOR = "�normal-IDS-ENG-SEPARATOR the-first-frame";

	private static final String END_BINARY_FRAME_SEPARATOR = "�normal-IDS-ENG-SEPARATOR the-last-frame";

	@InjectMocks
	private FileStreamingBean streamingBean;
	
	@Mock
	private WebSocketClientConfiguration webSocketClientConfiguration;
	@Mock
	private WebSocket webSocket;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		streamingBean.setup(webSocket);
	}
	
	@Test
	public void sendMultipartMessage() throws KeyManagementException, NoSuchAlgorithmException, InterruptedException, ExecutionException, IOException {
		when(webSocket.isOpen()).thenReturn(true);
		
		streamingBean.sendMultipartMessage(MultipartMessageUtil.getMultipartMessageAsString());
		
		verify(webSocket).sendBinaryFrame(START_BINARY_FRAME_SEPARATOR.getBytes(), false, 0);
		verify(webSocket).sendBinaryFrame(END_BINARY_FRAME_SEPARATOR.getBytes(), false, 0);
	}
}
