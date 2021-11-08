package it.eng.idsa.businesslogic.processor.receiver;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.MessagePart;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.businesslogic.util.RouterType;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.MultipartMessageKey;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ReceiverMultiPartMessageProcessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(ReceiverMultiPartMessageProcessor.class);

	@Value("${application.dataApp.websocket.isEnabled}")
	private boolean isEnabledDataAppWebSocket;

	@Value("${application.eccHttpSendRouter}")
	private String eccHttpSendRouter;
	
	@Value("${application.openDataAppReceiverRouter}")
	private String dataAppSendRouter;

	@Autowired
	private HttpHeaderService headerService;

	@Autowired
	private RejectionMessageService rejectionMessageService;

	@Override
	public void process(Exchange exchange) throws Exception {

		String header = null;
		String payload = null;
		Map<String, Object> headersParts = exchange.getMessage().getHeaders();
		Message message = null;
		MultipartMessage multipartMessage = null;

		headersParts.put("Is-Enabled-DataApp-WebSocket", isEnabledDataAppWebSocket);
		
		if (RouterType.HTTP_HEADER.equals(dataAppSendRouter)) { 
			headersParts.put("Payload-Content-Type", headersParts.get(MultipartMessageKey.CONTENT_TYPE.label));
			if (exchange.getMessage().getBody() != null) {
				payload = exchange.getMessage().getBody(String.class);
			} else {
				logger.error("Payload is null");
				rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON, message);
			}
			// create Message object from IDS-* headers, needs for UsageControl flow
//			header = headerService.headersToMessage(headersParts);
			message = headerService.headersToMessage(headersParts);
			multipartMessage = new MultipartMessageBuilder()
					.withHeaderContent(message)
					.withPayloadContent(payload)
					.build();

		} else {

			if (!headersParts.containsKey(MessagePart.HEADER)) {
				logger.error("Multipart message header is missing");
				rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON, message);
			}

			if (headersParts.get(MessagePart.HEADER) == null) {
				logger.error("Multipart message header is null");
				rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON, message);
			}

			try {
				if (headersParts.get(MessagePart.HEADER) instanceof String) {
					header = headersParts.get(MessagePart.HEADER).toString();
				} else {
					DataHandler dtHeader = (DataHandler) headersParts.get(MessagePart.HEADER);
					header = IOUtils.toString(dtHeader.getInputStream(), StandardCharsets.UTF_8);
				}
				if(headersParts.get(MessagePart.PAYLOAD) != null) {
					payload = headersParts.get(MessagePart.PAYLOAD).toString();
				}

				multipartMessage = new MultipartMessageBuilder().withHeaderContent(header).withPayloadContent(payload)
						.build();
				if(headersParts.containsKey("Payload-Content-Type")) {
					headersParts.put("Payload-Content-Type",
							headersParts.get("payload.org.eclipse.jetty.servlet.contentType"));
				} else {
					// Payload content type not present, setting default plain/text
					headersParts.put("Payload-Content-Type", ContentType.TEXT_PLAIN.toString());
				}
			} catch (Exception e) {
				logger.error("Error parsing multipart message:", e);
				rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON, message);
			}
		}

		exchange.getMessage().setHeaders(headersParts);
		exchange.getMessage().setBody(multipartMessage);
	}
}