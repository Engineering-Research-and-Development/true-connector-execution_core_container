package it.eng.idsa.businesslogic.processor.receiver;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.impl.SendDataToBusinessLogicServiceImpl;
import it.eng.idsa.businesslogic.util.MultipartMessageUtil;
import it.eng.idsa.businesslogic.util.RequestResponseUtil;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.UtilMessageService;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ReceiverSendDataToDataAppProcessorTest {
	
	@InjectMocks
	private ReceiverSendDataToDataAppProcessor processor;
	
	@Mock
	private SendDataToBusinessLogicServiceImpl sendDataToBusinessLogicService;
	@Mock
	private RejectionMessageService rejectionMessageService;
	@Mock
	private HttpHeaderService httpHeaderService;
	
	@Mock
	private Exchange exchange;
	@Mock
	private Message message;
	@Mock 
	private MultipartMessage multipartMessage;
	@Mock
	private ApplicationConfiguration configuration;
	@Mock
	private Response response;
	
	private static final String RESPONSE_SUCCESFULL_MESSAGE = "response message OK";
	private String URL = "https://mock.address.com";
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		when(exchange.getMessage()).thenReturn(message);
		when(message.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(configuration.getOpenDataAppReceiver()).thenReturn("http://openDataAppReceiver");
	}
	
	@Test
	public void processMixedSuccess() throws Exception {
		ReflectionTestUtils.setField(processor, "openDataAppReceiverRouter", "mixed", String.class);
		RequestBody mixRequestBody = RequestResponseUtil.createRequestBody("payload");
		MultipartMessage multipartMessage = MultipartMessageUtil.getMultipartMessage();
		Response response = RequestResponseUtil.createResponse(
				RequestResponseUtil.createRequest(URL, mixRequestBody), 
				RESPONSE_SUCCESFULL_MESSAGE, 
				RequestResponseUtil.createResponseBodyJsonUTF8(MultipartMessageProcessor.multipartMessagetoString(multipartMessage)), 
				200);
		
		when(sendDataToBusinessLogicService.sendMessageBinary(any(String.class), any(MultipartMessage.class), any(HashMap.class)))
			.thenReturn(response);
		
		processor.process(exchange);
		
		verify(message).setBody(multipartMessage);
	}
	
	@Test
	public void processMixedFailed() throws Exception {
		ReflectionTestUtils.setField(processor, "openDataAppReceiverRouter", "mixed", String.class);
		RequestBody mixRequestBody = RequestResponseUtil.createRequestBody("payload");
		MultipartMessage multipartMessage = MultipartMessageUtil.getMultipartMessage();
		String badMultipartMessage = MultipartMessageProcessor.multipartMessagetoString(multipartMessage).replaceFirst("Artifact", "something");
		Response response = RequestResponseUtil.createResponse(
				RequestResponseUtil.createRequest(URL, mixRequestBody), 
				RESPONSE_SUCCESFULL_MESSAGE, 
				RequestResponseUtil.createResponseBodyJsonUTF8(badMultipartMessage), 
				200);
		
		when(sendDataToBusinessLogicService.sendMessageBinary(any(String.class), any(MultipartMessage.class), any(HashMap.class)))
			.thenReturn(response);
		
		assertThrows(Exception.class,
	            ()->{
	            	processor.process(exchange);
	            });
		
		verify(message, times(0)).setBody(multipartMessage);
	}
	
	@Test
	public void processFormSuccess() throws Exception {
		ReflectionTestUtils.setField(processor, "openDataAppReceiverRouter", "form", String.class);
		RequestBody mixRequestBody = RequestResponseUtil.createRequestBody("payload");
		MultipartMessage multipartMessage = MultipartMessageUtil.getMultipartMessage();
		Response response = RequestResponseUtil.createResponse(
				RequestResponseUtil.createRequest(URL, mixRequestBody), 
				RESPONSE_SUCCESFULL_MESSAGE, 
				RequestResponseUtil.createResponseBodyJsonUTF8(MultipartMessageProcessor.multipartMessagetoString(multipartMessage)), 
				200);
		
		when(sendDataToBusinessLogicService.sendMessageFormData(any(String.class), any(MultipartMessage.class), any(HashMap.class)))
			.thenReturn(response);
		
		processor.process(exchange);
		
		verify(message).setBody(multipartMessage);
	}
	
	@Test
	public void processFormFailed() throws Exception {
		ReflectionTestUtils.setField(processor, "openDataAppReceiverRouter", "form", String.class);
		RequestBody mixRequestBody = RequestResponseUtil.createRequestBody("payload");
		MultipartMessage multipartMessage = MultipartMessageUtil.getMultipartMessage();
		String badMultipartMessage = MultipartMessageProcessor.multipartMessagetoString(multipartMessage).replaceFirst("Artifact", "something");
		Response response = RequestResponseUtil.createResponse(
				RequestResponseUtil.createRequest(URL, mixRequestBody), 
				RESPONSE_SUCCESFULL_MESSAGE, 
				RequestResponseUtil.createResponseBodyJsonUTF8(badMultipartMessage), 
				200);
		
		when(sendDataToBusinessLogicService.sendMessageFormData(any(String.class), any(MultipartMessage.class), any(HashMap.class)))
			.thenReturn(response);
		
		assertThrows(Exception.class,
	            ()->{
	            	processor.process(exchange);
	            });
		
		verify(message, times(0)).setBody(multipartMessage);
	}
	
	@Test
	public void processHttpHeaderSuccess() throws Exception {
		ReflectionTestUtils.setField(processor, "openDataAppReceiverRouter", "http-header", String.class);
		RequestBody mixRequestBody = RequestResponseUtil.createRequestBody("PAYLOAD");
		MultipartMessage multipartMessage = MultipartMessageUtil.getMultipartMessage();
		Response response = RequestResponseUtil.createResponse(
				RequestResponseUtil.createRequest(URL, mixRequestBody), 
				RESPONSE_SUCCESFULL_MESSAGE, 
				RequestResponseUtil.createResponseBodyJsonUTF8("PAYLOAD"), 
				200);
		
		when(httpHeaderService.headersToMessage(any(HashMap.class))).thenReturn(multipartMessage.getHeaderContent());
		when(sendDataToBusinessLogicService.sendMessageHttpHeader(any(String.class), any(MultipartMessage.class), any(HashMap.class)))
			.thenReturn(response);
		
		processor.process(exchange);
		
		verify(message).setBody(multipartMessage);
	}
	
	@Test
	public void processHttpHeaderFailed() throws Exception {
		ReflectionTestUtils.setField(processor, "openDataAppReceiverRouter", "http-header", String.class);
		RequestBody mixRequestBody = RequestResponseUtil.createRequestBody("PAYLOAD");
		MultipartMessage multipartMessage = MultipartMessageUtil.getMultipartMessage();
		Response response = RequestResponseUtil.createResponse(
				RequestResponseUtil.createRequest(URL, mixRequestBody), 
				RESPONSE_SUCCESFULL_MESSAGE, 
				RequestResponseUtil.createResponseBodyJsonUTF8("PAYLOAD"), 
				200);
		
		when(sendDataToBusinessLogicService.sendMessageHttpHeader(any(String.class), any(MultipartMessage.class), any(HashMap.class)))
			.thenReturn(response);
		
		assertThrows(Exception.class,
	            ()->{
	            	processor.process(exchange);
	            });
		
		verify(message, times(0)).setBody(multipartMessage);
	}
	
	@Test()
	public void defaultCase() throws Exception {
		ReflectionTestUtils.setField(processor, "openDataAppReceiverRouter", "def", String.class);
		ArtifactRequestMessage originalMessage = UtilMessageService.getArtifactRequestMessage();
		when(exchange.getProperty("Original-Message-Header")).thenReturn(originalMessage);
		doThrow(ExceptionForProcessor.class)
			.when(rejectionMessageService).sendRejectionMessage(any(de.fraunhofer.iais.eis.Message.class), any(RejectionReason.class));
		
		assertThrows(ExceptionForProcessor.class,
	            ()->{
	            	processor.process(exchange);
	            });
		
		verify(rejectionMessageService).sendRejectionMessage(originalMessage, RejectionReason.INTERNAL_RECIPIENT_ERROR);
	}
}
