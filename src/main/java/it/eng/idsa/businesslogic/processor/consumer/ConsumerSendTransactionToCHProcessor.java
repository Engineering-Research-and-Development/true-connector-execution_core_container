package it.eng.idsa.businesslogic.processor.consumer;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.service.ClearingHouseService;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ConsumerSendTransactionToCHProcessor implements Processor {


	@Autowired
	private ClearingHouseService clearingHouseService;

	@Override
	public void process(Exchange exchange) throws Exception {

//		// Get "multipartMessageString" from the input "exchange"
//		String multipartMessageString = exchange.getIn().getBody(String.class);
//		// Prepare data for CH
//		String header = multiPartMessageServiceImpl.getHeader(multipartMessageString);
//		String payload = multiPartMessageServiceImpl.getPayload(multipartMessageString);
//		Message message = multiPartMessageServiceImpl.getMessage(header);
//		// Send data to CH
//		clearingHouseService.registerTransaction(message, payload);
//		logger.info(multipartMessageString);
		
		exchange.getOut().setHeaders(exchange.getIn().getHeaders());
		exchange.getOut().setBody(exchange.getIn().getBody());
	}

	public ClearingHouseService getClearingHouseService() {
		return clearingHouseService;
	}

	public void setClearingHouseService(ClearingHouseService clearingHouseService) {
		this.clearingHouseService = clearingHouseService;
	}

}
