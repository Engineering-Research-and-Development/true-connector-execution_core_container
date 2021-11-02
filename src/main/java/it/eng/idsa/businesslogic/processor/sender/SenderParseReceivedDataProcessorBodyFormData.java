package it.eng.idsa.businesslogic.processor.sender;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.impl.ProtocolValidationService;
import it.eng.idsa.businesslogic.util.MessagePart;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class SenderParseReceivedDataProcessorBodyFormData implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(SenderParseReceivedDataProcessorBodyFormData.class);
	
	@Autowired
	private ProtocolValidationService protocolValidationService;

	@Autowired
	private RejectionMessageService rejectionMessageService;

	@Override
	public void process(Exchange exchange) throws Exception {

		String header = null;
		String payload = null;
		Message message = null;

		// Get from the input "exchange"
		Map<String, Object> receivedDataHeader = exchange.getMessage().getHeaders();
		logger.info("Received multipart/form request");

		try {
			// Create multipart message parts
			if (receivedDataHeader.containsKey(MessagePart.HEADER)) {
				if(receivedDataHeader.get(MessagePart.HEADER) instanceof DataHandler) {
					DataHandler dtHeader = (DataHandler) receivedDataHeader.get(MessagePart.HEADER);
					header = IOUtils.toString(dtHeader.getInputStream(), StandardCharsets.UTF_8);
				} else {
					header = (String) receivedDataHeader.get(MessagePart.HEADER);
				}
			} 
			logger.debug("Header part {}", header);
			message = MultipartMessageProcessor.getMessage(header);
			if (receivedDataHeader.containsKey(MessagePart.PAYLOAD)) {
				if(receivedDataHeader.get(MessagePart.PAYLOAD) instanceof DataHandler) {
					DataHandler dtPayload = (DataHandler) receivedDataHeader.get(MessagePart.PAYLOAD);
					payload = IOUtils.toString(dtPayload.getInputStream(), StandardCharsets.UTF_8);
				} else {
					payload = (String) receivedDataHeader.get(MessagePart.PAYLOAD);
				}
			}
			logger.debug("Payload part {}", payload);

			MultipartMessage multipartMessage = new MultipartMessageBuilder()
					.withHeaderContent(header)
					.withPayloadContent(payload)
					.build();
			
			String forwardTo =(String) receivedDataHeader.get("Forward-To");
			forwardTo = protocolValidationService.validateProtocol(forwardTo, message);
			receivedDataHeader.replace("Forward-To", forwardTo);

			// Return exchange
			exchange.getMessage().setHeaders(receivedDataHeader);
			exchange.getMessage().setBody(multipartMessage);

		} catch (Exception e) {
			logger.error("Error parsing multipart message:", e);
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES, message);
		}
	}
}
