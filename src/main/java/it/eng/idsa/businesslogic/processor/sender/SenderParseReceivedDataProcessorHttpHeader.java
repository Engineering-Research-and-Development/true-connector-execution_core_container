package it.eng.idsa.businesslogic.processor.sender;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.audit.CamelAuditable;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;
import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.exception.MultipartMessageException;
import it.eng.idsa.multipart.util.MultipartMessageKey;

@Component
public class SenderParseReceivedDataProcessorHttpHeader implements Processor{
	
	private static final Logger logger = LoggerFactory.getLogger(SenderParseReceivedDataProcessorHttpHeader.class);

	@Autowired
	private RejectionMessageService rejectionMessageService;
	
	@Autowired
	private HttpHeaderService headerService;
	
	@Autowired
	MultipartMessageService multipartMessageService;
	
	@Override
	@CamelAuditable(successEventType = TrueConnectorEventType.CONNECTOR_REQUEST, 
		failureEventType = TrueConnectorEventType.EXCEPTION_BAD_REQUEST)
	public void process(Exchange exchange) throws Exception {
		
		Message message = null;
		Map<String, Object> headersParts = null;
		String payload = null;
		Map<String, Object> headerContentHeaders = null;

		// Get from the input "exchange"
		headersParts = exchange.getMessage().getHeaders();
		payload = exchange.getMessage().getBody(String.class);
		try {
			headerContentHeaders = headerService.getIDSHeaders(headersParts);
			message = headerService.headersToMessage(headersParts);
			if(message==null) {
				logger.error("Message could not be created - check if all required headers are present.");
				throw new MultipartMessageException("Message could not be created - check if all required headers are present");
			}
			MultipartMessage multipartMessage = new MultipartMessageBuilder()
					.withHttpHeader(headerService.convertMapToStringString(headerContentHeaders))
//					.withHeaderContent(header)
					.withHeaderContent(message)
					.withPayloadContent(payload)
					.build();
			headersParts.put("Payload-Content-Type", headersParts.get(MultipartMessageKey.CONTENT_TYPE.label));

			// Return exchange
			exchange.getMessage().setHeaders(headersParts);
			exchange.getMessage().setBody(multipartMessage);

		} catch (Exception e) {
			logger.error("Error parsing multipart message:" + e);
			rejectionMessageService.sendRejectionMessage(null, RejectionReason.MALFORMED_MESSAGE);
		}
	}
}