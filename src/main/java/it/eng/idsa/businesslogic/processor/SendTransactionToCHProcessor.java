package it.eng.idsa.businesslogic.processor;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.impl.ClearingHouseServiceImpl;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class SendTransactionToCHProcessor implements Processor {

	@Autowired
	private ClearingHouseServiceImpl clearingHouseService;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		// Get "multipartMessageParts" from the input "exchange"
		Map<String, Object> multipartMessageParts = exchange.getIn().getBody(HashMap.class);
		Message message = (Message) multipartMessageParts.get("message");
		
		// Send the message to the Clearing House (CH)
		//clearingHouseService.registerTransaction(message);
	}

}
