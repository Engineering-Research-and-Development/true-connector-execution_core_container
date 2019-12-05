package it.eng.idsa.businesslogic.processor.producer;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.service.impl.MultiPartMessageServiceImpl;
import nl.tno.ids.common.serialization.SerializationHelper;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ProducerParseReceivedDataProcessor implements Processor {

	private static final Logger logger = LogManager.getLogger(ProducerParseReceivedDataProcessor.class);

	@Autowired
	private MultiPartMessageServiceImpl multiPartMessageServiceImpl;

	@Override
	public void process(Exchange exchange) throws Exception {

		String contentType;
		String forwardTo;
		String header;
		String payload;
		Message message = null;
		Map<String, Object> headesParts = new HashMap();
		Map<String, Object> multipartMessageParts = new HashMap();

		// Get from the input "exchange"
		Map<String, Object> receivedDataHeader = exchange.getIn().getHeaders();
		String receivedDataBody = exchange.getIn().getBody(String.class);
		if (receivedDataBody == null) {
			logger.error("Body of the received multipart message is null");
			Message rejectionMessageLocalIssues = multiPartMessageServiceImpl
					.createRejectionMessageLocalIssues(message);
			throw new ExceptionForProcessor(SerializationHelper.getInstance().toJsonLD(rejectionMessageLocalIssues));
		}
		try {
			// Create headers parts
			contentType = receivedDataHeader.get("Content-Type").toString();
			headesParts.put("Content-Type", contentType);
			forwardTo = receivedDataHeader.get("Forward-To").toString();
			headesParts.put("Forward-To", forwardTo);

			// Create multipart message parts
			header = multiPartMessageServiceImpl.getHeader(receivedDataBody);
			multipartMessageParts.put("header", header);
			payload = multiPartMessageServiceImpl.getPayload(receivedDataBody);
			multipartMessageParts.put("payload", payload);
			message = multiPartMessageServiceImpl.getMessage(multipartMessageParts.get("header"));
			
			// Return exchange
			exchange.getOut().setHeaders(headesParts);
			exchange.getOut().setBody(multipartMessageParts);

		} catch (Exception e) {
			logger.error("Error parsing multipart message:" + e);
			Message rejectionMessageLocalIssues = multiPartMessageServiceImpl
					.createRejectionMessageLocalIssues(message);
			throw new ExceptionForProcessor(SerializationHelper.getInstance().toJsonLD(rejectionMessageLocalIssues));
		}

	}

}
