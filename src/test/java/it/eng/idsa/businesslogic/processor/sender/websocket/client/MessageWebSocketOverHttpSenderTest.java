package it.eng.idsa.businesslogic.processor.sender.websocket.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

import org.apache.http.ParseException;
import org.asynchttpclient.ws.WebSocket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.processor.receiver.websocket.server.HttpWebSocketMessagingServletA;
import it.eng.idsa.businesslogic.processor.receiver.websocket.server.HttpWebSocketServerBean;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.multipart.util.UtilMessageService;

public class MessageWebSocketOverHttpSenderTest {

	@InjectMocks
	private MessageWebSocketOverHttpSender sender;

	@Mock
	private RejectionMessageService rejectionMessageService;
	@Mock
    private FileStreamingBean fileStreamingBean;
	
	private ResponseMessageBufferClient responseMessageBufferClient;
	private InputStreamSocketListenerClient inputStreamSocketListenerClient;
	
	private ResourceLoader resourceLoader;
	@Mock
	private Resource resourceKeyStore;
	
	private HttpWebSocketServerBean server;
	
	private String webSocketHost = "localhost";
	private Integer webSocketPort = 1234;
	private String header;
	private String payload = "PAYLOAD";
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		responseMessageBufferClient = new ResponseMessageBufferClient();
		ReflectionTestUtils.setField(responseMessageBufferClient, "responseMessageIsReceived", true, boolean.class);
		ReflectionTestUtils.setField(responseMessageBufferClient, "responseMessage", "RESPONSE".getBytes(StandardCharsets.UTF_8));
		
		inputStreamSocketListenerClient = new InputStreamSocketListenerClient();

		header = UtilMessageService.getMessageAsString(UtilMessageService.getArtifactRequestMessage());
		
		ReflectionTestUtils.setField(sender, "responseMessageBufferClient", responseMessageBufferClient, ResponseMessageBufferClient.class);
		ReflectionTestUtils.setField(sender, "inputStreamSocketListenerWebSocketClient", inputStreamSocketListenerClient, InputStreamSocketListenerClient.class);
		
		server = new HttpWebSocketServerBean();
		resourceLoader = new DefaultResourceLoader();
		ReflectionTestUtils.setField(server, "resourceLoader", resourceLoader, ResourceLoader.class);
		ReflectionTestUtils.setField(server, "keyStoreLocation", "classpath:ssl-server.jks", String.class);
		ReflectionTestUtils.setField(server, "keyStoreType", "JKS", String.class);
		ReflectionTestUtils.setField(server, "keyStorePassword", "changeit", String.class);
		
		server.setPort(webSocketPort);
		server.setMessagingServlet(HttpWebSocketMessagingServletA.class);
		server.createServer();
	}
	
	@AfterEach
	public void destroy() throws Exception {
		if(server != null) {
			server.onDestroy();
		}
	}
	
	@Test
	public void sendMultipartMessageWebSocketOverHttps1() throws KeyManagementException, ParseException, NoSuchAlgorithmException, IOException, InterruptedException, ExecutionException {
		
		String response = sender.sendMultipartMessageWebSocketOverHttps(webSocketHost, webSocketPort, header, payload);
		
		assertEquals("RESPONSE", response);
		verify(fileStreamingBean).setup(any(WebSocket.class));
        verify(fileStreamingBean).sendMultipartMessage(any(String.class));
	}
	
	@Test
	public void sendMultipartMessageWebSocketOverHttps2() throws KeyManagementException, ParseException, NoSuchAlgorithmException, IOException, InterruptedException, ExecutionException {
		Message message = UtilMessageService.getArtifactRequestMessage();
		
		String response = sender.sendMultipartMessageWebSocketOverHttps(webSocketHost, webSocketPort, header, payload, message);
		
		assertEquals("RESPONSE", response);
		verify(fileStreamingBean).setup(any(WebSocket.class));
        verify(fileStreamingBean).sendMultipartMessage(any(String.class));
	}
	
	@Test
	public void sendMultipartMessageWebSocketOverHttps3() throws KeyManagementException, ParseException, NoSuchAlgorithmException, IOException, InterruptedException, ExecutionException {
		String response = sender.sendMultipartMessageWebSocketOverHttps(webSocketHost, webSocketPort, null, header, payload);
		
		assertEquals("RESPONSE", response);
		verify(fileStreamingBean).setup(any(WebSocket.class));
        verify(fileStreamingBean).sendMultipartMessage(any(String.class));
	}
	
	@Test
	public void sendMultipartMessageWebSocketOverHttps4() throws KeyManagementException, ParseException, NoSuchAlgorithmException, IOException, InterruptedException, ExecutionException {
		Message message = UtilMessageService.getArtifactRequestMessage();
		String webSocketPath = null;

		String response = sender.sendMultipartMessageWebSocketOverHttps(webSocketHost, webSocketPort, webSocketPath, header, payload, message);
		
		assertEquals("RESPONSE", response);
		verify(fileStreamingBean).setup(any(WebSocket.class));
        verify(fileStreamingBean).sendMultipartMessage(any(String.class));
	}
	
}



