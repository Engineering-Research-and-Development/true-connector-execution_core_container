package it.eng.idsa.businesslogic.processor.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.service.DapsTokenProviderService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.MockUtil;
import it.eng.idsa.businesslogic.util.RouterType;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.UtilMessageService;

public class GetTokenFromDapsProcessorTest {

	@InjectMocks
	private GetTokenFromDapsProcessor processor;

	@Mock
	private Exchange exchange;

	@Mock
	private DapsTokenProviderService dapsTokenProviderService;
	@Mock
	private MultipartMessageService multipartMessageService;

	@Mock
	private org.apache.camel.Message camelMessage;
	
	@Mock
	private MultipartMessage multipartMessage;
	
	@Captor 
	ArgumentCaptor<MultipartMessage> argCaptorMultipartMessage;
	@Mock
	private ApplicationEventPublisher publisher;
	
	private RejectionMessageService rejectionMessageService;

	private Map<String, Object> headers = new HashMap<>();
	private Message message;

	private String messageAsString;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		message = UtilMessageService.getArtifactRequestMessage();
	}

	@Test
	public void getJwTokenSuccess() throws Exception {
		mockExchangeHeaderAndBody();
		when(dapsTokenProviderService.provideToken()).thenReturn(UtilMessageService.TOKEN_VALUE);
		messageAsString = UtilMessageService.getMessageAsString(message);
		when(multipartMessageService.addToken(message, UtilMessageService.TOKEN_VALUE)).thenReturn(messageAsString);
		
		processor.process(exchange);
		
		verify(dapsTokenProviderService).provideToken();
		verify(multipartMessageService).addToken(message, UtilMessageService.TOKEN_VALUE);
	}
	
	@Test
	public void getJwTokenSuccessHttpHeader() throws Exception {
		ReflectionTestUtils.setField(processor, "eccHttpSendRouter", RouterType.HTTP_HEADER, String.class);

		mockExchangeHeaderAndBody();
		when(dapsTokenProviderService.provideToken()).thenReturn(UtilMessageService.TOKEN_VALUE);
		messageAsString = UtilMessageService.getMessageAsString(message);
		when(multipartMessageService.addToken(message, UtilMessageService.TOKEN_VALUE)).thenReturn(messageAsString);
		
		processor.process(exchange);
		
		verify(camelMessage).setBody(argCaptorMultipartMessage.capture());
		assertEquals(UtilMessageService.TOKEN_VALUE, argCaptorMultipartMessage.getValue().getToken());
		
		verify(dapsTokenProviderService).provideToken();
//		verify(multipartMessageService, times(0)).addToken(message, UtilMessageService.TOKEN_VALUE);
	}
	
	@Test
	public void exceptionWhenCallingDaps() throws Exception {
		mockExchangeHeaderAndBody();
		
		rejectionMessageService = MockUtil.mockRejectionService(rejectionMessageService);
		ReflectionTestUtils.setField(processor, "rejectionMessageService", 
				rejectionMessageService, RejectionMessageService.class);
		
		doThrow(InternalServerError.class).when(dapsTokenProviderService).provideToken();
		
		assertThrows(ExceptionForProcessor.class,
	            ()->{
	            	processor.process(exchange);
	            });
		verify(dapsTokenProviderService).provideToken();
	}
	
	@Test
	public void nullReturnedWhenCallingDaps() throws Exception {
		mockExchangeHeaderAndBody();
		
		rejectionMessageService = MockUtil.mockRejectionService(rejectionMessageService);
		ReflectionTestUtils.setField(processor, "rejectionMessageService", 
				rejectionMessageService, RejectionMessageService.class);
		
		when(dapsTokenProviderService.provideToken()).thenReturn(null);
		
		assertThrows(ExceptionForProcessor.class,
	            ()->{
	            	processor.process(exchange);
	            });
		verify(dapsTokenProviderService).provideToken();
	}
	
	@Test
	public void emptyTokenReturnedWhenCallingDaps() throws Exception {
		mockExchangeHeaderAndBody();
		
		rejectionMessageService = MockUtil.mockRejectionService(rejectionMessageService);
		ReflectionTestUtils.setField(processor, "rejectionMessageService", 
				rejectionMessageService, RejectionMessageService.class);
		
		when(dapsTokenProviderService.provideToken()).thenReturn("");
		
		assertThrows(ExceptionForProcessor.class,
	            ()->{
	            	processor.process(exchange);
	            });
		verify(dapsTokenProviderService).provideToken();
	}

	private void mockExchangeHeaderAndBody() {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getHeaders()).thenReturn(headers);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(multipartMessage.getHeaderContent()).thenReturn(message);
	}
}
