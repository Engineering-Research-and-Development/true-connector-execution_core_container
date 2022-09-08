package it.eng.idsa.businesslogic.processor.sender.websocket.client;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

import org.apache.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.multipart.util.UtilMessageService;

@Disabled
public class MessageWebSocketOverHttpSenderTest {

	@InjectMocks
	private MessageWebSocketOverHttpSender sender;

	@Mock
	private RejectionMessageService rejectionMessageService;
	@Mock
    private FileStreamingBean fileStreamingBean;
	@Mock
	private ResponseMessageBufferClient responseMessageBufferClient;
	@Mock
	private InputStreamSocketListenerClient inputStreamSocketListenerWebSocketClient;
	
	private String webSocketHost = "localhost";
	private Integer webSocketPort = 1234;
	private String header;
	private String payload = "PAYLOAD";
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		
		doNothing().when(responseMessageBufferClient).add(ArgumentMatchers.<byte[]>any());
		doNothing().when(inputStreamSocketListenerWebSocketClient).onBinaryFrame(ArgumentMatchers.<byte[]>any(), eq(false), eq(0));
		
		header = UtilMessageService.getMessageAsString(UtilMessageService.getArtifactRequestMessage());
	}
	
	@Test
	public void sendMultipartMessageWebSocketOverHttps1() throws KeyManagementException, ParseException, NoSuchAlgorithmException, IOException, InterruptedException, ExecutionException {
		sender.sendMultipartMessageWebSocketOverHttps(webSocketHost, webSocketPort, header, payload);
	}
	
}



