package it.eng.idsa.businesslogic.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.DapsTokenProviderService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.SendDataToBusinessLogicService;
import it.eng.idsa.businesslogic.util.RequestResponseUtil;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.UtilMessageService;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class BrokerServiceImplTest {

	private BrokerServiceImpl brokerServiceImpl;

	@Mock
	private SendDataToBusinessLogicService sendDataToBusinessLogicService;
	@Mock
	private DapsTokenProviderService dapsTokenProviderService;
	@Mock
	private MultipartMessageService multiPartMessageService;
	@Mock
	private RejectionMessageService rejectionService;
	
	private Message message;
	private String payload = "mockPayload";
	private Map<String, Object> headers;
	private static final String FORWARD_TO = "http://forward.to";

	@BeforeEach
	public void init() {
		MockitoAnnotations.openMocks(this);
		brokerServiceImpl = new BrokerServiceImpl(sendDataToBusinessLogicService, Optional.of(dapsTokenProviderService), 
				multiPartMessageService, rejectionService, "mockBrokerURL");
		when(dapsTokenProviderService.provideToken())
				.thenReturn(UtilMessageService.getDynamicAttributeToken().getTokenValue());
		message = UtilMessageService.getConnectorUpdateMessage(UtilMessageService.SENDER_AGENT, UtilMessageService.ISSUER_CONNECTOR, UtilMessageService.AFFECTED_CONNECTOR);
		headers = new HashMap<String, Object>();
		headers.put("Payload-Content-Type", ContentType.APPLICATION_JSON);
		when(multiPartMessageService.addToken(message,
				UtilMessageService.getDynamicAttributeToken().getTokenValue()))
						.thenReturn(UtilMessageService.getMessageAsString(message));
	}

	@Test
	public void succesfullRequestTest() throws UnsupportedOperationException, IOException {
		String responseMessage = "Registration succesfull";

		Request request = RequestResponseUtil.createRequest(FORWARD_TO,
				RequestResponseUtil.createRequestBody(payload));

		ResponseBody responseBody = RequestResponseUtil.createResponseBodyJsonUTF8(responseMessage);

		when(sendDataToBusinessLogicService.sendMessageBinary(anyString(), any(MultipartMessage.class), anyMap()))
				.thenReturn(RequestResponseUtil.createResponse(request, responseMessage, responseBody, 200));

		brokerServiceImpl.sendBrokerRequest(message, payload);
		
		verify(sendDataToBusinessLogicService).sendMessageBinary(anyString(), any(MultipartMessage.class), anyMap());
	}

	@Test
	public void failedRequestTest() throws UnsupportedOperationException, IOException {
		when(sendDataToBusinessLogicService.sendMessageBinary(anyString(), any(MultipartMessage.class), anyMap()))
				.thenThrow(new UnsupportedEncodingException("Something went wrong"));
		
		brokerServiceImpl.sendBrokerRequest(message, payload);
		
		verify(sendDataToBusinessLogicService).sendMessageBinary(anyString(), any(MultipartMessage.class), anyMap());
	}

}
