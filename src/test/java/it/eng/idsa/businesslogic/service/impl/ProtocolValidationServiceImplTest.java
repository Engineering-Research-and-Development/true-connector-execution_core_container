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

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.multipart.util.UtilMessageService;

public class ProtocolValidationServiceImplTest {
	
    private static final String HTTPS = "https";
	private static final String HTTP = "http";
	private static final String WSS = "wss";
	
	@InjectMocks
	private ProtocolValidationServiceImpl protocolValidationService;
	
	private boolean websocketBetweenECC;
	
	private boolean jetty;
	
	private String requiredECCProtocol;
	
	@Mock
	private RejectionMessageService rejectionMessageService;
	
	private Message message;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	public void validateProtocolNoDelimiterInForwardTo() {
		requiredECCProtocol = setRequiredProtocol(HTTPS);
		mockFieldsToProtocol(HTTPS);
		String forwardTo = "mock.com";
		message = UtilMessageService.getArtifactRequestMessage();
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", true);
		protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
	}
	
	@Test
	public void validateProtocolWrongProtocolInForwardToReceivedHttpRequiredHttps() {
		requiredECCProtocol = setRequiredProtocol(HTTPS);
		mockFieldsToProtocol(HTTPS);
		String forwardTo = "http://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", true);
		protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
	}
	
	@Test
	public void validateProtocolWrongProtocolInForwardToReceivedWssRequiredHttps() {
		requiredECCProtocol = setRequiredProtocol(HTTPS);
		mockFieldsToProtocol(HTTPS);
		String forwardTo = "wss://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", true);
		protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
	}
	
	@Test
	public void validateProtocolWrongProtocolInForwardToReceivedNoProtocolRequiredHttps() {
		requiredECCProtocol = setRequiredProtocol(HTTPS);
		mockFieldsToProtocol(HTTPS);
		String forwardTo = "://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", true);
		protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
	}
	
	@Test
	public void validateProtocolWrongProtocolInForwardToReceivedNonStandardProtocolRequiredHttps() {
		requiredECCProtocol = setRequiredProtocol(HTTPS);
		mockFieldsToProtocol(HTTPS);
		String forwardTo = "dummy://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", true);
		protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
	}
	
	@Test
	public void validateProtocolCorrectForwardToReceivedHttpsRequiredHttps() {
		requiredECCProtocol = setRequiredProtocol(HTTPS);
		mockFieldsToProtocol(HTTPS);
		String forwardTo = "https://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", true);
		String testForwardTo = protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService, times(0)).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
		assertEquals(forwardTo, testForwardTo);
	}

	@Test
	public void validateProtocolWrongProtocolInForwardToReceivedHttpsRequiredHttp() {
		requiredECCProtocol = setRequiredProtocol(HTTP);
		mockFieldsToProtocol(HTTP);
		String forwardTo = "https://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", true);
		protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
	}
	
	@Test
	public void validateProtocolWrongProtocolInForwardToReceivedWssRequiredHttp() {
		requiredECCProtocol = setRequiredProtocol(HTTP);
		mockFieldsToProtocol(HTTP);
		String forwardTo = "wss://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", true);
		protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
	}
	
	@Test
	public void validateProtocolWrongProtocolInForwardToReceivedNoProtocolRequiredHttp() {
		requiredECCProtocol = setRequiredProtocol(HTTP);
		mockFieldsToProtocol(HTTP);
		String forwardTo = "://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", true);
		protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
	}
	
	@Test
	public void validateProtocolWrongProtocolInForwardToReceivedNonStandardProtocolRequiredHttp() {
		requiredECCProtocol = setRequiredProtocol(HTTP);
		mockFieldsToProtocol(HTTP);
		String forwardTo = "dummy://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", true);
		protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
	}
	
	@Test
	public void validateProtocolCorrectForwardToReceivedHttpRequiredHttp() {
		requiredECCProtocol = setRequiredProtocol(HTTP);
		mockFieldsToProtocol(HTTP);
		String forwardTo = "http://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", true);
		String testForwardTo = protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService, times(0)).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
		assertEquals(forwardTo, testForwardTo);
	}

	@Test
	public void validateProtocolWrongProtocolInForwardToReceivedHttpsRequiredWss() {
		requiredECCProtocol = setRequiredProtocol(WSS);
		mockFieldsToProtocol(WSS);
		String forwardTo = "https://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", true);
		protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
	}
	
	@Test
	public void validateProtocolWrongProtocolInForwardToReceivedHttpRequiredWss() {
		requiredECCProtocol = setRequiredProtocol(WSS);
		mockFieldsToProtocol(WSS);
		String forwardTo = "http://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", true);
		protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
	}
	
	@Test
	public void validateProtocolWrongProtocolInForwardToReceivedNoProtocolRequiredWss() {
		requiredECCProtocol = setRequiredProtocol(WSS);
		mockFieldsToProtocol(WSS);
		String forwardTo = "://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", true);
		protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
	}
	
	@Test
	public void validateProtocolWrongProtocolInForwardToReceivedNonStandardProtocolRequiredWss() {
		requiredECCProtocol = setRequiredProtocol(WSS);
		mockFieldsToProtocol(WSS);
		String forwardTo = "dummy://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", true);
		protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
	}

	@Test
	public void validateProtocolCorrectForwardToReceivedHttpRequiredWss() {
		requiredECCProtocol = setRequiredProtocol(WSS);
		mockFieldsToProtocol(WSS);
		String forwardTo = "wss://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", true);
		String testForwardTo = protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService, times(0)).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
		assertEquals(forwardTo, testForwardTo);
	}

	@Test
	public void noProtocolValidationReceivedHttpRequiredHttps() {
		requiredECCProtocol = setRequiredProtocol(HTTPS);
		mockFieldsToProtocol(HTTPS);
		String forwardTo = "http://mock.com";
		String expectedForwardTo = "https://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", false);
		String actualForwardTo = protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService, times(0)).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
		assertEquals(actualForwardTo, expectedForwardTo);
	}
	
	@Test
	public void noProtocolValidationReceivedHttpsRequiredHttps() {
		requiredECCProtocol = setRequiredProtocol(HTTPS);
		mockFieldsToProtocol(HTTPS);
		String forwardTo = "https://mock.com";
		String expectedForwardTo = "https://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", false);
		String actualForwardTo = protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService, times(0)).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
		assertEquals(actualForwardTo, expectedForwardTo);
	}
	
	@Test
	public void noProtocolValidationReceivedWssRequiredHttps() {
		requiredECCProtocol = setRequiredProtocol(HTTPS);
		mockFieldsToProtocol(HTTPS);
		String forwardTo = "wss://mock.com";
		String expectedForwardTo = "https://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", false);
		String actualForwardTo = protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService, times(0)).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
		assertEquals(actualForwardTo, expectedForwardTo);
	}
	
	@Test
	public void noProtocolValidationReceivedNonStandardProtocolRequiredHttps() {
		requiredECCProtocol = setRequiredProtocol(HTTPS);
		mockFieldsToProtocol(HTTPS);
		String forwardTo = "dummy://mock.com";
		String expectedForwardTo = "https://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", false);
		String actualForwardTo = protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService, times(0)).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
		assertEquals(actualForwardTo, expectedForwardTo);
	}
	
	@Test
	public void noProtocolValidationReceivedHttpRequiredHttp() {
		requiredECCProtocol = setRequiredProtocol(HTTP);
		mockFieldsToProtocol(HTTP);
		String forwardTo = "http://mock.com";
		String expectedForwardTo = "http://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", false);
		String actualForwardTo = protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService, times(0)).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
		assertEquals(actualForwardTo, expectedForwardTo);
	}
	
	@Test
	public void noProtocolValidationReceivedHttpsRequiredHttp() {
		requiredECCProtocol = setRequiredProtocol(HTTP);
		mockFieldsToProtocol(HTTP);
		String forwardTo = "https://mock.com";
		String expectedForwardTo = "http://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", false);
		String actualForwardTo = protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService, times(0)).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
		assertEquals(actualForwardTo, expectedForwardTo);
	}
	
	@Test
	public void noProtocolValidationReceivedWssRequiredHttp() {
		requiredECCProtocol = setRequiredProtocol(HTTP);
		mockFieldsToProtocol(HTTP);
		String forwardTo = "wss://mock.com";
		String expectedForwardTo = "http://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", false);
		String actualForwardTo = protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService, times(0)).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
		assertEquals(actualForwardTo, expectedForwardTo);
	}
	
	@Test
	public void noProtocolValidationReceivedNonStandardProtocolRequiredHttp() {
		requiredECCProtocol = setRequiredProtocol(HTTP);
		mockFieldsToProtocol(HTTP);
		String forwardTo = "dummy://mock.com";
		String expectedForwardTo = "http://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", false);
		String actualForwardTo = protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService, times(0)).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
		assertEquals(actualForwardTo, expectedForwardTo);
	}
	
	@Test
	public void noProtocolValidationReceivedHttpRequiredWss() {
		requiredECCProtocol = setRequiredProtocol(WSS);
		mockFieldsToProtocol(WSS);
		String forwardTo = "http://mock.com";
		String expectedForwardTo = "wss://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", false);
		String actualForwardTo = protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService, times(0)).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
		assertEquals(actualForwardTo, expectedForwardTo);
	}
	
	@Test
	public void noProtocolValidationReceivedHttpsRequiredWss() {
		requiredECCProtocol = setRequiredProtocol(WSS);
		mockFieldsToProtocol(WSS);
		String forwardTo = "https://mock.com";
		String expectedForwardTo = "wss://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", false);
		String actualForwardTo = protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService, times(0)).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
		assertEquals(actualForwardTo, expectedForwardTo);
	}
	
	@Test
	public void noProtocolValidationReceivedWssRequiredWss() {
		requiredECCProtocol = setRequiredProtocol(WSS);
		mockFieldsToProtocol(WSS);
		String forwardTo = "wss://mock.com";
		String expectedForwardTo = "wss://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", false);
		String actualForwardTo = protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService, times(0)).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
		assertEquals(actualForwardTo, expectedForwardTo);
	}
	
	@Test
	public void noProtocolValidationReceivedNonStandardProtocolRequiredWss() {
		requiredECCProtocol = setRequiredProtocol(WSS);
		mockFieldsToProtocol(WSS);
		String forwardTo = "dummy://mock.com";
		String expectedForwardTo = "wss://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", false);
		String actualForwardTo = protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService, times(0)).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
		assertEquals(actualForwardTo, expectedForwardTo);
	}
	
	@Test
	public void noProtocolValidationWithoutDelimiterInForwardToRequiredHttps() {
		requiredECCProtocol = setRequiredProtocol(HTTPS);
		mockFieldsToProtocol(HTTPS);
		String forwardTo = "mock.com";
		String expectedForwardTo = "https://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", false);
		String actualForwardTo = protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService, times(0)).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
		assertEquals(actualForwardTo, expectedForwardTo);
	}
	
	@Test
	public void noProtocolValidationWithoutDelimiterInForwardToRequiredHttp() {
		requiredECCProtocol = setRequiredProtocol(HTTP);
		mockFieldsToProtocol(HTTP);
		String forwardTo = "mock.com";
		String expectedForwardTo = "http://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", false);
		String actualForwardTo = protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService, times(0)).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
		assertEquals(actualForwardTo, expectedForwardTo);
	}
	
	@Test
	public void noProtocolValidationWithoutDelimiterInForwardToRequiredWss() {
		requiredECCProtocol = setRequiredProtocol(WSS);
		mockFieldsToProtocol(WSS);
		String forwardTo = "mock.com";
		String expectedForwardTo = "wss://mock.com";
		ReflectionTestUtils.setField(protocolValidationService, "validateProtocol", false);
		String actualForwardTo = protocolValidationService.validateProtocol( forwardTo, message);
		verify(rejectionMessageService, times(0)).sendRejectionMessage(message, RejectionReason.BAD_PARAMETERS);
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
		ReflectionTestUtils.setField(protocolValidationService, "jetty", jetty);
		ReflectionTestUtils.setField(protocolValidationService, "websocketBetweenECC", websocketBetweenECC);
		ReflectionTestUtils.setField(protocolValidationService, "requiredECCProtocol", requiredECCProtocol);


	}
	
}
