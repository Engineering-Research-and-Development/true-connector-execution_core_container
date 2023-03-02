package it.eng.idsa.businesslogic.service.impl;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.service.RejectionMessageService;

@Service
@ConditionalOnProperty(name = "application.enableProtocolValidation", havingValue = "true")
public class ProtocolValidationServiceImpl implements ProtocolValidationService {

	private static final Logger logger = LoggerFactory.getLogger(ProtocolValidationServiceImpl.class);
	
	private static final String HTTPS = "https";
	
	private static final String HTTP = "http";
	
	private static final String WSS = "wss";
	
	private static final String PROTOCOL_DELIMITER = "://";
	
	@Value("${application.validateProtocol}")
	private boolean validateProtocol;
	
	@Value("${application.websocket.isEnabled}")
	private boolean websocketBetweenECC;
	
	@Value("${camel.component.jetty.use-global-ssl-context-parameters}")
	private boolean jetty;
	
	@Autowired
	private RejectionMessageService rejectionMessageService;
	
	private String requiredECCProtocol;
	
	@PostConstruct
	private void findRequiredProtocols() {
		if (!jetty && !websocketBetweenECC) {
			requiredECCProtocol = HTTP;
		}else if (jetty && !websocketBetweenECC) {
			requiredECCProtocol = HTTPS;
		}else if (websocketBetweenECC) {
			requiredECCProtocol = WSS;
		}
	}

	@Override
	public String validateProtocol(String forwardTo, Message messageForRejection) {
		String requiredProtocol = null;

		requiredProtocol = requiredECCProtocol;

		if (validateProtocol) {
			logger.info("Validating Forward-To protocol - must be {}", requiredProtocol);
			if (forwardTo.contains(PROTOCOL_DELIMITER)) {
				String forwardToProtocol = forwardTo.split(PROTOCOL_DELIMITER)[0];
				if (!forwardToProtocol.equals(requiredProtocol)) {
					logger.error("Forward-To protocol not correct. Required: {}", requiredProtocol);
					rejectionMessageService.sendRejectionMessage(messageForRejection, RejectionReason.BAD_PARAMETERS);
				} else {
					logger.info("Protocol successfully validated.");
				}
			} else {
				logger.error("Forward-To protocol delimiter missing. Protocol delimiter -> {}", PROTOCOL_DELIMITER);
				rejectionMessageService.sendRejectionMessage(messageForRejection, RejectionReason.BAD_PARAMETERS);
			}
		} else {
			logger.info("Applying selected protocol to Forward-To: {}", requiredProtocol);
			logger.info("Initial Forward-To {}", forwardTo);
			if (forwardTo.contains(PROTOCOL_DELIMITER)) {
				String forwardToWithoutProtocol = forwardTo.split(PROTOCOL_DELIMITER)[1];
				forwardTo = requiredProtocol + PROTOCOL_DELIMITER + forwardToWithoutProtocol;
			} else {
				forwardTo = requiredProtocol + PROTOCOL_DELIMITER + forwardTo;
			}
			logger.info("Forward-To after rewriting {}", forwardTo);
		}
		return forwardTo;
	}
}
