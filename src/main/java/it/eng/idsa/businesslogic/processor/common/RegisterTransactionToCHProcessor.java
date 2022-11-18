package it.eng.idsa.businesslogic.processor.common;

import java.util.Optional;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.ContractAgreementMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.audit.TrueConnectorEvent;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;
import it.eng.idsa.businesslogic.configuration.ClearingHouseConfiguration;
import it.eng.idsa.businesslogic.service.ClearingHouseService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.multipart.domain.MultipartMessage;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Component
public class RegisterTransactionToCHProcessor implements Processor {
	
	private static final Logger logger = LoggerFactory.getLogger(RegisterTransactionToCHProcessor.class);

	private Optional<ClearingHouseService> clearingHouseService;
	
	private RejectionMessageService rejectionMessageService;
	
	private ApplicationEventPublisher publisher;
	
	private boolean isEnabledClearingHouse;
	
	public RegisterTransactionToCHProcessor(ClearingHouseConfiguration configuration,
			Optional<ClearingHouseService> clearingHouseService, 
			RejectionMessageService rejectionMessageService,
			ApplicationEventPublisher publisher,
			@Value("${application.isEnabledDapsInteraction}")boolean isEnabledDapsInteraction) {
		super();
		this.clearingHouseService = clearingHouseService;
		this.rejectionMessageService = rejectionMessageService;
		this.publisher = publisher;
		//checks if daps and clearing house are both true
		this.isEnabledClearingHouse = (configuration.getIsEnabledClearingHouse() && isEnabledDapsInteraction) == true ? true : false;
	}


	@Override
	public void process(Exchange exchange) throws Exception {
		if (!isEnabledClearingHouse) {
            logger.info("CH registration not configured - continued with flow");
            return;
        }
		
		// Get "multipartMessageString" from the input "exchange"
		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);
		if (!(multipartMessage.getHeaderContent() instanceof ContractAgreementMessage
				|| multipartMessage.getHeaderContent() instanceof ArtifactRequestMessage
				|| multipartMessage.getHeaderContent() instanceof ArtifactResponseMessage)) {
            logger.info("Skipping clearing house - message is not ContractAgreementMessage, ArtifactRequestMessage, ArtifactResponseMessage");
            return;
        }
		boolean registrationStatus = false;
		Message originalMessage = (Message) exchange.getProperty("Original-Message-Header");
		// Send data to CH
		registrationStatus = clearingHouseService.get().registerTransaction(multipartMessage.getHeaderContent(), multipartMessage.getPayloadContent(), originalMessage);
		if (registrationStatus) {
			publisher.publishEvent(new TrueConnectorEvent(TrueConnectorEventType.CONNECTOR_CLEARING_HOUSE_SUCCESS, multipartMessage));
			logger.info("Clearing house registered successfully");
		}else {
			logger.info("Failed to register to clearing house");
			publisher.publishEvent(new TrueConnectorEvent(TrueConnectorEventType.CONNECTOR_CLEARING_HOUSE_FAILURE, multipartMessage));
			rejectionMessageService.sendRejectionMessage(originalMessage, RejectionReason.INTERNAL_RECIPIENT_ERROR);
		}
		exchange.getMessage().setHeaders(exchange.getMessage().getHeaders());
		exchange.getMessage().setBody(exchange.getMessage().getBody());
	}
}
