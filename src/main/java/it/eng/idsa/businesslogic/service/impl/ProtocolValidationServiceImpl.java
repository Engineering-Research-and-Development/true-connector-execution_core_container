package it.eng.idsa.businesslogic.service.impl;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;

@Service
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

	/**
	 * <p>Validates the Forward-To address protocol if it matches with the one selected in the application.properties. If validation is false, then the protocol will be just added or overwritten.</p>
	 * <p>Examples:</p>
	 * 
	 * <p>validation - true:
	 * selected https -> forwardTo must be https://example.com -> method returns https://example.com</p>
	 * 
	 * <p>validation - false:
	 * selected https -> forwardTo can be example.com or whatever://example.com -> method returns https://example.com</p>
	 * 
	 * @param forwardTo the Forward-To address that is to be checked
	 * @return the correct Forward-To address
	 */
	@Override
	public String validateProtocol(String forwardTo) {
		String requiredProtocol = null;

		requiredProtocol = requiredECCProtocol;

		if (validateProtocol) {
			logger.info("Validating Forward-To protocol");
			if (forwardTo.contains(PROTOCOL_DELIMITER)) {
				String forwardToProtocol = forwardTo.split(PROTOCOL_DELIMITER)[0];
				if (!forwardToProtocol.equals(requiredProtocol)) {
					logger.error("Forward-To protocol not correct. Required: {}", requiredProtocol);
					rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES, null);
				}
			} else {
				logger.error("Forward-To protocol delimiter missing.");
				rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES, null);
			}
		} else {
			logger.info("Applying selected protocol to Forward-To: {}", requiredProtocol);
			if (forwardTo.contains(PROTOCOL_DELIMITER)) {
				String forwardToWithoutProtocol = forwardTo.split(PROTOCOL_DELIMITER)[1];
				forwardTo = requiredProtocol + PROTOCOL_DELIMITER + forwardToWithoutProtocol;
			} else {
				forwardTo = requiredProtocol + PROTOCOL_DELIMITER + forwardTo;
			}
		}
		return forwardTo;
	}
}
