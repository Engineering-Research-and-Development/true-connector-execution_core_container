package it.eng.idsa.businesslogic.processor.sender;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.audit.CamelAuditable;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.MultipartMessageKey;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class SenderParseReceivedDataProcessorBodyBinary implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(SenderParseReceivedDataProcessorBodyBinary.class);

	@Autowired
	private RejectionMessageService rejectionMessageService;

	@Override
	@CamelAuditable(successEventType = TrueConnectorEventType.CONNECTOR_REQUEST, 
		failureEventType = TrueConnectorEventType.EXCEPTION_BAD_REQUEST)
	public void process(Exchange exchange) throws Exception {
		logger.info("Received multipart/mixed request");
		Map<String, Object> headerParts = new HashMap<String, Object>();
		String receivedDataBodyBinary = null;

		// Get from the input "exchange"
		headerParts = exchange.getMessage().getHeaders();
		receivedDataBodyBinary = exchange.getMessage().getBody(String.class);
		if (receivedDataBodyBinary == null) {
			logger.error("Body of the received multipart message is null");
			rejectionMessageService.sendRejectionMessage(null, RejectionReason.MALFORMED_MESSAGE);
		}
		logger.debug(receivedDataBodyBinary);
		try {
			MultipartMessage multipartMessage = MultipartMessageProcessor.parseMultipartMessage(receivedDataBodyBinary);
			// Create headers parts
			headerParts.put("Payload-Content-Type",
					multipartMessage.getPayloadHeader().get(MultipartMessageKey.CONTENT_TYPE.label));
			
			logger.debug("Header part {}", multipartMessage.getHeaderContentString());
			logger.debug("Payload part {}", multipartMessage.getPayloadContent());

			// Return exchange
			exchange.getMessage().setBody(multipartMessage);
			exchange.getMessage().setHeaders(headerParts);

		} catch (Exception e) {
			logger.error("Error parsing multipart message:", e);
			rejectionMessageService.sendRejectionMessage(null, RejectionReason.MALFORMED_MESSAGE);
		}
	}
}
