package it.eng.idsa.businesslogic.processor.common;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionMessage;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.audit.TrueConnectorEvent;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;
import it.eng.idsa.businesslogic.service.DapsService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.TrueConnectorConstants;
import it.eng.idsa.multipart.domain.MultipartMessage;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class ValidateTokenProcessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(ValidateTokenProcessor.class);
	
	@Value("${application.isEnabledDapsInteraction}")
    private boolean isEnabledDapsInteraction;
	
	@Autowired(required = false)
	private DapsService dapsService;
	
	@Autowired
	private RejectionMessageService rejectionMessageService;
	
	@Autowired
	private ApplicationEventPublisher publisher;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		if (!isEnabledDapsInteraction) {
            logger.info("Daps interaction not configured - continued with flow");
            return;
        }
		
		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);
		String correlationId = (String) exchange.getMessage().getHeader(TrueConnectorConstants.CORRELATION_ID);
		
		if (multipartMessage.getHeaderContent() instanceof RejectionMessage) {
			logger.info("Not validating DAT for rejection message");
            return;
		}
		String token = multipartMessage.getHeaderContent().getSecurityToken().getTokenValue();
		
		// Check is "token" valid
		boolean isTokenValid = dapsService.validateToken(token);
		
		if(isTokenValid==false) {			
			logger.error("Token is invalid");
			publisher.publishEvent(new TrueConnectorEvent(TrueConnectorEventType.CONNECTOR_TOKEN_VALIDATED_FAILURE, multipartMessage, correlationId));
			rejectionMessageService.sendRejectionMessage((Message) exchange.getProperty("Original-Message-Header"), RejectionReason.NOT_AUTHENTICATED);
		}
		logger.info("is token valid: "+isTokenValid);
		publisher.publishEvent(new TrueConnectorEvent(TrueConnectorEventType.CONNECTOR_TOKEN_VALIDATED_SUCCESS, multipartMessage, correlationId));
	}
}
