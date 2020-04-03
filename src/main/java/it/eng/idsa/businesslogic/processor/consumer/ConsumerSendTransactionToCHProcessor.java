package it.eng.idsa.businesslogic.processor.consumer;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.service.impl.ClearingHouseServiceImpl;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ConsumerSendTransactionToCHProcessor implements Processor {

	private static final Logger logger = LogManager.getLogger(ConsumerSendTransactionToCHProcessor.class);
	
	@Autowired
	private ClearingHouseServiceImpl clearingHouseService;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		// Get "multipartMessageString" from the input "exchange"
		String multipartMessageString = exchange.getIn().getBody(String.class);
		
		logger.info(multipartMessageString);

	}

	public ClearingHouseServiceImpl getClearingHouseService() {
		return clearingHouseService;
	}

	public void setClearingHouseService(ClearingHouseServiceImpl clearingHouseService) {
		this.clearingHouseService = clearingHouseService;
	}

}
