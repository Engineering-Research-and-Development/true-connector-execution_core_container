package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessageImpl;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.util.Util;
import it.eng.idsa.multipart.util.UtilMessageService;
import okhttp3.Headers;

public class HttpHeaderServiceImplTest {
	
	private HttpHeaderServiceImpl httpHeaderServiceImpl;
	
	private Map<String, Object> headers;
	
	@BeforeEach
	public void init() {
		httpHeaderServiceImpl = new HttpHeaderServiceImpl();
		
		headers = new HashMap<String, Object>();
		headers.put("IDS-Messagetype","ids:ArtifactResponseMessage");
		headers.put("IDS-Issued", UtilMessageService.ISSUED.toString());
		headers.put("IDS-IssuerConnector", UtilMessageService.ISSUER_CONNECTOR);
		headers.put("IDS-CorrelationMessage", UtilMessageService.CORRELATION_MESSAGE);
		headers.put("IDS-TransferContract", UtilMessageService.TRANSFER_CONTRACT);
		headers.put("IDS-Id","https://w3id.org/idsa/autogen/artifactResponseMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f");
		headers.put("IDS-ModelVersion", UtilMessageService.MODEL_VERSION);
		headers.put("IDS-RequestedArtifact", "http://w3id.org/engrd/connector/artifact/1");
		headers.put("foo", "bar");
		headers.put("Forward-To", "https://forwardToURL");
	}
	
	@Test
	public void messageToHeadersTest_ArtifactRequestMessage() {
		Message message = UtilMessageService.getArtifactRequestMessage();
		
		Map<String, Object> headers = httpHeaderServiceImpl.messageToHeaders(message);
		assertNotNull(headers.entrySet());
		assertEquals(headers.get("IDS-Messagetype"), "ids:ArtifactRequestMessage");
		assertEquals(headers.get("IDS-Id"), message.getId().toString());
		assertEquals(headers.get("IDS-SecurityToken-TokenValue"), message.getSecurityToken().getTokenValue());
	}
	
	@Test
	public void headersToMessageTest_ArtifactRequestMessage() {
		ArtifactRequestMessage originalMessage = UtilMessageService.getArtifactRequestMessage();
		Map<String, Object> headers = httpHeaderServiceImpl.messageToHeaders(originalMessage);
		ArtifactRequestMessage message = (ArtifactRequestMessage) httpHeaderServiceImpl.headersToMessage(headers);
		assertNotNull(message);
		assertTrue(message instanceof ArtifactRequestMessage);
		assertEquals(originalMessage.getId(), message.getId());
		assertEquals(originalMessage.getRequestedArtifact(), message.getRequestedArtifact());

		assertNotNull(message.getSecurityToken());
		assertEquals(UtilMessageService.TOKEN_VALUE, message.getSecurityToken().getTokenValue());
		// verify that message is serialized correct and that there are no properties that could not be parsed
		assertNull(message.getProperties());
	}
	
	@Test
	public void messageToHeadersTest_DescriptionRequestMessage() {
		Message message = UtilMessageService.getDescriptionRequestMessage(null);
		
		Map<String, Object> headers = httpHeaderServiceImpl.messageToHeaders(message);
		assertNotNull(headers.entrySet());
		assertEquals(headers.get("IDS-Messagetype"), "ids:DescriptionRequestMessage");
		assertEquals(headers.get("IDS-Id"), message.getId().toString());
		assertEquals(headers.get("IDS-SecurityToken-TokenValue"), message.getSecurityToken().getTokenValue());
	}
	
	@Test
	public void headersToMessageTest_ArtifactRequestMessage_AdditionalHeaders() {
		ArtifactRequestMessage originalMessage = UtilMessageService.getArtifactRequestMessage();
		Map<String, Object> headers = httpHeaderServiceImpl.messageToHeaders(originalMessage);
		headers.put("Accept", "*/*");
		headers.put("foo", null);

		ArtifactRequestMessage message = (ArtifactRequestMessage) httpHeaderServiceImpl.headersToMessage(headers);
		assertNotNull(message);
		assertTrue(message instanceof ArtifactRequestMessage);
		assertEquals(originalMessage.getId(), message.getId());
		assertEquals(originalMessage.getRequestedArtifact(), message.getRequestedArtifact());

		assertNotNull(message.getSecurityToken());
		assertEquals(UtilMessageService.TOKEN_VALUE, message.getSecurityToken().getTokenValue());
		// verify that message is serialized correct and that there are no properties that could not be parsed
		assertNull(message.getProperties());
	}
	
	@Test
	public void artifactResponseMessageToHeaders() {
		ArtifactResponseMessage artifactResponseMessage = UtilMessageService.getArtifactResponseMessage();
		ArrayList<URI> recipentConnector = Util.asList(
				URI.create("https://connector1.com"),
				URI.create("https://connector2.com"));
		
		((ArtifactResponseMessageImpl) artifactResponseMessage ).setRecipientConnector(recipentConnector);
		
		Map<String, Object> headers = httpHeaderServiceImpl.messageToHeaders(artifactResponseMessage);
		assertEquals(2, ((List<URI>) headers.get("IDS-RecipientConnector")).size());
	}
	
	@Test
	public void artifactResponseMessageToMessage() {
		ArtifactResponseMessage artifactResponseMessage = UtilMessageService.getArtifactResponseMessage();
		ArrayList<URI> recipentConnector = Util.asList(
				URI.create("https://connector1.com"),
				URI.create("https://connector2.com"));
		((ArtifactResponseMessageImpl) artifactResponseMessage ).setRecipientConnector(recipentConnector);
		Map<String, Object> headers = httpHeaderServiceImpl.messageToHeaders(artifactResponseMessage);
		
		ArtifactResponseMessage result = (ArtifactResponseMessage) httpHeaderServiceImpl.headersToMessage(headers);
		assertNotNull(result);
		// verify that message is serialized correct and that there are no properties that could not be parsed
		assertNull(result.getProperties());
	}
	
	@Test
	public void okHttpHeadersToMapTest() {
		Headers.Builder hb = new Headers.Builder();

		hb.add("IDS-MessageType", "ids:ArtifactRequestMessage");
		hb.add("IDS-recipientAgent", "https://agent1.com");
		hb.add("IDS-recipientAgent", "https://agent2.com");
		hb.add("IDS-Id", "https://www.id.com");
		hb.add("IDS-RecipientConnector", "https://connector1.com");
		hb.add("IDS-RecipientConnector", "https://connector2.com");
		hb.add("IDS-InfoModel", UtilMessageService.MODEL_VERSION);
		
		Map<String, Object> headersAsMap = httpHeaderServiceImpl.okHttpHeadersToMap(hb.build());
		assertEquals(headersAsMap.get("IDS-MessageType"), "ids:ArtifactRequestMessage");
		assertEquals(((List<String>) headersAsMap.get("IDS-RecipientConnector")).size(), 2);
		assertEquals(((List<String>) headersAsMap.get("IDS-RecipientAgent")).size(), 2);
		assertEquals(headersAsMap.get("IDS-InfoModel"), UtilMessageService.MODEL_VERSION);
	}
}
