package it.eng.idsa.businesslogic.util;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.UtilMessageService;

public class MultipartMessageUtil {
	
	public static MultipartMessage getMultipartMessage() {
		return new MultipartMessageBuilder()
				.withHeaderContent(UtilMessageService.getArtifactResponseMessage())
				.withPayloadContent("PAYLOAD")
				.build();
	}
	
	public static MultipartMessage getMultipartMessage(Message message) {
		return new MultipartMessageBuilder()
				.withHeaderContent(message)
				.withPayloadContent("PAYLOAD")
				.build();
	}
	
	public static MultipartMessage getMultipartMessageToken(String token) {
		return new MultipartMessageBuilder()
				.withHeaderContent(UtilMessageService.getArtifactResponseMessage())
				.withPayloadContent("PAYLOAD")
				.withToken(token)
				.build();
	}
	
	public static MultipartMessage getMultipartMessage(Message message, String payload) {
		return new MultipartMessageBuilder()
				.withHeaderContent(message)
				.withPayloadContent(payload)
				.build();
	}
	
	public static MultipartMessage getMultipartMessage(Message message, String payload, String token) {
		return new MultipartMessageBuilder()
				.withHeaderContent(message)
				.withPayloadContent(payload)
				.withToken(token)
				.build();
	}
	
	public static String getMultipartMessageAsString() {
		return MultipartMessageProcessor.multipartMessagetoString(getMultipartMessage());
	}
}
