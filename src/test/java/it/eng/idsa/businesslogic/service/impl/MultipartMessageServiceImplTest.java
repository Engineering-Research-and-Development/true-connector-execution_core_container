package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;

import javax.xml.datatype.DatatypeConfigurationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.TestUtilMessageService;

public class MultipartMessageServiceImplTest {
	
	@Mock
	private RejectionMessageService rejectionMessageService;
	
	private MultipartMessageServiceImpl service;
	
	@BeforeEach
	public void setup() throws ConstraintViolationException, URISyntaxException {
		MockitoAnnotations.initMocks(this);
		service = new MultipartMessageServiceImpl();
	}
	
	@Test
	@Disabled
	public void testParseJsonLdMessageOnlyHeader() {
		String message = "--5h-mAJBSLPxhWW0KWTF-eHuB2KedYdNJMF2uYvF\r\n" + 
				"Content-Disposition: form-data; name=\"header\"\r\n" + 
				"Content-Type: application/json+ld\r\n" + 
				"Content-Length: 1070\r\n" + 
				"\r\n" + 
				"{\r\n" + 
				"  \"@context\" : {\r\n" + 
				"    \"ids\" : \"https://w3id.org/idsa/core/\",\r\n" + 
				"    \"idsc\" : \"https://w3id.org/idsa/code/\"\r\n" + 
				"  },\r\n" + 
				"  \"@type\" : \"ids:MessageProcessedNotificationMessage\",\r\n" + 
				"  \"@id\" : \"https://w3id.org/idsa/autogen/messageProcessedNotificationMessage/7fc336b9-8b90-4f2a-a34d-a6c0cb716fa4\",\r\n" + 
				"  \"ids:correlationMessage\" : {\r\n" + 
				"    \"@id\" : \"https://w3id.org/idsa/autogen/connectorAvailableMessage/56d2bac7-a8b8-4c14-95ce-d3e6770fe018\"\r\n" + 
				"  },\r\n" + 
				"  \"ids:senderAgent\" : {\r\n" + 
				"    \"@id\" : \"https://www.iais.fraunhofer.de\"\r\n" + 
				"  },\r\n" + 
				"  \"ids:issued\" : {\r\n" + 
				"    \"@value\" : \"2020-09-18T12:29:57.142+02:00\",\r\n" + 
				"    \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"  },\r\n" + 
				"  \"ids:modelVersion\" : \"3.1.4\",\r\n" + 
				"  \"ids:issuerConnector\" : {\r\n" + 
				"    \"@id\" : \"https://ids0.datahub.c.fraunhofer.de/\"\r\n" + 
				"  },\r\n" + 
				"  \"ids:securityToken\" : {\r\n" + 
				"    \"@type\" : \"ids:DynamicAttributeToken\",\r\n" + 
				"    \"@id\" : \"https://w3id.org/idsa/autogen/dynamicAttributeToken/30a5fbce-fcfe-4248-ab76-d096d1dcab09\",\r\n" + 
				"    \"ids:tokenValue\" : \"DummyTokenAsNoDapsIsAvailable\",\r\n" + 
				"    \"ids:tokenFormat\" : {\r\n" + 
				"      \"@id\" : \"idsc:JWT\"\r\n" + 
				"    }\r\n" + 
				"  }\r\n" + 
				"}\r\n" + 
				"--5h-mAJBSLPxhWW0KWTF-eHuB2KedYdNJMF2uYvF--\r\n";
		// Workaround - removing Content-Type in response
		String[] lines = message.split(System.getProperty("line.separator"));
		for(int i=0;i<lines.length;i++){
		    if(lines[i].startsWith("Content-Type")){
		        lines[i]="";
		    }
		}
		StringBuilder finalStringBuilder= new StringBuilder("");
		for(String s:lines){
		   if(!s.equals("")){
		       finalStringBuilder.append(s).append(System.getProperty("line.separator"));
		    }
		}
		String finalString = finalStringBuilder.toString();
		
		String header = service.getHeaderContentString(finalString);
		String payload = service.getPayloadContent(finalString);
		
		assertNotNull(header);
		assertNull(payload);
	}
	
	@Test
	public void removeToken() throws ConstraintViolationException, URISyntaxException, DatatypeConfigurationException {
		String result = service.removeToken(TestUtilMessageService.getArtifactRequestMessageWithToken());
		assertNotNull(result);
		assertFalse(result.contains("securityToken"));
	}
	
	@Test
	public void addToken() {
		Message message = TestUtilMessageService.getArtifactRequestMessage();
		String token = "DUMMY_TOKEN_VALUE";
		String messageWithToken = service.addToken(message, token );
		assertNotNull(messageWithToken);
		assertTrue(messageWithToken.contains(token));
	}
	
	@Test
	public void getTokenTest() throws JsonProcessingException {
		String token = service.getToken(TestUtilMessageService.getArtifactRequestMessageWithToken());
		assertNotNull(token);
	}
}
