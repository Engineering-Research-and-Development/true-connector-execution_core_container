//package it.eng.idsa.businesslogic.processor.sender;
//
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.doThrow;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.apache.camel.Exchange;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import com.google.gson.Gson;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//
//import de.fraunhofer.iais.eis.Message;
//import de.fraunhofer.iais.eis.RejectionReason;
//import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
//import it.eng.idsa.businesslogic.service.RejectionMessageService;
//import it.eng.idsa.businesslogic.usagecontrol.service.UsageControlService;
//import it.eng.idsa.businesslogic.util.HeaderCleaner;
//import it.eng.idsa.businesslogic.util.MockUtil;
//import it.eng.idsa.businesslogic.util.RejectionMessageType;
//import it.eng.idsa.multipart.domain.MultipartMessage;
//import it.eng.idsa.multipart.util.UtilMessageService;
//
//public class SenderUsageControlProcessorTest {
//
//	@InjectMocks
//	private SenderUsageControlProcessor processor;
//
//	@Mock
//	private UsageControlService ucService;
//	@Mock
//	private HeaderCleaner headerCleaner;
//	@Mock
//	private RejectionMessageService rejectionMessageService;
//	@Mock
//	private Exchange exchange;
//	@Mock
//	private org.apache.camel.Message camelMessage;
//	@Mock
//	private MultipartMessage multipartMessage;
//	
//	private Gson gson;
//
//	private Map<String, Object> headers;
//	private Message message;
//	
//	private JsonObject jsonUCObject;
//	
//	@BeforeEach
//	public void setup() {
//		MockitoAnnotations.initMocks(this);
//		message = UtilMessageService.getArtifactResponseMessage();
//		headers = new HashMap<>();
//		jsonUCObject = new JsonObject();
//		gson = mock(Gson.class);
//		when(gson.fromJson(mockUsageControlPayload(), JsonElement.class)).thenReturn(jsonUCObject);
//		ReflectionTestUtils.setField(processor, "gson", gson);
//	}
//
//	@Test
//	public void usageControlDisabled() throws Exception {
//		ReflectionTestUtils.setField(processor, "isEnabledUsageControl", false);
//		processor.process(exchange);
//		verify(ucService, times(0)).enforceUsageControl(any(JsonElement.class));
//	}
//
//	@Test
//	public void usageControlEnabledAndAllowed() throws Exception {
//		ReflectionTestUtils.setField(processor, "isEnabledUsageControl", true);
//		mockExchangeHeaderAndBody();
//		when(multipartMessage.getPayloadContent()).thenReturn(mockUsageControlPayload());
//		when(ucService.enforceUsageControl(any(JsonElement.class))).thenReturn("Usage allowed");
//
//		processor.process(exchange);
//		
//		verify(ucService).enforceUsageControl(any(JsonElement.class));
//		verify(rejectionMessageService, times(0)).sendRejectionMessage(
//                RejectionMessageType.REJECTION_USAGE_CONTROL,
//                message);
//	}
//
//	@Test
//	public void usageControlEnabledAndInhibited() throws Exception {
//		ReflectionTestUtils.setField(processor, "isEnabledUsageControl", true);
//		rejectionMessageService = MockUtil.mockRejectionService(rejectionMessageService);
//		ReflectionTestUtils.setField(processor, "rejectionMessageService", 
//				rejectionMessageService, RejectionMessageService.class);
//
//		mockExchangeHeaderAndBody();
//		when(multipartMessage.getPayloadContent()).thenReturn(mockUsageControlPayload());
//		when(ucService.enforceUsageControl(any(JsonElement.class))).thenReturn(null);
//		doThrow(Exception.class)
//			.when(ucService).enforceUsageControl(any(JsonElement.class));
//		
//		assertThrows(ExceptionForProcessor.class,
//	            ()->{
//	            	processor.process(exchange);
//	            });
//		
//		verify(ucService).enforceUsageControl(any(JsonElement.class));
//	}
//
//	@Test
//	public void usageControlEnabledAndNotUCObject() throws Exception {
//		ReflectionTestUtils.setField(processor, "isEnabledUsageControl", true);
//		mockExchangeHeaderAndBody();
//		when(multipartMessage.getPayloadContent()).thenReturn("not UC payload");
//		doThrow(NullPointerException.class).when(ucService).enforceUsageControl(null);
//
//		
//		processor.process(exchange);
//		
//		verify(ucService, times(0)).enforceUsageControl(any(JsonElement.class));
//		verify(rejectionMessageService).sendRejectionMessage(
//                RejectionMessageType.REJECTION_USAGE_CONTROL,
//                message);
//	}
//	
//	@Test
//	public void usageControlEnabledAndPayloadNull() throws Exception {
//		ReflectionTestUtils.setField(processor, "isEnabledUsageControl", true);
//		mockExchangeHeaderAndBody();
//		when(multipartMessage.getPayloadContent()).thenReturn(null);
//		doThrow(NullPointerException.class).when(ucService).enforceUsageControl(null);
//		
//		processor.process(exchange);
//		
//		verify(ucService, times(0)).enforceUsageControl(any(JsonElement.class));
//		verify(rejectionMessageService).sendRejectionMessage(
//                RejectionMessageType.REJECTION_USAGE_CONTROL,
//                message);
//	}
//	
//	@Test
//	public void usageControlEnabledMessageNotArtifactResponseMessage() throws Exception{
//		ReflectionTestUtils.setField(processor, "isEnabledUsageControl", true);
//		message = UtilMessageService.getRejectionMessage(RejectionReason.NOT_AUTHORIZED);
//		mockExchangeHeaderAndBody();
//		
//		processor.process(exchange);
//		
//		verify(ucService, times(0)).enforceUsageControl(any(JsonElement.class));
//		verify(rejectionMessageService, times(0)).sendRejectionMessage(
//                RejectionMessageType.REJECTION_USAGE_CONTROL,
//                message);
//	}
//
//	private void mockExchangeHeaderAndBody() {
//		when(exchange.getMessage()).thenReturn(camelMessage);
//		when(camelMessage.getHeaders()).thenReturn(headers);
//		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
//		when(multipartMessage.getHeaderContent()).thenReturn(message);
//	}
//
//	private String mockUsageControlPayload() {
//		return "{\"payload\":\"{\\\"firstName\\\":\\\"John\\\",\\\"lastName\\\":\\\"Doe\\\",\\\"address\\\":\\\"591  Franklin Street, Pennsylvania\\\",\\\"checksum\\\":\\\"ABC123 2022/06/22 10:22:39\\\",\\\"dateOfBirth\\\":\\\"2022/06/22 10:22:39\\\"}\",\"targetArtifactId\":\"http://w3id.org/engrd/connector/artifact/23\",\"assignee\":\"https://consumer.com\",\"assigner\":\"https://provider.com\"}";
//	}
//}