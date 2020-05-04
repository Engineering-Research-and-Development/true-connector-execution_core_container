package it.eng.idsa.businesslogic.multipart.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import it.eng.idsa.businesslogic.multipart.MultipartMessage;
import it.eng.idsa.businesslogic.multipart.MultipartMessageBuilder;
import it.eng.idsa.businesslogic.multipart.MultipartMessageKey;
import it.eng.idsa.businesslogic.service.impl.MultipartMessageTransformerServiceImpl;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MultipartMessageTransformerServiceTest {
	
	@Autowired
	private MultipartMessageTransformerServiceImpl multipartMessageTransformerService;
	
	private String RESOURCE_MESSAGE_PATH = "./src/test/resources/message/";
	private String MULTIPART_MESSAGE_NAME = "IDS-multipart.txt";
	
	private Map<String, String> expectedHttpHeader  = new HashMap<String, String>() {{
	    put(MultipartMessageKey.CONTENT_TYPE.label, "Content-Type: multipart/mixed; boundary=CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6;charset=UTF-8");
	    put(MultipartMessageKey.FORWARD_TO.label, "Forward-To: broker");
	}}; 
	
	private String expectedHeaderContentString = "{"  + System.lineSeparator() +
			"  \"@type\" : \"ids:ArtifactResponseMessage\"," + System.lineSeparator() + 
			"  \"issued\" : \"2019-05-27T13:09:42.306Z\"," + System.lineSeparator() +
			"  \"issuerConnector\" : \"http://iais.fraunhofer.de/ids/mdm-connector\"," + System.lineSeparator() +
			"  \"correlationMessage\" : \"http://industrialdataspace.org/connectorUnavailableMessage/1a421b8c-3407-44a8-aeb9-253f145c869a\","  + System.lineSeparator() +  
			"  \"transferContract\" : \"https://mdm-connector.ids.isst.fraunhofer.de/examplecontract/bab-bayern-sample/\","  + System.lineSeparator() +
			"  \"modelVersion\" : \"1.0.2-SNAPSHOT\"," + System.lineSeparator() + 
			"  \"@id\" : \"https://w3id.org/idsa/autogen/artifactResponseMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f\"" + System.lineSeparator() +  
			"}";
	private String expectedPayloadContentString = "{\"catalog.offers.0.resourceEndpoints.path\":\"/pet\"}";
	private String expectedSignatureContentString = "{\"signature.resourceEndpoints.path\":\"/signature\"}";
	
	private Map<String, String> expectedHeaderHeader  = new HashMap<String, String>() {{
	    put(MultipartMessageKey.CONTENT_DISPOSITION.label, "Content-Disposition: form-data; name=\"header\"");
	    put(MultipartMessageKey.CONTENT_LENGTH.label, "Content-Length: 534");
	}};
	
	private Map<String, String> expectedPayloadHeader  = new HashMap<String, String>() {{
	    put(MultipartMessageKey.CONTENT_DISPOSITION.label, "Content-Disposition: form-data; name=\"payload\"");
	    put(MultipartMessageKey.CONTENT_LENGTH.label, "Content-Length: 50");
	}};
	
	private Map<String, String> expectedSignatureHeader  = new HashMap<String, String>() {{
	    put(MultipartMessageKey.CONTENT_DISPOSITION.label, "Content-Disposition: form-data; name=\"signature\"");
	    put(MultipartMessageKey.CONTENT_LENGTH.label, "Content-Length: 49");
	}};
	
	
	private String multipartMessageString;
	
	private MultipartMessage expectedMultipartMessage;

	@Before
    public void init() throws IOException {
		multipartMessageString = new String(Files.readAllBytes(Paths.get(RESOURCE_MESSAGE_PATH + MULTIPART_MESSAGE_NAME)));
		expectedMultipartMessage = new MultipartMessageBuilder()
															.withHttpHeader(expectedHttpHeader)
															.withHeaderHeader(expectedHeaderHeader)
															.withHeaderContent(expectedHeaderContentString)
															.withPayloadHeader(expectedPayloadHeader)
															.withPayloadContent(expectedPayloadContentString)
															.withSignatureHeader(expectedSignatureHeader)
															.withSignatureContent(expectedSignatureContentString)
															.build();
    }

	// test WITHOUT HttpHeader
	@Test
	public void testParseMultipartMessageWithoutHttpHeader() {
		
		// do
		MultipartMessage resultMultipartMessage = multipartMessageTransformerService.parseMultipartMessage(multipartMessageString);
		
		// then
		assertTrue("httpHeader shuould be empty", resultMultipartMessage.getHttpHeaders().isEmpty());
		assertTrue("headerContents, payloadContents and signatureContents are not equals", expectedMultipartMessage.equals(resultMultipartMessage));
		assertEquals("headerHeaders::Content-Disposition are not equals", expectedMultipartMessage.getHeaderHeader().get(MultipartMessageKey.CONTENT_DISPOSITION.label).toString(), resultMultipartMessage.getHeaderHeader().get(MultipartMessageKey.CONTENT_DISPOSITION.label).toString());
		assertEquals("headerHeaders::Content-Length are not equals", expectedMultipartMessage.getHeaderHeader().get(MultipartMessageKey.CONTENT_LENGTH.label).toString(), resultMultipartMessage.getHeaderHeader().get(MultipartMessageKey.CONTENT_LENGTH.label).toString());
		assertEquals("payloadHeaders::Content-Disposition are not equals", expectedMultipartMessage.getPayloadHeader().get(MultipartMessageKey.CONTENT_DISPOSITION.label).toString(), resultMultipartMessage.getPayloadHeader().get(MultipartMessageKey.CONTENT_DISPOSITION.label).toString());
		assertEquals("payloadHeaders::Content-Length are not equals", expectedMultipartMessage.getPayloadHeader().get(MultipartMessageKey.CONTENT_LENGTH.label).toString(), resultMultipartMessage.getPayloadHeader().get(MultipartMessageKey.CONTENT_LENGTH.label).toString());
		assertEquals("signatureHeaders::Content-Disposition are not equals", expectedMultipartMessage.getSignatureHeader().get(MultipartMessageKey.CONTENT_DISPOSITION.label).toString(), resultMultipartMessage.getSignatureHeader().get(MultipartMessageKey.CONTENT_DISPOSITION.label).toString());
		assertEquals("signatureHeaders::Content-Length are not equals", expectedMultipartMessage.getSignatureHeader().get(MultipartMessageKey.CONTENT_LENGTH.label).toString(), resultMultipartMessage.getSignatureHeader().get(MultipartMessageKey.CONTENT_LENGTH.label).toString());
	}
	
	// test WITH HttpHeader
	@Test
	public void testParseMultipartMessageWithHttpHeader() {
		
		// when
		String contentType = "Content-Type: form-data; boundary=TESTCQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4;charset=UTF-8";
		
		// do
		MultipartMessage resultMultipartMessage = multipartMessageTransformerService.parseMultipartMessage(multipartMessageString, contentType);
		
		// then
		assertTrue("Content-Type in the httpHeader shuould exists", resultMultipartMessage.getHttpHeaders().get(MultipartMessageKey.CONTENT_TYPE.label).startsWith("Content-Type: form-data; boundary="));
		assertTrue("headerContents, payloadContents and signatureContents are not equals", expectedMultipartMessage.equals(resultMultipartMessage));
		assertEquals("headerHeaders::Content-Disposition are not equals", expectedMultipartMessage.getHeaderHeader().get(MultipartMessageKey.CONTENT_DISPOSITION.label).toString(), resultMultipartMessage.getHeaderHeader().get(MultipartMessageKey.CONTENT_DISPOSITION.label).toString());
		assertEquals("headerHeaders::Content-Length are not equals", expectedMultipartMessage.getHeaderHeader().get(MultipartMessageKey.CONTENT_LENGTH.label).toString(), resultMultipartMessage.getHeaderHeader().get(MultipartMessageKey.CONTENT_LENGTH.label).toString());
		assertEquals("payloadHeaders::Content-Disposition are not equals", expectedMultipartMessage.getPayloadHeader().get(MultipartMessageKey.CONTENT_DISPOSITION.label).toString(), resultMultipartMessage.getPayloadHeader().get(MultipartMessageKey.CONTENT_DISPOSITION.label).toString());
		assertEquals("payloadHeaders::Content-Length are not equals", expectedMultipartMessage.getPayloadHeader().get(MultipartMessageKey.CONTENT_LENGTH.label).toString(), resultMultipartMessage.getPayloadHeader().get(MultipartMessageKey.CONTENT_LENGTH.label).toString());
		assertEquals("signatureHeaders::Content-Disposition are not equals", expectedMultipartMessage.getSignatureHeader().get(MultipartMessageKey.CONTENT_DISPOSITION.label).toString(), resultMultipartMessage.getSignatureHeader().get(MultipartMessageKey.CONTENT_DISPOSITION.label).toString());
		assertEquals("signatureHeaders::Content-Length are not equals", expectedMultipartMessage.getSignatureHeader().get(MultipartMessageKey.CONTENT_LENGTH.label).toString(), resultMultipartMessage.getSignatureHeader().get(MultipartMessageKey.CONTENT_LENGTH.label).toString());
	}
	
	// test WITHOUT HttpHeader
	// TODO: improve assertion for the all parts in the multipart message; should fix the part "assertEquals(..." for the "headerContent" part
	// Problm:order in the JSON headerContent is not same every time....; in the test different boundary is autogenerated each time when parsing multipart message
	// Solution use JSON object and Regex for parsing; headerContentString put in the JSON object which will be compared
	@Test
	public void testMultipartMessagetoStringWithoutHttpHeader() {
		
		// when
		MultipartMessage multipartMessage = new MultipartMessageBuilder()
				.withHttpHeader(expectedHttpHeader)
				.withHeaderHeader(expectedHeaderHeader)
				.withHeaderContent(expectedHeaderContentString)
				.withPayloadHeader(expectedPayloadHeader)
				.withPayloadContent(expectedPayloadContentString)
				.withSignatureHeader(expectedSignatureHeader)
				.withSignatureContent(expectedSignatureContentString)
				.build();
		
		// do
		String resultMultipartMessageString = multipartMessageTransformerService.multipartMessagetoString(multipartMessage, false);
		
		// Devide multipart message on the lines
		Stream<String> lines = resultMultipartMessageString.trim().lines();
		List<String> linesInMultipartMessage = lines.collect(Collectors.toList());
		// Boundary
		String boundary = linesInMultipartMessage.get(0);
		// Delete first and the last line;
		linesInMultipartMessage.remove(0);
		linesInMultipartMessage.remove(linesInMultipartMessage.size()-1);
		
		StringBuilder stringBuilder = new StringBuilder();
		linesInMultipartMessage.forEach(e -> stringBuilder.append(e + System.lineSeparator()));
		
		String[] parts =  stringBuilder.toString().split(boundary);
		String resultHeader = parts[0];
		String resultPayload = parts[1];
		String resultSignature = parts[2];
		
		// then
		String expectedHeader = "Content-Disposition: form-data; name=\"header\"" + System.lineSeparator() +
				"Content-Length: 534" + System.lineSeparator() + 
				"" + System.lineSeparator() + 
				"{" + System.lineSeparator() + 
				"  \"@context\" : \"https://w3id.org/idsa/contexts/2.1.0/context.jsonld\"," + System.lineSeparator() + 
				"  \"@type\" : \"ids:ArtifactResponseMessage\"," + System.lineSeparator() + 
				"  \"correlationMessage\" : \"http://industrialdataspace.org/connectorUnavailableMessage/1a421b8c-3407-44a8-aeb9-253f145c869a\"," + System.lineSeparator() + 
				"  \"transferContract\" : \"https://mdm-connector.ids.isst.fraunhofer.de/examplecontract/bab-bayern-sample/\"," + System.lineSeparator() + 
				"  \"issuerConnector\" : \"http://iais.fraunhofer.de/ids/mdm-connector\"," + System.lineSeparator() + 
				"  \"modelVersion\" : \"1.0.2-SNAPSHOT\"," + System.lineSeparator() + 
				"  \"issued\" : \"2019-05-27T13:09:42.306Z\"," + System.lineSeparator() + 
				"  \"@id\" : \"https://w3id.org/idsa/autogen/artifactResponseMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f\"" + System.lineSeparator() + 
				"}" + System.lineSeparator() + 
				"";
		String expectedPayload = "Content-Disposition: form-data; name=\"payload\"" + System.lineSeparator() +  
				"Content-Length: 50" + System.lineSeparator() +  
				"" + System.lineSeparator() +  
				"{\"catalog.offers.0.resourceEndpoints.path\":\"/pet\"}";
		String expectedSignature = "Content-Disposition: form-data; name=\"signature\"" + System.lineSeparator() + 
				"Content-Length: 49" + System.lineSeparator() + 
				"" + System.lineSeparator() + 
				"{\"signature.resourceEndpoints.path\":\"/signature\"}";
		// TODO: should fix the part "assertEquals(..." for the "headerContent" part
		// Problem: order in the JSON headerContent is not same every time....
//		assertEquals("Header is not as expected", expectedHeader.trim(), resultHeader.trim());
		assertEquals("Numbers parts in the multipart message are 3", 3, parts.length);
		assertEquals("Payload is not as expected", expectedPayload.trim(), resultPayload.trim());
		assertEquals("Singnture is not as expected", expectedSignature.trim(), resultSignature.trim());
	}
	
	// test WITH HttpHeader
	// TODO: improve assertion for the all parts in the multipart message
	// Problm: in the test different boundary is autogenerated each time when parsing multipart message
	// Solution use JSON object and Regex for parsing; headerContentString put in the JSON object which will be compared
	@Test
	public void testMultipartMessagetoStringWithHttpHeader() {
		
		// when
		MultipartMessage multipartMessage = new MultipartMessageBuilder()
				.withHttpHeader(expectedHttpHeader)
				.withHeaderHeader(expectedHeaderHeader)
				.withHeaderContent(expectedHeaderContentString)
				.withPayloadHeader(expectedPayloadHeader)
				.withPayloadContent(expectedPayloadContentString)
				.withSignatureHeader(expectedSignatureHeader)
				.withSignatureContent(expectedSignatureContentString)
				.build();
				
		// do
		String resultMultipartMessageString = multipartMessageTransformerService.multipartMessagetoString(multipartMessage);
		
		// Devide multipart message on the lines
		Stream<String> lines = resultMultipartMessageString.trim().lines();
		List<String> linesInMultipartMessage = lines.collect(Collectors.toList());
		// Boundary
		String boundary = linesInMultipartMessage.get(3);
		
		// get Contet-Type
		String contentType = linesInMultipartMessage.get(1);
		
		// Delete HttpHeaders, first boundary and the last boundary
		linesInMultipartMessage.remove(0);
		linesInMultipartMessage.remove(0);
		linesInMultipartMessage.remove(0);
		linesInMultipartMessage.remove(0);
		linesInMultipartMessage.remove(linesInMultipartMessage.size()-1);
		
		StringBuilder stringBuilder = new StringBuilder();
		linesInMultipartMessage.forEach(e -> stringBuilder.append(e + System.lineSeparator()));
		
		String[] parts =  stringBuilder.toString().split(boundary);
		String resultHeader = parts[0];
		String resultPayload = parts[1];
		String resultSignature = parts[2];
		
		// then
		String expectedHeader = "Content-Disposition: form-data; name=\"header\"" + System.lineSeparator() +
				"Content-Length: 534" + System.lineSeparator() + 
				"" + System.lineSeparator() + 
				"{" + System.lineSeparator() + 
				"  \"@context\" : \"https://w3id.org/idsa/contexts/2.1.0/context.jsonld\"," + System.lineSeparator() + 
				"  \"@type\" : \"ids:ArtifactResponseMessage\"," + System.lineSeparator() + 
				"  \"correlationMessage\" : \"http://industrialdataspace.org/connectorUnavailableMessage/1a421b8c-3407-44a8-aeb9-253f145c869a\"," + System.lineSeparator() + 
				"  \"transferContract\" : \"https://mdm-connector.ids.isst.fraunhofer.de/examplecontract/bab-bayern-sample/\"," + System.lineSeparator() + 
				"  \"issuerConnector\" : \"http://iais.fraunhofer.de/ids/mdm-connector\"," + System.lineSeparator() + 
				"  \"modelVersion\" : \"1.0.2-SNAPSHOT\"," + System.lineSeparator() + 
				"  \"issued\" : \"2019-05-27T13:09:42.306Z\"," + System.lineSeparator() + 
				"  \"@id\" : \"https://w3id.org/idsa/autogen/artifactResponseMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f\"" + System.lineSeparator() + 
				"}" + System.lineSeparator() + 
				"";
		String expectedPayload = "Content-Disposition: form-data; name=\"payload\"" + System.lineSeparator() +  
				"Content-Length: 50" + System.lineSeparator() +  
				"" + System.lineSeparator() +  
				"{\"catalog.offers.0.resourceEndpoints.path\":\"/pet\"}";
		String expectedSignature = "Content-Disposition: form-data; name=\"signature\"" + System.lineSeparator() + 
				"Content-Length: 49" + System.lineSeparator() + 
				"" + System.lineSeparator() + 
				"{\"signature.resourceEndpoints.path\":\"/signature\"}";
		// TODO: should fix the part "assertEquals(..." for the "headerContent" part
		// Problem: order in the JSON headerContent is not same every time....
//		assertEquals("Header is not as expected", expectedHeader.trim(), resultHeader.trim());
		assertTrue("", contentType.startsWith("Content-Type: multipart/mixed; boundary="));
		assertEquals("Numbers parts in the multipart message are 3", 3, parts.length);
		assertEquals("Payload is not as expected", expectedPayload.trim(), resultPayload.trim());
		assertEquals("Singnture is not as expected", expectedSignature.trim(), resultSignature.trim());
		
	}
	
	
}
