package it.eng.idsa.businesslogic.processor.sender;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
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

	private static final Logger logger = LogManager.getLogger(SenderParseReceivedDataProcessorBodyBinary.class);

	@Autowired
	private RejectionMessageService rejectionMessageService;

	@Override
	public void process(Exchange exchange) throws Exception {

		Message message = null;
		Map<String, Object> headesParts = new HashMap<String, Object>();
		String receivedDataBodyBinary = null;

		// Get from the input "exchange"
		headesParts = exchange.getMessage().getHeaders();
		receivedDataBodyBinary = exchange.getMessage().getBody(String.class);

		if (receivedDataBodyBinary == null) {
			logger.error("Body of the received multipart message is null");
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES, null);
		}

		try {
			MultipartMessage multipartMessage = MultipartMessageProcessor.parseMultipartMessage(receivedDataBodyBinary);
			message= multipartMessage.getHeaderContent();
			if (!checkHeaderContentType(
					multipartMessage.getHeaderHeader().get(MultipartMessageKey.CONTENT_TYPE.label))) {
				logger.error("Content type of the header must be application/json or application/json UTF-8");
				rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES,
						message);
			}
			// Create headers parts
			headesParts.put("Payload-Content-Type",
					multipartMessage.getPayloadHeader().get(MultipartMessageKey.CONTENT_TYPE.label));

			// Return exchange
			exchange.getMessage().setBody(multipartMessage);
			exchange.getMessage().setHeaders(headesParts);
		} catch (Exception e) {
			logger.error("Error parsing multipart message:", e);
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES, message);
		}
	}

	/**
	 * Check if header content type is application/json; UTF-8 or application/json
	 * @param contentType
	 * @return
	 */
	private boolean checkHeaderContentType(String contentType) {
		if (contentType != null && (contentType
				.equals(ContentType.APPLICATION_JSON.toString())
				|| contentType.equals(ContentType.create("application/json").toString())
				|| contentType.equals(ContentType.create("application/json+ld").toString()))) {
			return true;
		}
		return false;
	}

}
