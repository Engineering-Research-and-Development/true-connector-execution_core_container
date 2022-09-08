package it.eng.idsa.businesslogic.processor.receiver.websocket.server;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.eng.idsa.businesslogic.configuration.WebSocketServerConfiguration;

public class FileRecreatorBeanServerTest {
	
	private static final String END_BINARY_FRAME_SEPARATOR = "�normal-IDS-ENG-SEPARATOR the-last-frame";

	@InjectMocks
	private FileRecreatorBeanServer fileRecreatorBeanServer;
	
	@Mock
	private RecreatedMultipartMessageBean recreatedmultipartMessage;
	@Mock
	private WebSocketServerConfiguration webSocketServerConfiguration;
	@Mock
	private FrameBufferBean frameBufferBean;
	
	private String message = "MESSAGE";
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(frameBufferBean.remove()).thenReturn(message.getBytes(StandardCharsets.UTF_8));
		when(frameBufferBean.remove()).thenReturn(END_BINARY_FRAME_SEPARATOR.getBytes(StandardCharsets.UTF_8));
	}
	
	@Test
	public void run() {
		fileRecreatorBeanServer.run();
		
		verify(recreatedmultipartMessage).set(any(String.class));
	}
}
