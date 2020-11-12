package it.eng.idsa.businesslogic.service.impl;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.SendDataToBusinessLogicService;

public class SendDataToBusinessLogicServiceImplTest {
	
	@InjectMocks
	SendDataToBusinessLogicService service; 
	
	@Mock
	private RejectionMessageService rejectionMessageService;
	
	@Mock
	private MultipartMessageService multipartMessageService;
	
	String header;
	
	String payload;
	
	String address;
	
	Map<String, Object> headerParts;
	
	
	@BeforeEach
	void init() {
		header = "{\r\n" + 
				"  \"@type\" : \"ids:ArtifactResponseMessage\",\r\n" + 
				"  \"@id\" : \"https://w3id.org/idsa/autogen/artifactResponseMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f\",\r\n" + 
				"  \"ids:issuerConnector\" : \"http://iais.fraunhofer.de/ids/mdm-connector\",\r\n" + 
				"  \"ids:modelVersion\" : \"1.0.2-SNAPSHOT\",\r\n" + 
				"  \"ids:issued\" : \"2019-05-27T13:09:42.306Z\",\r\n" + 
				"  \"ids:correlationMessage\" : \"http://industrialdataspace.org/connectorUnavailableMessage/1a421b8c-3407-44a8-aeb9-253f145c869a\",\r\n" + 
				"  \"ids:transferContract\" : \"https://mdm-connector.ids.isst.fraunhofer.de/examplecontract/bab-bayern-sample/\"\r\n" + 
				"}";
		
		payload = "{\"catalog.offers.0.resourceEndpoints.path\":\"/pet2\"}";
		
		address = "https://localhost:8890/incoming-data-channel/receivedMessage";
		
		headerParts = new HashMap<>();
	}
	
	@Test
	void sendMessageBinary_Success() throws UnsupportedEncodingException {
//		service.sendMessageBinary(address, header, payload, headerParts);
		
	}

}
