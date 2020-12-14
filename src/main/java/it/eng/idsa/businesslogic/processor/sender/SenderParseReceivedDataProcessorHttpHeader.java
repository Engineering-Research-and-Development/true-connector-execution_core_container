package it.eng.idsa.businesslogic.processor.sender;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.MultipartMessageKey;

@Component
public class SenderParseReceivedDataProcessorHttpHeader implements Processor{
	
	private static final Logger logger = LogManager.getLogger(SenderParseReceivedDataProcessorHttpHeader.class);

	@Value("${application.isEnabledDapsInteraction}")
	private boolean isEnabledDapsInteraction;
	
	@Value("${application.isEnabledClearingHouse}")
	private boolean isEnabledClearingHouse;
	
	@Autowired
	private RejectionMessageService rejectionMessageService;
	
	@Autowired
	private HttpHeaderService headerService;
	
	@Autowired
	MultipartMessageService multipartMessageService;

	@Override
	public void process(Exchange exchange) throws Exception {
		
		Message message = null;
		Map<String, Object> headersParts = null;
		String payload = null;
		Map<String, Object> headerContentHeaders = null;

		// Get from the input "exchange"
		headersParts = exchange.getIn().getHeaders();
		payload = exchange.getIn().getBody(String.class);
		
		try {
			// Put in the header value of the application.property: application.isEnabledDapsInteraction
			headersParts.put("Is-Enabled-Daps-Interaction", isEnabledDapsInteraction);
			headersParts.put("Is-Enabled-Clearing-House", isEnabledClearingHouse);
			headerContentHeaders = headerService.getHeaderContentHeaders(headersParts);
			String header = headerService.getHeaderMessagePartFromHttpHeadersWithoutToken(headersParts);
			message = multipartMessageService.getMessage(header);
			MultipartMessage multipartMessage = new MultipartMessageBuilder()
					.withHttpHeader(headerService.convertMapToStringString(headerContentHeaders))
					.withHeaderContent(header)
					.withHeaderContent(message)
					.withPayloadContent(payload)
					.build();
			headersParts.put("Payload-Content-Type", headersParts.get(MultipartMessageKey.CONTENT_TYPE.label));
			
			// Return exchange
			exchange.getOut().setHeaders(headersParts);
			exchange.getOut().setBody(multipartMessage);

		} catch (Exception e) {
			logger.error("Error parsing multipart message:" + e);
			rejectionMessageService.sendRejectionMessage(
					RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES, 
					message);
		}
	}
}