package it.eng.idsa.businesslogic.processor.common;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.impl.ProtocolValidationService;

@Component
public class ProtocolValidationProcessor implements Processor {
	
	private static final Logger logger = LoggerFactory.getLogger(ProtocolValidationProcessor.class);
	
	@Value("${application.enableProtocolValidation}")
	private boolean enableProtocolValidation;
	
	@Autowired(required = false)
	private ProtocolValidationService protocolValidationService;

	@Override
	public void process(Exchange exchange) throws Exception {
		if (!enableProtocolValidation) {
			logger.info("Skipping protocol validation");
			return;
		}
		logger.info("Processing Forward-To protocol");
		String forwardTo = (String) exchange.getMessage().getHeader("Forward-To");
		String validatedForwardTo = protocolValidationService.validateProtocol(forwardTo, (Message) exchange.getProperty("Original-Message-Header"));
		exchange.getMessage().getHeaders().replace("Forward-To", validatedForwardTo);
	}

}
