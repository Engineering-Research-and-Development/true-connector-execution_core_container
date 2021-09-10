package it.eng.idsa.businesslogic.processor.sender;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.impl.ProtocolValidationService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
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
	private ProtocolValidationService protocolValidationService;

	@Autowired
	private RejectionMessageService rejectionMessageService;

	@Override
	public void process(Exchange exchange) throws Exception {

		Message message = null;
		Map<String, Object> headerParts = new HashMap<String, Object>();
		String receivedDataBodyBinary = null;

		// Get from the input "exchange"
		headerParts = exchange.getMessage().getHeaders();
		receivedDataBodyBinary = exchange.getMessage().getBody(String.class);
		
		if (receivedDataBodyBinary == null) {
			logger.error("Body of the received multipart message is null");
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES, null);
		}

		try {
			MultipartMessage multipartMessage = MultipartMessageProcessor.parseMultipartMessage(receivedDataBodyBinary);
			message = multipartMessage.getHeaderContent();
			// Create headers parts
			headerParts.put("Payload-Content-Type",
					multipartMessage.getPayloadHeader().get(MultipartMessageKey.CONTENT_TYPE.label));
			
			String forwardTo = (String) headerParts.get("Forward-To");
			forwardTo = protocolValidationService.validateProtocol(forwardTo, message);
			headerParts.replace("Forward-To", forwardTo);

			// Return exchange
			exchange.getMessage().setBody(multipartMessage);
			exchange.getMessage().setHeaders(headerParts);

		} catch (Exception e) {
			logger.error("Error parsing multipart message:", e);
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES, message);
		}
	}
}
