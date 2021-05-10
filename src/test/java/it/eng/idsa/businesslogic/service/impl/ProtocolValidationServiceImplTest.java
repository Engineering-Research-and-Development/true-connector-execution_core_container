package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;

public class ProtocolValidationServiceImplTest {
	
    private static final String HTTPS = "https";
	
	private static final String HTTP = "http";
	
	private static final String WSS = "wss";
	
	@InjectMocks
	private ProtocolValidationServiceImpl protocolValidationServiceImpl;
	
	private boolean websocketBetweenECC;
	
	private boolean jetty;
	
	private String requiredECCProtocol;
	
	@Mock
	private RejectionMessageService rejectionMessageService;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		requiredECCProtocol = setRequiredProtocol(HTTPS);
		mockFieldsToProtocol(HTTPS);
	}
	
	
	@Test
	public void validateProtocolNoDelimiterInForwardTo() {
		String forwardTo = "mock.com";
		ReflectionTestUtils.setField(protocolValidationServiceImpl, "validateProtocol", true);
		protocolValidationServiceImpl.validateProtocol(forwardTo);
		verify(rejectionMessageService).sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES, null);
	}
	
	@Test
	public void validateProtocolWrongProtocolInForwardTo() {
		String forwardTo = "http://mock.com";
		ReflectionTestUtils.setField(protocolValidationServiceImpl, "validateProtocol", true);
		protocolValidationServiceImpl.validateProtocol(forwardTo);
		verify(rejectionMessageService).sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES, null);
	}
	
	@Test
	public void validateProtocolCorrectForwardTo() {
		String forwardTo = "https://mock.com";
		ReflectionTestUtils.setField(protocolValidationServiceImpl, "validateProtocol", true);
		String testForwardTo = protocolValidationServiceImpl.validateProtocol(forwardTo);
		verify(rejectionMessageService, times(0)).sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES, null);
		assertEquals(forwardTo, testForwardTo);
	}
	
	@Test
	public void noProtocolValidationWithDelimiterInForwardTo() {
		String forwardTo = "wss://mock.com";
		String expectedForwardTo = "https://mock.com";
		ReflectionTestUtils.setField(protocolValidationServiceImpl, "validateProtocol", false);
		String actualForwardTo = protocolValidationServiceImpl.validateProtocol(forwardTo);
		verify(rejectionMessageService, times(0)).sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES, null);
		assertEquals(actualForwardTo, expectedForwardTo);
	}
	
	@Test
	public void noProtocolValidationWithoutDelimiterInForwardTo() {
		String forwardTo = "mock.com";
		String expectedForwardTo = "https://mock.com";
		ReflectionTestUtils.setField(protocolValidationServiceImpl, "validateProtocol", false);
		String actualForwardTo = protocolValidationServiceImpl.validateProtocol(forwardTo);
		verify(rejectionMessageService, times(0)).sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES, null);
		assertEquals(actualForwardTo, expectedForwardTo);
	}
	
	private String setRequiredProtocol(String protocol) {
		if (HTTP.equals(protocol)) {
			jetty = false;
			websocketBetweenECC = false;
		}else if (HTTPS.equals(protocol)) {
			jetty = true;
			websocketBetweenECC = false;
		}else if (WSS.equals(protocol)) {
			jetty = true;
			websocketBetweenECC = true;
		}
		
		return protocol;
	}
	
	private void mockFieldsToProtocol(String protocol) {
		ReflectionTestUtils.setField(protocolValidationServiceImpl, "jetty", jetty);
		ReflectionTestUtils.setField(protocolValidationServiceImpl, "websocketBetweenECC", websocketBetweenECC);
		ReflectionTestUtils.setField(protocolValidationServiceImpl, "requiredECCProtocol", requiredECCProtocol);


	}
	
}



//validate protocol = true
//
//must match from Forward-To like configured
//validate protocol = false
//configured http:
//received wss -> must convert to http
//received https -> must convert to http
//received http -> must convert to http
//received dummy -> must convert to http
//received 'empty' -> must convert to http
//    
//empty - ecc-producer:8086/data
//-> http://ecc-producer:8086/data
