package it.eng.idsa.businesslogic.processor.consumer;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.service.impl.MultiPartMessageServiceImpl;
import nl.tno.ids.common.multipart.MultiPart;
import nl.tno.ids.common.multipart.MultiPartMessage;
import nl.tno.ids.common.multipart.MultiPartMessage.Builder;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ConsumerMultiPartMessageProcessor implements Processor {
	
	private static final Logger logger = LogManager.getLogger(ConsumerMultiPartMessageProcessor.class);

	@Autowired
	private MultiPartMessageServiceImpl multiPartMessageServiceImpl;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		String header;
		String payload;
		Message message=null;
		Map<String, Object> multipartMessageParts = new HashMap();
		
		if(
			exchange.getIn().getHeaders().containsKey("header")==false || 
			exchange.getIn().getHeaders().containsKey("payload")==false 
			)
		{
			logger.error("Multipart message header or/and payload is null");
			Message rejectionMessage = multiPartMessageServiceImpl.createRejectionMessage(message);
			Builder builder = new MultiPartMessage.Builder();
			builder.setHeader(rejectionMessage);
			MultiPartMessage builtMessage = builder.build();
			String stringMessage = MultiPart.toString(builtMessage, false);
			throw new ExceptionForProcessor(stringMessage);
		}
		try {

			// Create multipart message
			header=exchange.getIn().getHeader("header").toString();
			multipartMessageParts.put("header", header);
			payload=exchange.getIn().getHeader("payload").toString();
			multipartMessageParts.put("payload", payload);
			message=multiPartMessageServiceImpl.getMessage(multipartMessageParts.get("header"));
						
			// Return multipartMessageParts
			exchange.getOut().setBody(multipartMessageParts);
			
		} catch (Exception e) {
			logger.error("Error parsing multipart message:" + e);
			Message rejectionMessage = multiPartMessageServiceImpl.createRejectionMessage(message);
			Builder builder = new MultiPartMessage.Builder();
			builder.setHeader(rejectionMessage);
			MultiPartMessage builtMessage = builder.build();
			String stringMessage = MultiPart.toString(builtMessage, false);
			throw new ExceptionForProcessor(stringMessage);
			
		}
	}
	
}
