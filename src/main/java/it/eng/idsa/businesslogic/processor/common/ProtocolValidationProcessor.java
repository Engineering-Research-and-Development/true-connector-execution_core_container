package it.eng.idsa.businesslogic.processor.common;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import it.eng.idsa.businesslogic.service.impl.ProtocolValidationService;
import it.eng.idsa.multipart.domain.MultipartMessage;

public class ProtocolValidationProcessor implements Processor {
	
	private static final Logger logger = LoggerFactory.getLogger(ProtocolValidationProcessor.class);
	
	@Value("${application.skipProtocolValidation}")
	private boolean skipProtocolValidation;
	
	@Autowired(required = false)
	private ProtocolValidationService protocolValidationService;

	@Override
	public void process(Exchange exchange) throws Exception {
		if (skipProtocolValidation) {
			logger.info("Skipping protocol validation");
			return;
		}
		logger.info("Processing Forward-To protocol");
		String forwardTo = (String) exchange.getMessage().getHeader("Forward-To");
		MultipartMessage mm = exchange.getMessage().getBody(MultipartMessage.class);
		String validatedForwardTo = protocolValidationService.validateProtocol(forwardTo, mm.getHeaderContent());
		exchange.getMessage().getHeaders().replace("Forward-To", validatedForwardTo);
	}

}
