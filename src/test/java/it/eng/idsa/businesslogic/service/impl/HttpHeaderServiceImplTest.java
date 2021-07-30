package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asynchttpclient.uri.Uri;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessageImpl;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.util.Util;
import it.eng.idsa.multipart.processor.util.TestUtilMessageService;
import okhttp3.Headers;

public class HttpHeaderServiceImplTest {
	
	private HttpHeaderServiceImpl httpHeaderServiceImpl;
	
	private Map<String, Object> headers;
	
	@BeforeEach
	public void init() {
		httpHeaderServiceImpl = new HttpHeaderServiceImpl();
		
		headers = new HashMap<String, Object>();
		headers.put("IDS-Messagetype","ids:ArtifactResponseMessage");
		headers.put("IDS-Issued","2019-05-27T13:09:42.306Z");
		headers.put("IDS-IssuerConnector","http://iais.fraunhofer.de/ids/mdm-connector");
		headers.put("IDS-CorrelationMessage","http://industrialdataspace.org/connectorUnavailableMessage/1a421b8c-3407-44a8-aeb9-253f145c869a");
		headers.put("IDS-TransferContract","https://mdm-connector.ids.isst.fraunhofer.de/examplecontract/bab-bayern-sample/");
		headers.put("IDS-Id","https://w3id.org/idsa/autogen/artifactResponseMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f");
		headers.put("IDS-ModelVersion","4.0.0");
		headers.put("IDS-RequestedArtifact", "http://mdm-connector.ids.isst.fraunhofer.de/artifact/1");
		headers.put("foo", "bar");
		headers.put("Forward-To", "https://forwardToURL");
	}
	
	@Test
	public void messageToHeadersTest_ArtifactRequestMessage() {
		Message message = TestUtilMessageService.getArtifactRequestMessageWithToken();
		
		Map<String, Object> headers = httpHeaderServiceImpl.messageToHeaders(message);
		assertNotNull(headers.entrySet());
		assertEquals(headers.get("IDS-Messagetype"), "ids:ArtifactRequestMessage");
		assertEquals(headers.get("IDS-Id"), message.getId().toString());
		assertEquals(headers.get("IDS-SecurityToken-TokenValue"), message.getSecurityToken().getTokenValue());
	}
	
	@Test
	public void headersToMessageTest_ArtifactRequestMessage() {
		ArtifactRequestMessage originalMessage = TestUtilMessageService.getArtifactRequestMessageWithToken();
		Map<String, Object> headers = httpHeaderServiceImpl.messageToHeaders(originalMessage);
		ArtifactRequestMessage message = (ArtifactRequestMessage) httpHeaderServiceImpl.headersToMessage(headers);
		assertNotNull(message);
		assertTrue(message instanceof ArtifactRequestMessage);
		assertEquals(originalMessage.getId(), message.getId());
		assertEquals(originalMessage.getRequestedArtifact(), message.getRequestedArtifact());

		assertNotNull(message.getSecurityToken());
		assertEquals(TestUtilMessageService.TOKEN_VALUE, message.getSecurityToken().getTokenValue());
	}
	
	@Test
	public void messageToHeadersTest_DescriptionRequestMessage() {
		Message message = TestUtilMessageService.getDescriptionRequestMessage();
		
		Map<String, Object> headers = httpHeaderServiceImpl.messageToHeaders(message);
		assertNotNull(headers.entrySet());
		assertEquals(headers.get("IDS-Messagetype"), "ids:DescriptionRequestMessage");
		assertEquals(headers.get("IDS-Id"), message.getId().toString());
		// TODO token not present with current info model version
//		assertEquals(headers.get("IDS-SecurityToken-Value"), message.getSecurityToken().getTokenValue());
	}
	
	@Test
	public void headersToMessageTest_ArtifactRequestMessage_AdditionalHeaders() {
		ArtifactRequestMessage originalMessage = TestUtilMessageService.getArtifactRequestMessageWithToken();
		Map<String, Object> headers = httpHeaderServiceImpl.messageToHeaders(originalMessage);
		headers.put("Accept", "*/*");
		headers.put("foo", null);

		ArtifactRequestMessage message = (ArtifactRequestMessage) httpHeaderServiceImpl.headersToMessage(headers);
		assertNotNull(message);
		assertTrue(message instanceof ArtifactRequestMessage);
		assertEquals(originalMessage.getId(), message.getId());
		assertEquals(originalMessage.getRequestedArtifact(), message.getRequestedArtifact());

		assertNotNull(message.getSecurityToken());
		assertEquals(TestUtilMessageService.TOKEN_VALUE, message.getSecurityToken().getTokenValue());
	}
	
	@Test
	public void artifactResponseMessageToHeaders() {
		ArtifactResponseMessage artifactResponseMessage = TestUtilMessageService.getArtifactResponseMessage();
		ArrayList<URI> recipentConnector = Util.asList(
				URI.create("https://connector1.com"),
				URI.create("https://connector2.com"));
		
		((ArtifactResponseMessageImpl) artifactResponseMessage ).setRecipientConnector(recipentConnector);
		
		Map<String, Object> headers = httpHeaderServiceImpl.messageToHeaders(artifactResponseMessage);
		assertEquals(2, ((List<URI>) headers.get("IDS-recipientConnector")).size());
	}
	
	@Test
	public void artifactResponseMessageToMessage() {
		ArtifactResponseMessage artifactResponseMessage = TestUtilMessageService.getArtifactResponseMessage();
		ArrayList<URI> recipentConnector = Util.asList(
				URI.create("https://connector1.com"),
				URI.create("https://connector2.com"));
		((ArtifactResponseMessageImpl) artifactResponseMessage ).setRecipientConnector(recipentConnector);
		Map<String, Object> headers = httpHeaderServiceImpl.messageToHeaders(artifactResponseMessage);
		
		ArtifactResponseMessage result = (ArtifactResponseMessage) httpHeaderServiceImpl.headersToMessage(headers);
		assertNotNull(result);
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
		hb.add("IDS-InfoModel", "4.0.0");
		
		Map<String, Object> headersAsMap = httpHeaderServiceImpl.okHttpHeadersToMap(hb.build());
		assertEquals(headersAsMap.get("IDS-MessageType"), "ids:ArtifactRequestMessage");
		assertEquals(((List<String>) headersAsMap.get("IDS-RecipientConnector")).size(), 2);
		assertEquals(((List<String>) headersAsMap.get("IDS-recipientAgent")).size(), 2);
		assertEquals(headersAsMap.get("IDS-InfoModel"), "4.0.0");
	}
}
