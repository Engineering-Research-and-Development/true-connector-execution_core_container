package it.eng.idsa.businesslogic.processor.sender;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.util.TestUtilMessageService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;

public class SenderParseReceivedDataProcessorHttpHeaderTest {

	@Mock
	private MultipartMessageService multipartMessageService;
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

	private String header;
	private Map<String, Object> httpHeaders;
	Map<String, Object> headerContentHeaders = new HashMap<>();

	@InjectMocks
	private SenderParseReceivedDataProcessorHttpHeader processor;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void processHttpHeaderTest() throws Exception {
		mockExchangeGetHttpHeaders(exchange);
		msg = TestUtilMessageService.getArtifactRequestMessage();
		header = TestUtilMessageService.getMessageAsString(msg);
		when(httpHeaderService.getHeaderContentHeaders(httpHeaders)).thenReturn(headerContentHeaders);
		when(httpHeaderService.getHeaderMessagePartFromHttpHeadersWithoutToken(httpHeaders)).thenReturn(header);
		when(multipartMessageService.getMessage(header)).thenReturn(msg);
//		when(exchange.getOut()).thenReturn(messageOut);

		processor.process(exchange);

		MultipartMessage multipartMessage = new MultipartMessageBuilder().withHttpHeader(new HashMap<>())
				.withHeaderContent(header)
				.withHeaderContent(msg)
				.withPayloadContent(null).build();

		verify(message).setBody(multipartMessage);
	}

	private void mockExchangeGetHttpHeaders(Exchange exchange) {
		when(exchange.getMessage()).thenReturn(message);
		httpHeaders = new HashMap<>();
		httpHeaders.put("Content-Type", ContentType.APPLICATION_JSON);
		httpHeaders.put("Forward-To", "https://forward.to.example");
		httpHeaders.put("IDS-Messagetype", "ids:ArtifactRequestMessage");
		httpHeaders.put("IDS-Issued", "2019-05-27T13:09:42.306Z");
		httpHeaders.put("IDS-IssuerConnector", "http://iais.fraunhofer.de/ids/mdm-connector");
		httpHeaders.put("IDS-Id",
				"https://w3id.org/idsa/autogen/artifactRequestMessage/d107ab28-5dc4-4f0c-a440-6d12ae6f2aab");
		httpHeaders.put("IDS-ModelVersion", "4.0.0");
		httpHeaders.put("IDS-RequestedArtifact", "http://mdm-connector.ids.isst.fraunhofer.de/artifact/1");
		httpHeaders.put("Payload-Content-Type", ContentType.APPLICATION_JSON);
		when(message.getHeaders()).thenReturn(httpHeaders);
	}

}
