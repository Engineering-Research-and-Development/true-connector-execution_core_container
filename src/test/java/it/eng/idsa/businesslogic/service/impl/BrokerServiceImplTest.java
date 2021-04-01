package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.DapsTokenProviderService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.SendDataToBusinessLogicService;
import it.eng.idsa.businesslogic.util.TestUtilMessageService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class BrokerServiceImplTest {
	
	@InjectMocks
	private BrokerServiceImpl brokerServiceImpl;
	
	@Mock
	private SendDataToBusinessLogicService sendDataToBusinessLogicService;
	
	@Mock
	private DapsTokenProviderService dapsTokenProviderService;
	
	@Mock
	private MultipartMessageService multiPartMessageService;
	
	private String brokerURL;
	
	private Message messageWithToken;
	
	private Message messageWithoutToken;
	
	private String selfDescription;
	
	@Mock
	private Response response;
	@Mock
	private ResponseBody responseBody;
	
	private MultipartMessage multipartMessage;
	
	private Map<String, Object> headers;
	
	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
		brokerURL = "mockBrokerURL";
		ReflectionTestUtils.setField(brokerServiceImpl, "brokerURL", brokerURL);
		when(dapsTokenProviderService.provideToken()).thenReturn(TestUtilMessageService.getDynamicAttributeToken().getTokenValue());
		messageWithToken = TestUtilMessageService.getArtifactRequestMessageWithToken();
		messageWithoutToken = TestUtilMessageService.getArtifactRequestMessage();
		selfDescription = "connectorSelfDescription";
		multipartMessage = new MultipartMessageBuilder()
				.withHeaderContent(messageWithToken)
				.withPayloadContent(selfDescription)
				.build();
		headers = new HashMap<String, Object>();
		headers.put("Payload-Content-Type", ContentType.APPLICATION_JSON);
		when(multiPartMessageService.addToken(messageWithoutToken, TestUtilMessageService.getDynamicAttributeToken().getTokenValue())).thenReturn(TestUtilMessageService.getMessageAsString(messageWithToken));
	}
	
	@Test
	public void succesfullRequestTest() throws UnsupportedOperationException, IOException {
		String responseMessage = "Registration succesfull";
		mockResponse(responseMessage);
		when(sendDataToBusinessLogicService.sendMessageBinary(brokerURL, multipartMessage, headers, true)).thenReturn(response);
		assertEquals(responseMessage, new String(((CloseableHttpResponse)ReflectionTestUtils.getField(brokerServiceImpl, "response")).getEntity().getContent().readAllBytes()));
	}
	
	@Test
	public void failedRequestTest() throws UnsupportedOperationException, IOException {
		when(sendDataToBusinessLogicService.sendMessageBinary(brokerURL, multipartMessage, headers, true)).thenThrow(new UnsupportedEncodingException("Something went wrong"));
		verify(response, times(0)).body().string();
	}
	

	private void mockResponse(String responseMessage) throws UnsupportedOperationException, IOException {
		when(response.body()).thenReturn(responseBody);
		when(responseBody.string()).thenReturn("ABC");
	}
}
