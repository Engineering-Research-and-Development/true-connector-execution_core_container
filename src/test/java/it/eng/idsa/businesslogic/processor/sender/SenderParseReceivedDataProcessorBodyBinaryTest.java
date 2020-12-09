package it.eng.idsa.businesslogic.processor.sender;

import static org.junit.Assert.assertNotEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;

import it.eng.idsa.businesslogic.util.TestUtilMessageService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

public class SenderParseReceivedDataProcessorBodyBinaryTest {

	private String message;

	MultipartMessage multipartMessage = new MultipartMessageBuilder()
			.withHeaderContent(TestUtilMessageService.getArtifactRequestMessage())
			.withPayloadContent("foo bar")
			.build();

	@Test
	public void processWithoutContentType() {
		System.out.println("To string");
		message = MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false);
		System.out.println(message);
	}

	@Test
	public void processWithContentTypeApplicationJson() {
		System.out.println("To string - include headerHeaders");
		message = MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false, false);
		System.out.println(message);
	}

	@Test
	public void processWithContentTypeJsonLd() {
		System.out.println("To string - full new LD");
		message = MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false, true);
		System.out.println(message);
	}

	@Test
	public void processWithInvalidContentType() {
		Map<String, String> headerHeader = new HashMap<String, String>();
		headerHeader.put("Content-Type", ContentType.APPLICATION_XML.toString());

		MultipartMessage messageWithInvalidContentType = new MultipartMessageBuilder()
				.withHeaderContent(TestUtilMessageService.getArtifactRequestMessage())
				.withHeaderHeader(headerHeader)
				.withPayloadContent("foo bar").build();

		assertNotEquals(MultipartMessageProcessor.multipartMessagetoString(messageWithInvalidContentType, false, false), MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false, false));
	}

}
