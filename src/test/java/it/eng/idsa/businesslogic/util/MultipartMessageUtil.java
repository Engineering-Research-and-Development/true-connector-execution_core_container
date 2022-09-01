package it.eng.idsa.businesslogic.util;

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
	
	public static String getMultipartMessageAsString() {
		return MultipartMessageProcessor.multipartMessagetoString(getMultipartMessage());
	}
}
