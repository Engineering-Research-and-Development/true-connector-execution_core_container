package it.eng.idsa.businesslogic.processor.sender;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.UtilMessageService;

public class SenderParseReceivedDataProcessorHttpHeaderTest {

	@Mock
	private HttpHeaderService httpHeaderService;
	@Mock
	private Exchange exchange;
	@Mock
	private Message message;
	@Mock
	private Message messageOut;
	@Mock
	private de.fraunhofer.iais.eis.Message msg;
	
	@Mock
	private RejectionMessageService rejectionMessageService;
	
	private String header;
	private Map<String, Object> httpHeaders;
	Map<String, Object> headerContentHeaders = new HashMap<>();
	private String forwardTo;

	@InjectMocks
	private SenderParseReceivedDataProcessorHttpHeader processor;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		forwardTo = "https://forward.to.example";
	}

	@Test
	public void processHttpHeader() throws Exception {
		mockExchangeGetHttpHeaders(exchange);
		msg = UtilMessageService.getArtifactRequestMessage();
		header = UtilMessageService.getMessageAsString(msg);
		when(httpHeaderService.headersToMessage(httpHeaders)).thenReturn(msg);

		processor.process(exchange);

		MultipartMessage multipartMessage = new MultipartMessageBuilder().withHttpHeader(new HashMap<>())
				.withHeaderContent(header)
				.withHeaderContent(msg)
				.withPayloadContent(null).build();

		verify(message).setBody(multipartMessage);
		verify(rejectionMessageService,times(0)).sendRejectionMessage(null, RejectionReason.MALFORMED_MESSAGE);
	}
	
	@Test
	@Disabled("Not valid anymore")
	public void processHttpHeadersForwardTo_Null() throws Exception {
		forwardTo = null;
		mockExchangeGetHttpHeaders(exchange);
		msg = UtilMessageService.getArtifactRequestMessage();
		header = UtilMessageService.getMessageAsString(msg);
		when(httpHeaderService.headersToMessage(httpHeaders)).thenReturn(msg);

		processor.process(exchange);

		MultipartMessage multipartMessage = new MultipartMessageBuilder().withHttpHeader(new HashMap<>())
				.withHeaderContent(header)
				.withHeaderContent(msg)
				.withPayloadContent(null).build();

		verify(message).setBody(multipartMessage);
		verify(rejectionMessageService,times(0)).sendRejectionMessage(null, RejectionReason.MALFORMED_MESSAGE);
	}
	
	@Test
	public void processHttpHeaderRequiredHeadersNotPresent() throws Exception {
		mockExchangeGetHttpHeaders(exchange);
		httpHeaders.remove("IDS-Messagetype");
		msg = UtilMessageService.getArtifactRequestMessage();
		header = UtilMessageService.getMessageAsString(msg);
		when(httpHeaderService.headersToMessage(httpHeaders)).thenReturn(null);
		doThrow(ExceptionForProcessor.class)
			.when(rejectionMessageService).sendRejectionMessage(null, RejectionReason.MALFORMED_MESSAGE);

		assertThrows(ExceptionForProcessor.class,
	            ()->{
	            	processor.process(exchange);
	            });

		verify(rejectionMessageService).sendRejectionMessage(null, RejectionReason.MALFORMED_MESSAGE);
	}
	
	private void mockExchangeGetHttpHeaders(Exchange exchange) {
		when(exchange.getMessage()).thenReturn(message);
		httpHeaders = new HashMap<>();
		httpHeaders.put("Content-Type", ContentType.APPLICATION_JSON);
		httpHeaders.put("Forward-To", forwardTo);
		httpHeaders.put("IDS-Messagetype", "ids:ArtifactRequestMessage");
		httpHeaders.put("IDS-Issued", "2019-05-27T13:09:42.306Z");
		httpHeaders.put("IDS-IssuerConnector", "http://iais.fraunhofer.de/ids/mdm-connector");
		httpHeaders.put("IDS-Id",
				"https://w3id.org/idsa/autogen/artifactRequestMessage/d107ab28-5dc4-4f0c-a440-6d12ae6f2aab");
		httpHeaders.put("IDS-ModelVersion", "4.2.7");
		httpHeaders.put("IDS-RequestedArtifact", "http://mdm-connector.ids.isst.fraunhofer.de/artifact/1");
		httpHeaders.put("Payload-Content-Type", ContentType.APPLICATION_JSON);
		when(message.getHeaders()).thenReturn(httpHeaders);
	}

}
