package it.eng.idsa.businesslogic.processor.sender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.util.HeaderCleaner;
import it.eng.idsa.businesslogic.util.RouterType;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.UtilMessageService;

public class SenderSendResponseToDataAppProcessorTest {
	
	private static final String PAYLOAD = "Payload response";

	@InjectMocks
	private SenderSendResponseToDataAppProcessor processor;

	@Mock
	private MultipartMessageService multipartMessageService;
	@Mock
	private HttpHeaderService httpHeaderService;
	@Mock
	private HeaderCleaner headerCleaner;
	
	@Mock
	private Exchange exchange;
	@Mock
	private org.apache.camel.Message camelMessage;
	@Mock
	private HttpEntity httpEntity;
	@Mock
	private org.apache.http.Header header;
	
	private MultipartMessage multipartMessage;

	private Map<String, Object> headers = new HashMap<>();
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		multipartMessage = new MultipartMessageBuilder()
				.withHeaderContent(UtilMessageService.getArtifactRequestMessage())
				.withPayloadContent(PAYLOAD).build();
	}
	
	@Test
	public void sendDataToMultipartMixDapsDisabled() throws Exception {
		ReflectionTestUtils.setField(processor, "openDataAppReceiverRouter", RouterType.MULTIPART_MIX);
		mockExchangeHeaderAndBody();
		
		processor.process(exchange);
		
		// need to do it with any(String) since json sting can have different ordering
		verify(camelMessage).setBody(any(String.class));
    	verify(headerCleaner).removeTechnicalHeaders(exchange.getMessage().getHeaders());
    	verify(headerCleaner).removeTechnicalHeaders(camelMessage.getHeaders());

	}
	
	@Test
	public void sendDataToMultipartMixDapsEnabled() throws Exception {
		ReflectionTestUtils.setField(processor, "openDataAppReceiverRouter", RouterType.MULTIPART_MIX);
		ReflectionTestUtils.setField(processor, "isEnabledDapsInteraction", true);

		mockExchangeHeaderAndBody();
		
		processor.process(exchange);
		
		verify(camelMessage).setBody(any(String.class));
    	verify(headerCleaner).removeTechnicalHeaders(exchange.getMessage().getHeaders());
    	verify(headerCleaner).removeTechnicalHeaders(camelMessage.getHeaders());

	}
	
	@Test
	public void sendDataToMultipartForm() throws Exception {
		ReflectionTestUtils.setField(processor, "openDataAppReceiverRouter", RouterType.MULTIPART_BODY_FORM);
		mockExchangeHeaderAndBody();

		when(multipartMessageService.createMultipartMessage(multipartMessage.getHeaderContentString(), 
						multipartMessage.getPayloadContent(),
						null, ContentType.TEXT_PLAIN)).thenReturn(httpEntity);
		when(httpEntity.getContentType()).thenReturn(header);
		when(header.getValue()).thenReturn("text/plain");
		
		processor.process(exchange);
		
    	verify(camelMessage).setBody(anyString());
    	verify(headerCleaner).removeTechnicalHeaders(camelMessage.getHeaders());
	}
	
	@Test
	public void sendDataToHttpHeader() throws Exception {
		ReflectionTestUtils.setField(processor, "openDataAppReceiverRouter", RouterType.HTTP_HEADER);
		mockExchangeHeaderAndBody();

		processor.process(exchange);
		
		verify(camelMessage).setBody(multipartMessage.getPayloadContent());
    	verify(headerCleaner).removeTechnicalHeaders(camelMessage.getHeaders());
	}
	
	private void mockExchangeHeaderAndBody() {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getHeaders()).thenReturn(headers);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
	}

}