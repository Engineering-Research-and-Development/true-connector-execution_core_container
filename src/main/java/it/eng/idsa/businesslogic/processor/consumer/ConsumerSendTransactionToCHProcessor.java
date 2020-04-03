package it.eng.idsa.businesslogic.processor.consumer;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.impl.ClearingHouseServiceImpl;
import it.eng.idsa.businesslogic.service.impl.MultiPartMessageServiceImpl;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ConsumerSendTransactionToCHProcessor implements Processor {

	private static final Logger logger = LogManager.getLogger(ConsumerSendTransactionToCHProcessor.class);

	@Autowired
	private MultiPartMessageServiceImpl multiPartMessageServiceImpl;

	@Autowired
	private ClearingHouseServiceImpl clearingHouseService;

	@Override
	public void process(Exchange exchange) throws Exception {

		// Get "multipartMessageString" from the input "exchange"
		String multipartMessageString = exchange.getIn().getBody(String.class);
		// Prepare data for CH
		String header = multiPartMessageServiceImpl.getHeader(multipartMessageString);
		String payload = multiPartMessageServiceImpl.getPayload(multipartMessageString);
		Message message = multiPartMessageServiceImpl.getMessage(header);
		// Send data to CH
		clearingHouseService.registerTransaction(message, payload);
		logger.info(multipartMessageString);

	}

	public ClearingHouseServiceImpl getClearingHouseService() {
		return clearingHouseService;
	}

	public void setClearingHouseService(ClearingHouseServiceImpl clearingHouseService) {
		this.clearingHouseService = clearingHouseService;
	}

}
