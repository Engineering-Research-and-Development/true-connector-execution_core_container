package it.eng.idsa.businesslogic.processor.common;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import it.eng.idsa.multipart.domain.MultipartMessage;

@Component
public class OriginalMessageProcessor implements Processor {
	
	private static final Logger logger = LoggerFactory.getLogger(OriginalMessageProcessor.class);
	
	@Override
	public void process(Exchange exchange) throws Exception {
		//Original message needed for rejection and usage control
		logger.info("Saving original message");
		
		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);
		exchange.getProperties().put("Original-Message-Header", multipartMessage.getHeaderContent());
		if (null != multipartMessage.getPayloadContent()) {
			exchange.getProperties().put("Original-Message-Payload", multipartMessage.getPayloadContent());
		}

	}

}
