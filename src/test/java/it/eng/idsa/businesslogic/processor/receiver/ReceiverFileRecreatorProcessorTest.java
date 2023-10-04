package it.eng.idsa.businesslogic.processor.receiver;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.eng.idsa.businesslogic.configuration.WebSocketServerConfigurationB;
import it.eng.idsa.businesslogic.processor.receiver.websocket.server.FileRecreatorBeanServer;
import it.eng.idsa.businesslogic.processor.receiver.websocket.server.RecreatedMultipartMessageBean;

public class ReceiverFileRecreatorProcessorTest {
	
	@InjectMocks
	private ReceiverFileRecreatorProcessor processor;
	
	@Mock
	private Exchange exchange;
	@Mock
	private Message message;
	
	@Mock
	private WebSocketServerConfigurationB webSocketServerConfiguration;
	
	@Mock
	private FileRecreatorBeanServer fileRecreatorBean;
	@Mock
	private RecreatedMultipartMessageBean recreateBean;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		when(exchange.getMessage()).thenReturn(message);
	}
	
	@Test
	public void processSuccess() throws Exception {
		when(webSocketServerConfiguration.fileRecreatorBeanWebSocket()).thenReturn(fileRecreatorBean);
		when(webSocketServerConfiguration.recreatedMultipartMessageBeanWebSocket()).thenReturn(recreateBean);
		processor.process(exchange);
		
		verify(fileRecreatorBean).setup();
	}
}
