package it.eng.idsa.businesslogic.processor.sender;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.MessagePart;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.businesslogic.util.RouterType;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.MultipartMessageKey;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class SenderParseReceivedResponseMessage implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(SenderParseReceivedResponseMessage.class);

	@Autowired
	private RejectionMessageService rejectionMessageService;

	@Autowired
	private HttpHeaderService headerService;

	@Autowired
	private MultipartMessageService multipartMessageService;

	@Value("${application.eccHttpSendRouter}")
	private String eccHttpSendRouter;

	@Value("${application.isEnabledDapsInteraction}")
	private boolean isEnabledDapsInteraction;
	
	@Override
	public void process(Exchange exchange) throws Exception {

		String header = null;
		Map<String, Object> headersParts = exchange.getMessage().getHeaders();
		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);
		Message message = null;
		MultipartMessage multipartMessageWithToken = null;
		String token = null;

		if (RouterType.HTTP_HEADER.equals(eccHttpSendRouter)) {
			message = headerService.headersToMessage(headersParts);
			if (message == null) {
				logger.error("Can't generate a IDS-Message from the received headers.");
				rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON);
				return;
			}
			headersParts.put("Payload-Content-Type", headersParts.get(MultipartMessageKey.CONTENT_TYPE.label));

//			if (headersParts.get("IDS-SecurityToken-TokenValue") != null) {
//				token = headersParts.get("IDS-SecurityToken-TokenValue").toString();
//			}
			token = message.getSecurityToken() != null ? message.getSecurityToken().getTokenValue() : null;
			Map<String, String> headerHeaderContentType = new HashMap<>();
			headerHeaderContentType.put("Content-Type", "application/ld+json");
			Map<String, String> payloadHeaderContentType = new HashMap<>();
			payloadHeaderContentType.put("Content-Type", (String) headersParts.get(MultipartMessageKey.CONTENT_TYPE.label));
			multipartMessageWithToken = new MultipartMessageBuilder()
					.withHttpHeader(headerService.convertMapToStringString(headersParts))
					.withHeaderContent(message)
					.withHeaderHeader(headerHeaderContentType)
					.withPayloadContent(exchange.getMessage().getBody(String.class))
					.withPayloadHeader(payloadHeaderContentType)
					.withToken(token)
					.build();

		} else {
			if (!headersParts.containsKey(MessagePart.HEADER)) {
				logger.error("Multipart message header is missing");
				rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON);
			}

			if (headersParts.get(MessagePart.HEADER) == null) {
				logger.error("Multipart message header is null");
				rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON);
			}

			try {
				if (headersParts.get(MessagePart.HEADER) instanceof String) {
					header = headersParts.get(MessagePart.HEADER).toString();
				} else {
					DataHandler dtHeader = (DataHandler) headersParts.get(MessagePart.HEADER);
					header = IOUtils.toString(dtHeader.getInputStream(), StandardCharsets.UTF_8);
				}
				message = MultipartMessageProcessor.getMessage(header);
				if(headersParts.get(MessagePart.PAYLOAD) != null) {
				}
				
				if (isEnabledDapsInteraction) {
					token = multipartMessageService.getToken(message);
				}
				multipartMessageWithToken = new MultipartMessageBuilder()
						.withHttpHeader(multipartMessage.getHttpHeaders())
						.withHeaderContent(multipartMessage.getHeaderContent())
						.withHeaderHeader(multipartMessage.getHeaderHeader())
						.withPayloadContent(multipartMessage.getPayloadContent())
						.withPayloadHeader(multipartMessage.getPayloadHeader())
						.withToken(token)
						.build();
				
			} catch (Exception e) {
				logger.error("Error parsing multipart message:", e);
				rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON);
			}
		}
		exchange.getMessage().setHeaders(headersParts);
		exchange.getMessage().setBody(multipartMessageWithToken);
	}
}
