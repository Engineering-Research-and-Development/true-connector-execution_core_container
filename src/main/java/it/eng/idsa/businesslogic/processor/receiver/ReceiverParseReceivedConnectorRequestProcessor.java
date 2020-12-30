package it.eng.idsa.businesslogic.processor.receiver;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.MultipartMessageKey;

@Component
public class ReceiverParseReceivedConnectorRequestProcessor implements Processor {
	
	private static final Logger logger = LogManager.getLogger(ReceiverParseReceivedConnectorRequestProcessor.class);

	@Value("${application.isEnabledDapsInteraction}")
	private boolean isEnabledDapsInteraction;

	@Value("${application.dataApp.websocket.isEnabled}")
	private boolean isEnabledDataAppWebSocket;

	@Value("${application.eccHttpSendRouter}")
	private String eccHttpSendRouter;

	@Autowired
	private MultipartMessageService multipartMessageService;

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
		String token = null;

		headersParts.put("Is-Enabled-DataApp-WebSocket", isEnabledDataAppWebSocket);
		
		if (eccHttpSendRouter.equals("http-header")) { 
			// create Message object from IDS-* headers, needs for UsageControl flow
			header = headerService.getHeaderMessagePartFromHttpHeadersWithoutToken(headersParts);
			message = multipartMessageService.getMessage(header);
			
			if (headersParts.get("IDS-SecurityToken-TokenValue") != null) {
				token = headersParts.get("IDS-SecurityToken-TokenValue").toString();
			}
			payload = exchange.getMessage().getBody(String.class);
			 
			multipartMessage = new MultipartMessageBuilder()
					.withHeaderContent(header)
					.withPayloadContent(payload)
					.withToken(token).build();
			headersParts.put("Payload-Content-Type", headersParts.get(MultipartMessageKey.CONTENT_TYPE.label));
		} else {
			if (!headersParts.containsKey("header")) {
				logger.error("Multipart message header is missing");
				rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON, message);
			}

			if (headersParts.get("header") == null) {
				logger.error("Multipart message header is null");
				rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON, message);
			}

			try {
				// Save the original message header for Usage Control Enforcement
				if (headersParts.containsKey("Original-Message-Header")) {
					headersParts.put("Original-Message-Header", headersParts.get("Original-Message-Header").toString());
				}
				if (headersParts.get("header") instanceof String) {
					header = headersParts.get("header").toString();
				} else {
					DataHandler dtHeader = (DataHandler) headersParts.get("header");
					header = IOUtils.toString(dtHeader.getInputStream(), StandardCharsets.UTF_8);
				}
				
				message = multipartMessageService.getMessage(header);
				if(headersParts.get("payload")!=null) {
					payload = headersParts.get("payload").toString();
				}
				
				if (isEnabledDapsInteraction) {
					token = multipartMessageService.getToken(message);
				}
				multipartMessage = new MultipartMessageBuilder().withHeaderContent(header).withPayloadContent(payload).withToken(token)
						.build();

				headersParts.put("Payload-Content-Type",
						headersParts.get("payload.org.eclipse.jetty.servlet.contentType"));
			} catch (Exception e) {
				logger.error("Error parsing multipart message:", e);
				rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON, message);
			}
		}
		exchange.getMessage().setHeaders(headersParts);
		exchange.getMessage().setBody(multipartMessage);
	}
}
