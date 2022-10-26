package it.eng.idsa.businesslogic.processor.sender;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.activation.DataHandler;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.attachment.Attachment;
import org.apache.camel.attachment.AttachmentMessage;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.audit.CamelAuditable;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.MessagePart;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class SenderParseReceivedDataProcessorBodyFormData implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(SenderParseReceivedDataProcessorBodyFormData.class);

	@Autowired
	private RejectionMessageService rejectionMessageService;

	@Override
	@CamelAuditable(successEventType = TrueConnectorEventType.CONNECTOR_REQUEST, 
		failureEventType = TrueConnectorEventType.EXCEPTION_BAD_REQUEST)
	public void process(Exchange exchange) throws Exception {

		String header = null;
		String payload = null;
		Map<String, String> payloadHeaders = new HashMap<>();

		// Get from the input "exchange"
		Map<String, Object> receivedDataHeader = exchange.getMessage().getHeaders();
		logger.info("Received multipart/form request");
		try {
			// Create multipart message parts
			if (receivedDataHeader.containsKey(MessagePart.HEADER)) {
				if(receivedDataHeader.get(MessagePart.HEADER) instanceof DataHandler) {
					DataHandler dtHeader = (DataHandler) receivedDataHeader.get(MessagePart.HEADER);
					header = IOUtils.toString(dtHeader.getInputStream(), StandardCharsets.UTF_8);
				} else {
					header = (String) receivedDataHeader.get(MessagePart.HEADER);
				}
			} 
			logger.debug("Header part {}", header);
			if (receivedDataHeader.containsKey(MessagePart.PAYLOAD)) {
				if(receivedDataHeader.get(MessagePart.PAYLOAD) instanceof String) {
					payload = (String) receivedDataHeader.get(MessagePart.PAYLOAD);
				}
			} else if (exchange.getMessage(AttachmentMessage.class) != null 
					&& exchange.getMessage(AttachmentMessage.class).getAttachmentObject(MessagePart.PAYLOAD) != null) {
				Attachment att1 = exchange.getMessage(AttachmentMessage.class).getAttachmentObject(MessagePart.PAYLOAD);
				DataHandler dh1 = att1.getDataHandler();
				payload = Base64.getEncoder().encodeToString(IOUtils.toByteArray(dh1.getInputStream()));
				payloadHeaders = getPayloadHeadersFromAttachment(att1);
			}
				
			logger.debug("Payload part {}", payload);

			MultipartMessage multipartMessage = new MultipartMessageBuilder()
					.withHeaderContent(header)
					.withPayloadHeader(payloadHeaders)
					.withPayloadContent(payload)
					.build();

			// Return exchange
			exchange.getMessage().setHeaders(receivedDataHeader);
			exchange.getMessage().setBody(multipartMessage);

		} catch (Exception e) {
			logger.error("Error parsing multipart message:", e);
			rejectionMessageService.sendRejectionMessage(null, RejectionReason.MALFORMED_MESSAGE);
		}
	}

	private Map<String, String> getPayloadHeadersFromAttachment(Attachment att1) {
		return att1.getHeaderNames().stream().collect(Collectors.toMap(i -> i, i -> att1.getHeader(i)));
	}
}
