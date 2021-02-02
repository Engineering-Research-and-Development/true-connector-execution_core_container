package it.eng.idsa.businesslogic.processor.sender;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class SenderParseReceivedDataProcessorBodyFormData implements Processor {

	private static final Logger logger = LogManager.getLogger(SenderParseReceivedDataProcessorBodyFormData.class);

	@Autowired
	private MultipartMessageService multipartMessageService;

	@Autowired
	private RejectionMessageService rejectionMessageService;

	@Override
	public void process(Exchange exchange) throws Exception {

		String contentType;
		String forwardTo;
		String header = null;
		String payload = null;
		Message message = null;

		// Get from the input "exchange"
		Map<String, Object> receivedDataHeader = exchange.getMessage().getHeaders();

		try {
			// Create headers parts
			contentType = receivedDataHeader.get("Content-Type").toString();
			receivedDataHeader.put("Content-Type", contentType);
			forwardTo = receivedDataHeader.get("Forward-To").toString();
			receivedDataHeader.put("Forward-To", forwardTo);

			// Create multipart message parts
			if (receivedDataHeader.containsKey("header")) {
				if(receivedDataHeader.get("header") instanceof DataHandler) {
					DataHandler dtHeader = (DataHandler) receivedDataHeader.get("header");
					header = IOUtils.toString(dtHeader.getInputStream(), StandardCharsets.UTF_8);
				} else {
					header = (String) receivedDataHeader.get("header");
				}
			} 
			message = multipartMessageService.getMessage(header);
			if (receivedDataHeader.containsKey("payload")) {
				if(receivedDataHeader.get("payload") instanceof DataHandler) {
					DataHandler dtPayload = (DataHandler) receivedDataHeader.get("payload");
					payload = IOUtils.toString(dtPayload.getInputStream(), StandardCharsets.UTF_8);
				} else {
					payload = (String) receivedDataHeader.get("payload");
				}
			}

			MultipartMessage multipartMessage = new MultipartMessageBuilder()
					.withHeaderContent(header)
					.withPayloadContent(payload)
					.build();

			// Return exchange
			exchange.getMessage().setHeaders(receivedDataHeader);
			exchange.getMessage().setBody(multipartMessage);

		} catch (Exception e) {
			logger.error("Error parsing multipart message:", e);
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES, message);
		}
	}
}
