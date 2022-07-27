package it.eng.idsa.businesslogic.processor.exception;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.multipart.domain.MultipartMessage;

@Component
public class RejectionProcessor implements Processor {
	
	private static final Logger logger = LoggerFactory.getLogger(RejectionProcessor.class);
	
	@Autowired
	private RejectionMessageService rejectionMessageService;

	@Override
	public void process(Exchange exchange) throws Exception {
		logger.info("Saving request message for rejection");
		rejectionMessageService.saveMessage(exchange.getMessage().getBody(MultipartMessage.class).getHeaderContent());

	}

}
