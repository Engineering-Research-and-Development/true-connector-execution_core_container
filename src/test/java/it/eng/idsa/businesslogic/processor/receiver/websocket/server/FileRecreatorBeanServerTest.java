package it.eng.idsa.businesslogic.processor.receiver.websocket.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.eng.idsa.businesslogic.configuration.WebSocketServerConfiguration;

public class FileRecreatorBeanServerTest {
	
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
		MockitoAnnotations.openMocks(this);
		when(frameBufferBean.remove()).thenReturn(message.getBytes(StandardCharsets.UTF_8));
		when(frameBufferBean.remove()).thenReturn(InputStreamSocketListenerServer.END_BINARY_FRAME_SEPARATOR.getBytes(StandardCharsets.UTF_8));
	}
	
	@Test
	public void run() {
		fileRecreatorBeanServer.run();
		
		verify(recreatedmultipartMessage).set(any(String.class));
	}
}
