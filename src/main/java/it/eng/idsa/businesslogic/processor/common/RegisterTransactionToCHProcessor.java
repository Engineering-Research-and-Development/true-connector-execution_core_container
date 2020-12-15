package it.eng.idsa.businesslogic.processor.common;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.service.ClearingHouseService;
import it.eng.idsa.multipart.domain.MultipartMessage;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class RegisterTransactionToCHProcessor implements Processor {
	
	private static final Logger logger = LogManager.getLogger(RegisterTransactionToCHProcessor.class);


	@Autowired
	private ClearingHouseService clearingHouseService;
	
	@Value("${application.isEnabledClearingHouse}")
	private boolean isEnabledClearingHouse;

	@Override
	public void process(Exchange exchange) throws Exception {
		if (!isEnabledClearingHouse) {
            exchange.getOut().setHeaders(exchange.getIn().getHeaders());
            exchange.getOut().setBody(exchange.getIn().getBody());
            return;
        }

		// Get "multipartMessageString" from the input "exchange"
		MultipartMessage multipartMessage = exchange.getIn().getBody(MultipartMessage.class);
		// Send data to CH
		boolean registrationStatus = clearingHouseService.registerTransaction(multipartMessage.getHeaderContent(), multipartMessage.getPayloadContent());
		if (registrationStatus) {
			logger.info("Clearing house registered: " + multipartMessage.getHeaderContent());
		}else {
			logger.info("Could not register to clearing house: " + multipartMessage.getHeaderContent());
		}
		exchange.getOut().setHeaders(exchange.getIn().getHeaders());
		exchange.getOut().setBody(exchange.getIn().getBody());
	}

}
