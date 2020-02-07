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
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.service.impl.MultiPartMessageServiceImpl;
import nl.tno.ids.common.multipart.MultiPart;
import nl.tno.ids.common.multipart.MultiPartMessage;
import nl.tno.ids.common.multipart.MultiPartMessage.Builder;
import nl.tno.ids.common.serialization.SerializationHelper;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ProducerParseReceivedDataProcessorBodyFormData implements Processor {
	
	private static final Logger logger = LogManager.getLogger(ProducerParseReceivedDataProcessorBodyFormData.class);
	
	@Value("${application.isEnabledDapsInteraction}")
	private boolean isEnabledDapsInteraction;
	
	@Autowired
	private MultiPartMessageServiceImpl multiPartMessageServiceImpl;

	@Override
	public void process(Exchange exchange) throws Exception {
		
		String contentType;
		String forwardTo;
		String header = null;
		String payload = null;
		Message message = null;
		Map<String, Object> headesParts = new HashMap();
		Map<String, Object> multipartMessageParts = new HashMap();

		// Get from the input "exchange"
		Map<String, Object> receivedDataHeader = exchange.getIn().getHeaders();
		
		try {
			// Create headers parts
			// Put in the header value of the application.property: application.isEnabledDapsInteraction
			headesParts.put("Is-Enabled-Daps-Interaction", isEnabledDapsInteraction);
			contentType = receivedDataHeader.get("Content-Type").toString();
			headesParts.put("Content-Type", contentType);
			forwardTo = receivedDataHeader.get("Forward-To").toString();
			headesParts.put("Forward-To", forwardTo);

			// Create multipart message parts
			header = receivedDataHeader.get("header").toString();
			multipartMessageParts.put("header", header);
			payload = receivedDataHeader.get("payload").toString();
			multipartMessageParts.put("payload", payload);
			message = multiPartMessageServiceImpl.getMessage(multipartMessageParts.get("header"));
			
			// Return exchange
			exchange.getOut().setHeaders(headesParts);
			exchange.getOut().setBody(multipartMessageParts);

		} catch (Exception e) {			
			logger.error("Error parsing multipart message:" + e);
			Message rejectionMessageLocalIssues = multiPartMessageServiceImpl
					.createRejectionMessageLocalIssues(message);
			Builder builder = new MultiPartMessage.Builder();
			builder.setHeader(rejectionMessageLocalIssues);
			MultiPartMessage builtMessage = builder.build();
			String stringMessage = MultiPart.toString(builtMessage, false);
			throw new ExceptionForProcessor(stringMessage);
		}
		
	}
	
	
}
