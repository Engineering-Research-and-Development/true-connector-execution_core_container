package it.eng.idsa.businesslogic.web.rest;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;



import org.apache.http.HttpEntity;
import org.junit.Before;

import org.junit.jupiter.api.Test;


import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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


@SpringBootTest
@Import(ApplicationConfiguration.class)

class IncomingDataChannelResourceTest {

	@InjectMocks
	IncomingDataChannelResource incomingDCResource;

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
	private static final String MULTIPATR_MESSAGE = "header_payload_message";
	private static final String RETURN_STATUS = "200 OK";
	private static final ResponseEntity<?> RESPONSE_OK = new ResponseEntity<>(HttpStatus.OK);
	private static final String RETURNED_HEADER = "header";
	private static final String RETURNED_PAYLOAD = "payload";
	private static final String RETURNED_TOKEN = "123";
	private static final String HEADER_WITHOUT_TOKEN = "headerWithoutToken";

	@Before
	public void init() {
	    MockitoAnnotations.initMocks(this);
	}

	@Test
	public void postReceivedMessageTestForSuccess() {

		// when
		// Getting Header
		when(multipartMessageServiceImplMock.getHeader(MULTIPATR_MESSAGE)).thenReturn(RETURNED_HEADER);

		// Getting Payload
		when(multipartMessageServiceImplMock.getPayload(MULTIPATR_MESSAGE)).thenReturn(RETURNED_PAYLOAD);

		// Getting Message
		when(multipartMessageServiceImplMock.getMessage(MULTIPATR_MESSAGE)).thenReturn(messageimpl);

		// Getting Token
		when(multipartMessageServiceImplMock.getToken(RETURNED_HEADER)).thenReturn(RETURNED_TOKEN);

		// Validate Token
		when(dapsServiceImpl.validateToken(RETURNED_TOKEN)).thenReturn(true);

		// Removing Token
		when(multipartMessageServiceImplMock.removeToken(messageimpl)).thenReturn(HEADER_WITHOUT_TOKEN);

		// Creating MultipartMessage without Token
		when(multipartMessageServiceImplMock.createMultipartMessage(HEADER_WITHOUT_TOKEN, RETURNED_PAYLOAD))
				.thenReturn(multipart);

		// Sending Data
		when(communicationServiceImpl.sendData(
				"http://" + configuration.getActivemqAddress() + "/api/message/outcoming?type=queue", multipart))
						.thenReturn(RETURN_STATUS);

		// do
		ResponseEntity<?> result = incomingDCResource.postReceivedMessage(MULTIPATR_MESSAGE);

		// than
		// Assertion
		assertEquals("Error: response is not correct...", RESPONSE_OK, result);

		InOrder inOrder = Mockito.inOrder(multipartMessageServiceImplMock, dapsServiceImpl, communicationServiceImpl);
		inOrder.verify(multipartMessageServiceImplMock, times(1)).getHeader(MULTIPATR_MESSAGE);
		inOrder.verify(multipartMessageServiceImplMock, times(1)).getPayload(MULTIPATR_MESSAGE);
		inOrder.verify(multipartMessageServiceImplMock, times(1)).getMessage(MULTIPATR_MESSAGE);
		inOrder.verify(multipartMessageServiceImplMock, times(1)).getToken(RETURNED_HEADER);
		inOrder.verify(dapsServiceImpl, times(1)).validateToken(RETURNED_TOKEN);
		inOrder.verify(multipartMessageServiceImplMock, times(1)).removeToken(messageimpl);
		inOrder.verify(multipartMessageServiceImplMock, times(1)).createMultipartMessage(HEADER_WITHOUT_TOKEN,
				RETURNED_PAYLOAD);
		inOrder.verify(communicationServiceImpl, times(1)).sendData(
				"http://" + configuration.getActivemqAddress() + "/api/message/outcoming?type=queue", multipart);

	}

	@Test
	public void postRecivedMessageTestIfTokenIsInvalid() {

		// when
		// Getting Header
		when(multipartMessageServiceImplMock.getHeader(MULTIPATR_MESSAGE)).thenReturn(RETURNED_HEADER);

		// Getting Payload
		when(multipartMessageServiceImplMock.getPayload(MULTIPATR_MESSAGE)).thenReturn(RETURNED_PAYLOAD);

		// Getting Message
		when(multipartMessageServiceImplMock.getMessage(MULTIPATR_MESSAGE)).thenReturn(messageimpl);

		// Getting Token
		when(multipartMessageServiceImplMock.getToken(RETURNED_HEADER)).thenReturn(RETURNED_TOKEN);

		// Validate Token
		when(dapsServiceImpl.validateToken(RETURNED_TOKEN)).thenReturn(false);

		// do
		ResponseEntity<?> result = incomingDCResource.postReceivedMessage(MULTIPATR_MESSAGE);

		// than
		// Assertion
		assertEquals("Error: response is not correct...", RESPONSE_OK, result);

	}

}
