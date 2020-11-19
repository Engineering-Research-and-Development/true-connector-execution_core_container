package it.eng.idsa.businesslogic.processor.sender;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.activation.DataHandler;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.multipart.domain.MultipartMessage;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class SenderParseReceivedDataProcessorBodyFormData implements Processor {

	private static final Logger logger = LogManager.getLogger(SenderParseReceivedDataProcessorBodyFormData.class);

	@Value("${application.isEnabledDapsInteraction}")
	private boolean isEnabledDapsInteraction;

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
		Map<String, Object> headesParts = new HashMap<String, Object>();

		// Get from the input "exchange"
		Map<String, Object> receivedDataHeader = exchange.getIn().getHeaders();

		try {
			// Create headers parts
			// Put in the header value of the application.property:
			// application.isEnabledDapsInteraction
			headesParts = exchange.getIn().getHeaders();
			headesParts.put("Is-Enabled-Daps-Interaction", isEnabledDapsInteraction);
			contentType = receivedDataHeader.get("Content-Type").toString();
			headesParts.put("Content-Type", contentType);
			forwardTo = receivedDataHeader.get("Forward-To").toString();
			headesParts.put("Forward-To", forwardTo);

			// Create multipart message parts
			if (headesParts.containsKey("header.org.eclipse.jetty.servlet.contentType")) {
				DataHandler dtHeader = (DataHandler) receivedDataHeader.get("header");
				header = IOUtils.toString(dtHeader.getInputStream(), StandardCharsets.UTF_8);
			} else {
				header = receivedDataHeader.get("header").toString();
			}
			message = multipartMessageService.getMessage(header);
			if (receivedDataHeader.containsKey("payload")) {
				payload = receivedDataHeader.get("payload").toString();
			}
			MultipartMessage multipartMessage = new MultipartMessage(
					receivedDataHeader.entrySet().stream().filter(entry -> entry.getValue() instanceof String)
							.collect(Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue())),
					null, message, null, payload, null, null,null);

			// Return exchange
			exchange.getOut().setHeaders(headesParts);
			exchange.getOut().setBody(multipartMessage);

		} catch (Exception e) {
			logger.error("Error parsing multipart message:", e);
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES, message);
		}
	}
}
