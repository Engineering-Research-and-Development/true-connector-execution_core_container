package it.eng.idsa.businesslogic.processor.receiver;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.support.MessageHelper;
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

@Component
public class ReceiverParseReceivedConnectorRequestProcessor implements Processor {
	
	private static final Logger logger = LoggerFactory.getLogger(ReceiverParseReceivedConnectorRequestProcessor.class);

	@Value("${application.isEnabledDapsInteraction}")
	private boolean isEnabledDapsInteraction;

	@Value("${application.dataApp.websocket.isEnabled}")
	private boolean isEnabledDataAppWebSocket;

	@Value("${application.websocket.isEnabled}")
	private boolean isEnabledWebSocket;
	
	@Value("${application.eccHttpSendRouter}")
	private String eccHttpSendRouter;
	
	@Value("${application.isEnabledUsageControl:false}")
    private boolean isEnabledUsageControl;

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
		
		logger.info("Received response on B-endpoint");
		
		if (RouterType.HTTP_HEADER.equals(eccHttpSendRouter)) { 
			logger.debug("Http-header request");
			// create Message object from IDS-* headers, needs for UsageControl flow
			headersParts.put("Payload-Content-Type", headersParts.get(MultipartMessageKey.CONTENT_TYPE.label));
			header = headerService.getHeaderMessagePartFromHttpHeaders(headersParts);
			message = multipartMessageService.getMessage(header);
			if(message == null) {
				logger.error("Message could not be created - check if all required headers are present.");
				rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES, null);
			}
			
			if (null != headersParts.get("IDS-SecurityToken-TokenValue")) {
				token = headersParts.get("IDS-SecurityToken-TokenValue").toString();
			}
			payload = exchange.getMessage().getBody(String.class);
			 
			multipartMessage = new MultipartMessageBuilder()
					.withHeaderContent(header)
					.withPayloadContent(payload)
					.withToken(token)
					.build();
			
		} 
		else if(RouterType.MULTIPART_MIX.equals(eccHttpSendRouter)) {
			logger.debug("Multipart/mixed request");
			String receivedDataBodyBinary = MessageHelper.extractBodyAsString(exchange.getMessage());
			multipartMessage = MultipartMessageProcessor.parseMultipartMessage(receivedDataBodyBinary);
			if (isEnabledDapsInteraction) {
				token = multipartMessageService.getToken(multipartMessage.getHeaderContent());
			}
			multipartMessage = new MultipartMessageBuilder()
					.withHttpHeader(multipartMessage.getHttpHeaders())
					.withHeaderHeader(multipartMessage.getHeaderHeader())
					.withHeaderContent(multipartMessage.getHeaderContent())
					.withPayloadHeader(multipartMessage.getPayloadHeader())
					.withPayloadContent(multipartMessage.getPayloadContent())
					.withToken(token)
					.build();
		}
		else {
			logger.debug("Multipart/form request");
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
				
				message = multipartMessageService.getMessage(header);
				if(headersParts.get(MessagePart.PAYLOAD) != null) {
					payload = headersParts.get(MessagePart.PAYLOAD).toString();
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
		logger.debug("Incomming message {}", multipartMessage.getHeaderContentString());
		logger.debug("Payload {}", multipartMessage.getPayloadContent());
		
		exchange.getMessage().setHeaders(headersParts);
		exchange.getMessage().setBody(multipartMessage);
	}
}
