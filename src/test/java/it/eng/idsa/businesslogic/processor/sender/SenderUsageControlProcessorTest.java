package it.eng.idsa.businesslogic.processor.sender;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.gson.internal.LinkedTreeMap;

import de.fraunhofer.dataspaces.iese.camel.interceptor.model.IdsUseObject;
import de.fraunhofer.dataspaces.iese.camel.interceptor.service.UcService;
import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.impl.RejectionMessageServiceImpl;
import it.eng.idsa.businesslogic.util.HeaderCleaner;
import it.eng.idsa.businesslogic.util.MessagePart;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.businesslogic.util.TestUtilMessageService;
import it.eng.idsa.multipart.domain.MultipartMessage;

public class SenderUsageControlProcessorTest {

	@InjectMocks
	private SenderUsageControlProcessor processor;

	@Mock
	private UcService ucService;
	@Mock
	private HeaderCleaner headerCleaner;
	@Mock
	private RejectionMessageService rejectionMessageService;
	@Mock
	private Exchange exchange;
	@Mock
	private org.apache.camel.Message camelMessage;
	@Mock
	private MultipartMessage multipartMessage;

	private Map<String, Object> headers = new HashMap<>();
	private Message message;

	private Map<String, String> ucResult;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		message = TestUtilMessageService.getArtifactResponseMessage();
		ucResult = new LinkedTreeMap<>();
	}

	@Test
	public void usageControlDisabled() {
		ReflectionTestUtils.setField(processor, "isEnabledUsageControl", false);
		processor.process(exchange);
		verify(ucService, times(0)).enforceUsageControl(any(IdsUseObject.class));
	}

	@Test
	public void usageControlEnabledAndAllowed() throws URISyntaxException {
		ReflectionTestUtils.setField(processor, "isEnabledUsageControl", true);
		mockExchangeHeaderAndBody();
		when(multipartMessage.getPayloadContent()).thenReturn(mockUsageControlPayload());
		ucResult.put(MessagePart.PAYLOAD, "Payload");
		when(ucService.enforceUsageControl(any(IdsUseObject.class))).thenReturn(ucResult);

		processor.process(exchange);
		
		verify(ucService).enforceUsageControl(any(IdsUseObject.class));
		verify(rejectionMessageService, times(0)).sendRejectionMessage(
                RejectionMessageType.REJECTION_USAGE_CONTROL,
                message);
	}

	@Test
	public void usageControlEnabledAndInhibited() {
		ReflectionTestUtils.setField(processor, "isEnabledUsageControl", true);
		rejectionMessageService = new RejectionMessageServiceImpl();
		ReflectionTestUtils.setField(processor, "rejectionMessageService", 
				rejectionMessageService, RejectionMessageService.class);

		mockExchangeHeaderAndBody();
		when(multipartMessage.getPayloadContent()).thenReturn(mockUsageControlPayload());
		ucResult.put(MessagePart.PAYLOAD, "Payload");
		when(ucService.enforceUsageControl(any(IdsUseObject.class))).thenReturn(ucResult);
		doThrow(AccessDeniedException.class)
			.when(ucService).enforceUsageControl(any(IdsUseObject.class));
		
		assertThrows(ExceptionForProcessor.class,
	            ()->{
	            	processor.process(exchange);
	            });
		
		verify(ucService).enforceUsageControl(any(IdsUseObject.class));
	}

	@Test
	public void usageControlEnabledAndNotUCObject() {
		ReflectionTestUtils.setField(processor, "isEnabledUsageControl", true);
		mockExchangeHeaderAndBody();
		when(multipartMessage.getPayloadContent()).thenReturn("not UC payload");
		
		processor.process(exchange);
		
		verify(ucService, times(0)).enforceUsageControl(any(IdsUseObject.class));
		verify(rejectionMessageService).sendRejectionMessage(
                RejectionMessageType.REJECTION_USAGE_CONTROL,
                message);
	}
	
	@Test
	public void usageControlEnabledAndPayloadNull() {
		ReflectionTestUtils.setField(processor, "isEnabledUsageControl", true);
		mockExchangeHeaderAndBody();
		when(multipartMessage.getPayloadContent()).thenReturn(null);
		
		processor.process(exchange);
		
		verify(ucService, times(0)).enforceUsageControl(any(IdsUseObject.class));
		verify(rejectionMessageService).sendRejectionMessage(
                RejectionMessageType.REJECTION_USAGE_CONTROL,
                message);
	}
	
	@Test
	public void usageControlEnabledMessageNotArtifactResponseMessage(){
		ReflectionTestUtils.setField(processor, "isEnabledUsageControl", true);
		message = TestUtilMessageService.getRejectionMessage();
		mockExchangeHeaderAndBody();
		
		processor.process(exchange);
		
		verify(ucService, times(0)).enforceUsageControl(any(IdsUseObject.class));
		verify(rejectionMessageService, times(0)).sendRejectionMessage(
                RejectionMessageType.REJECTION_USAGE_CONTROL,
                message);
	}

	private void mockExchangeHeaderAndBody() {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getHeaders()).thenReturn(headers);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(multipartMessage.getHeaderContent()).thenReturn(message);
	}

	private String mockUsageControlPayload() {
		return "{\r\n" + "	\"meta\": {\r\n" + "		\"assigner\": \"http://issuerConnector.com\",\r\n"
				+ "		\"assignee\": \"http://issuerConnector.com\",\r\n" + "		\"targetArtifact\": {\r\n"
				+ "			\"creationDate\": \"2021-01-06T15:32:40.6837894+01:00\",\r\n"
				+ "			\"@id\": \"http://dummy.uri\"\r\n" + "		}\r\n" + "	},\r\n"
				+ "	\"payload\": \"Payload\"\r\n" + "}";
	}
}