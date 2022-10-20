package it.eng.idsa.businesslogic.processor.receiver;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.activation.DataHandler;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.attachment.Attachment;
import org.apache.camel.attachment.AttachmentMessage;
import org.apache.camel.support.MessageHelper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.audit.CamelAuditable;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;
import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.MessagePart;
import it.eng.idsa.businesslogic.util.RouterType;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.exception.MultipartMessageException;
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
	
	@Value("#{new Boolean('${application.isEnabledUsageControl}')}")
    private boolean isEnabledUsageControl;

	@Autowired
	private MultipartMessageService multipartMessageService;

	@Autowired
	private HttpHeaderService headerService;

	@Autowired
	private RejectionMessageService rejectionMessageService;
	
	@Override
	@CamelAuditable(successEventType = TrueConnectorEventType.CONNECTOR_REQUEST, 
	failureEventType = TrueConnectorEventType.EXCEPTION_BAD_REQUEST)
	public void process(Exchange exchange) throws Exception {
		String header = null;
		String payload = null;
		Map<String, Object> headersParts = exchange.getMessage().getHeaders();
		Message message = null;
		MultipartMessage multipartMessage = null;
		String token = null;

		headersParts.put("Is-Enabled-DataApp-WebSocket", isEnabledDataAppWebSocket);
		
		if (RouterType.HTTP_HEADER.equals(eccHttpSendRouter)) { 
			// create Message object from IDS-* headers, needs for UsageControl flow
			headersParts.put("Payload-Content-Type", headersParts.get(MultipartMessageKey.CONTENT_TYPE.label));
			try {
				message = headerService.headersToMessage(headersParts);
			} catch (Exception e) {
				logger.error("Message could not be created - check if all required headers are present.");
				rejectionMessageService.sendRejectionMessage(null, RejectionReason.MALFORMED_MESSAGE);
			}
			// TODO check if we need catch and null check
			if(message == null) {
				logger.error("Message could not be created - check if all required headers are present.");
				rejectionMessageService.sendRejectionMessage(null, RejectionReason.MALFORMED_MESSAGE);
			}

			token = message.getSecurityToken() != null ? message.getSecurityToken().getTokenValue() : null;
			payload = exchange.getMessage().getBody(String.class);
			 
			multipartMessage = new MultipartMessageBuilder()
					.withHeaderContent(message)
					.withPayloadContent(payload)
					.withToken(token)
					.build();
			
		} 
		else if(RouterType.MULTIPART_MIX.equals(eccHttpSendRouter)) {
			String receivedDataBodyBinary = null;
			try {
				receivedDataBodyBinary = MessageHelper.extractBodyAsString(exchange.getMessage());
			} catch (NullPointerException npe) {
				logger.error("Received body is empty.");
				rejectionMessageService.sendRejectionMessage(null, RejectionReason.MALFORMED_MESSAGE);
			}
			try {
				multipartMessage = MultipartMessageProcessor.parseMultipartMessage(receivedDataBodyBinary);
			}
			catch (MultipartMessageException e) {
				logger.error("Error parsing multipart message:", e);
				rejectionMessageService.sendRejectionMessage(null, RejectionReason.MALFORMED_MESSAGE);
			}			
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
			if (!headersParts.containsKey(MessagePart.HEADER)) {
				logger.error("Multipart message header is missing");
				rejectionMessageService.sendRejectionMessage(null, RejectionReason.MALFORMED_MESSAGE);
			}

			if (headersParts.get(MessagePart.HEADER) == null) {
				logger.error("Multipart message header is null");
				rejectionMessageService.sendRejectionMessage(null, RejectionReason.MALFORMED_MESSAGE);
			}

			Map<String, String> payloadHeaders = new HashMap<>();

			try {
				if (headersParts.get(MessagePart.HEADER) instanceof String) {
					header = (String) headersParts.get(MessagePart.HEADER);
				} else {
					DataHandler dtHeader = (DataHandler) headersParts.get(MessagePart.HEADER);
					header = IOUtils.toString(dtHeader.getInputStream(), StandardCharsets.UTF_8);
				}
				//TODO Consider to refactor multipartMessageService.getMessage(header) to throw exception instead returnin null
				// so that we have consistent logic 
				message = MultipartMessageProcessor.getMessage(header);
				if(message == null) {
					throw new MultipartMessageException("Could not create message from request");				
					}
				if (headersParts.containsKey(MessagePart.PAYLOAD)) {
					if(headersParts.get(MessagePart.PAYLOAD) instanceof String) {
						payload = (String) headersParts.get(MessagePart.PAYLOAD);
					}
				} else if (exchange.getMessage(AttachmentMessage.class) != null && 
						exchange.getMessage(AttachmentMessage.class).getAttachmentObject(MessagePart.PAYLOAD) != null) {
					Attachment att1 = exchange.getMessage(AttachmentMessage.class).getAttachmentObject(MessagePart.PAYLOAD);
					DataHandler dh1 = att1.getDataHandler();
					payload = IOUtils.toString(dh1.getInputStream(), StandardCharsets.UTF_8);
					payloadHeaders = getPayloadHeadersFromAttachment(att1);
				} 
				
				if (isEnabledDapsInteraction) {
					token = multipartMessageService.getToken(message);
				}
				multipartMessage = new MultipartMessageBuilder()
						.withHeaderContent(header)
						.withPayloadContent(payload)
						.withPayloadHeader(payloadHeaders)
						.withToken(token)
						.build();

				headersParts.put("Payload-Content-Type", headersParts.get("payload.org.eclipse.jetty.servlet.contentType"));
			} catch (Exception e) {
				logger.error("Error parsing multipart message:", e);
				rejectionMessageService.sendRejectionMessage(null, RejectionReason.MALFORMED_MESSAGE);
			}
		}
		
		exchange.getMessage().setHeaders(headersParts);
		exchange.getMessage().setBody(multipartMessage);
		
	}
	
	private Map<String, String> getPayloadHeadersFromAttachment(Attachment att1) {
		return att1.getHeaderNames().stream().collect(Collectors.toMap(i -> i, i -> att1.getHeader(i)));
	}
}
