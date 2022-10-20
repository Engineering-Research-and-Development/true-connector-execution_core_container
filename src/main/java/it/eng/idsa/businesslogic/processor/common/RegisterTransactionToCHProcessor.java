package it.eng.idsa.businesslogic.processor.common;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.audit.CamelAuditable;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;
import it.eng.idsa.businesslogic.service.ClearingHouseService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.multipart.domain.MultipartMessage;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class RegisterTransactionToCHProcessor implements Processor {
	
	private static final Logger logger = LoggerFactory.getLogger(RegisterTransactionToCHProcessor.class);

	@Autowired
	private ClearingHouseService clearingHouseService;
	
	@Autowired
	private RejectionMessageService rejectionMessageService;
	
	@Value("${application.isEnabledClearingHouse}")
	private boolean isEnabledClearingHouse;

	@Override
	@CamelAuditable(successEventType = TrueConnectorEventType.CONNECTOR_CLEARING_HOUSE_SUCCESS, 
	failureEventType = TrueConnectorEventType.CONNECTOR_CLEARING_HOUSE_FAILURE)
	public void process(Exchange exchange) throws Exception {
		if (!isEnabledClearingHouse) {
            logger.info("CH registration not configured - continued with flow");
            return;
        }

		// Get "multipartMessageString" from the input "exchange"
		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);
		// Send data to CH
		boolean registrationStatus = clearingHouseService.registerTransaction(multipartMessage.getHeaderContent(), multipartMessage.getPayloadContent());
		if (registrationStatus) {
			logger.info("Clearing house registered successfully");
		}else {
			logger.info("Failed to register to clearing house");
			rejectionMessageService.sendRejectionMessage((Message) exchange.getProperty("Original-Message-Header"), RejectionReason.INTERNAL_RECIPIENT_ERROR);
		}
		exchange.getMessage().setHeaders(exchange.getMessage().getHeaders());
		exchange.getMessage().setBody(exchange.getMessage().getBody());
	}

}
