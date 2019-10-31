package it.eng.idsa.businesslogic.web.rest;

import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.when;

import org.apache.http.HttpEntity;
import org.junit.Before;

import org.junit.jupiter.api.Test;

import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import de.fraunhofer.iais.eis.Message;

import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;

import it.eng.idsa.businesslogic.service.impl.CommunicationServiceImpl;
import it.eng.idsa.businesslogic.service.impl.DapsServiceImpl;
import it.eng.idsa.businesslogic.service.impl.MultiPartMessageServiceImpl;

import static org.mockito.Mockito.*;

@SpringBootTest
@Import(ApplicationConfiguration.class)

class IncomingDataAppResourceTest {

	@InjectMocks
	IncomingDataAppResource incomingDAResource;

	@Mock
	MultiPartMessageServiceImpl multipartMessageServiceImplMock;

	@Mock
	CommunicationServiceImpl communicationServiceImpl;

	@Mock
	DapsServiceImpl dapsServiceImpl;

	@Mock
	private ApplicationConfiguration configuration;

	Message messageimpl = new MessageImplTest();
	HttpEntity multipart = new MultipartFormEntityTest();
	private static final String JWT_TOKEN = "ENG-Token";
	private static final String KEY_STORE_NAME = "Enginnering-keystore.key";
	private static final String ADDED_TOKEN = "Token143hg8";
	private static final String MULTIPATR_MESSAGE_HEADER = "Token143hg8";
	private static final String MULTIPATR_MESSAGE_PAYLOAD = "MMPayload";
	private static final String SENT_DATA_STATUS = "200 OK";
	private static final String SEND_DATA_ENPOINT = "http://ApplicationIncomeMessage/multipartMessage";
	private static final String APP_JSON = "Application/json";
	private static final ResponseEntity<?> RESPONSE_OK = new ResponseEntity<>(SENT_DATA_STATUS, HttpStatus.OK);
	private static final Message messageimplNull = null;
	private static final ResponseEntity<?> RESPONSE_OK_WHEN_MESSAGE_IS_NULL = ResponseEntity.ok().build();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void postMessageTestForSuccess() {

		// when
		// Getting the Key Store Name...
		when(configuration.getKeyStoreName()).thenReturn(KEY_STORE_NAME);

		// Getting the Message...
		when(multipartMessageServiceImplMock.getMessage(any(Object.class))).thenReturn(messageimpl);

		// Getting Token...
		when(dapsServiceImpl.getJwtToken()).thenReturn(JWT_TOKEN);

		// Adding Token to MultipartMessage...
		when(multipartMessageServiceImplMock.addToken(messageimpl, JWT_TOKEN)).thenReturn(ADDED_TOKEN);

		// Creating MultipartMessage...
		when(multipartMessageServiceImplMock.createMultipartMessage(MULTIPATR_MESSAGE_HEADER,
				MULTIPATR_MESSAGE_PAYLOAD)).thenReturn(multipart);

		// Sending Data...
		when(communicationServiceImpl.sendData(SEND_DATA_ENPOINT, multipart)).thenReturn(SENT_DATA_STATUS);

		// do
		ResponseEntity<?> result = incomingDAResource.postMessage(APP_JSON, SEND_DATA_ENPOINT, MULTIPATR_MESSAGE_HEADER,
				MULTIPATR_MESSAGE_PAYLOAD);

		// than
		// Assertion
		assertEquals("Error: response is not correct...", RESPONSE_OK, result);

		InOrder inOrder = inOrder(configuration, multipartMessageServiceImplMock, dapsServiceImpl,
				communicationServiceImpl);
		inOrder.verify(configuration, times(1)).getKeyStoreName();
		inOrder.verify(multipartMessageServiceImplMock, times(1)).getMessage(any(Object.class));
		inOrder.verify(dapsServiceImpl, times(1)).getJwtToken();
		inOrder.verify(multipartMessageServiceImplMock, times(1)).addToken(messageimpl, JWT_TOKEN);
		inOrder.verify(multipartMessageServiceImplMock, times(1)).createMultipartMessage(MULTIPATR_MESSAGE_HEADER,
				MULTIPATR_MESSAGE_PAYLOAD);
		inOrder.verify(communicationServiceImpl, times(1)).sendData(SEND_DATA_ENPOINT, multipart);

	}

	@Test
	public void postMessageTestIfMessageIsNull() {

		// when
		// Getting the Message...
		when(multipartMessageServiceImplMock.getMessage(any(Object.class))).thenReturn(messageimplNull);

		// do
		ResponseEntity<?> result = incomingDAResource.postMessage(APP_JSON, SEND_DATA_ENPOINT, MULTIPATR_MESSAGE_HEADER,
				MULTIPATR_MESSAGE_PAYLOAD);
		// than
		// Assertion
		assertEquals("Error : message is null...", result, RESPONSE_OK_WHEN_MESSAGE_IS_NULL);

	}

}
