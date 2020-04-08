package it.eng.idsa.businesslogic.processor.producer;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.impl.MultiPartMessageServiceImpl;
import it.eng.idsa.businesslogic.service.impl.RejectionMessageServiceImpl;
import it.eng.idsa.businesslogic.util.RejectionMessageType;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ProducerParseReceivedDataProcessorBodyBinary implements Processor {

	private static final Logger logger = LogManager.getLogger(ProducerParseReceivedDataProcessorBodyBinary.class);
	
	@Value("${application.isEnabledDapsInteraction}")
	private boolean isEnabledDapsInteraction;

	@Autowired
	private MultiPartMessageServiceImpl multiPartMessageServiceImpl;
	
	@Autowired
	private RejectionMessageServiceImpl rejectionMessageServiceImpl;

	@Override
	public void process(Exchange exchange) throws Exception {

		String contentType;
		String forwardTo;
		String header = null;
		String payload = null;
		Message message = null;
		Map<String, Object> headesParts = new HashMap<String, Object>();
		Map<String, Object> multipartMessageParts = new HashMap<String, Object>();
		String receivedDataBodyBinary = null;

		// Get from the input "exchange"
		Map<String, Object> receivedDataHeader = exchange.getIn().getHeaders();
		receivedDataBodyBinary = exchange.getIn().getBody(String.class);
		if (receivedDataBodyBinary == null) {			
			logger.error("Body of the received multipart message is null");
			rejectionMessageServiceImpl.sendRejectionMessage(
					RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES, 
					message);
		}
		try {
			// Create headers parts
			// Put in the header value of the application.property: application.isEnabledDapsInteraction
			headesParts.put("Is-Enabled-Daps-Interaction", isEnabledDapsInteraction);
			contentType = receivedDataHeader.get("Content-Type").toString();
			headesParts.put("Content-Type", contentType);
			forwardTo = receivedDataHeader.get("Forward-To").toString();
			headesParts.put("Forward-To", forwardTo);

			// Create multipart message parts
			header = multiPartMessageServiceImpl.getHeader(receivedDataBodyBinary);
			multipartMessageParts.put("header", header);
			payload = multiPartMessageServiceImpl.getPayload(receivedDataBodyBinary);
			if(payload!=null) {
				multipartMessageParts.put("payload", payload);
			}
			message = multiPartMessageServiceImpl.getMessage(multipartMessageParts.get("header"));
			
			// Return exchange
			exchange.getOut().setHeaders(headesParts);
			exchange.getOut().setBody(multipartMessageParts);

		} catch (Exception e) {
			logger.error("Error parsing multipart message:" + e);
			rejectionMessageServiceImpl.sendRejectionMessage(
					RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES, 
					message);
		}

	}

}
