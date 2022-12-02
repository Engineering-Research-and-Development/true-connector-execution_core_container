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
import de.fraunhofer.iais.eis.ContractAgreement;
import de.fraunhofer.iais.eis.ContractAgreementMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessage;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.businesslogic.audit.TrueConnectorEvent;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;
import it.eng.idsa.businesslogic.configuration.ClearingHouseConfiguration;
import it.eng.idsa.businesslogic.service.ClearingHouseService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.usagecontrol.service.UsageControlService;
import it.eng.idsa.businesslogic.util.Helper;
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
	
	private Optional<UsageControlService> usageControlService;
	
	private RejectionMessageService rejectionMessageService;
	
	private ApplicationEventPublisher publisher;
	
	private boolean isEnabledClearingHouse;
	
	private Boolean isReceiver;
	
	public RegisterTransactionToCHProcessor(ClearingHouseConfiguration configuration,
			Optional<ClearingHouseService> clearingHouseService,
			Optional<UsageControlService> usageControlService,
			RejectionMessageService rejectionMessageService,
			ApplicationEventPublisher publisher,
			@Value("${application.isEnabledDapsInteraction}")boolean isEnabledDapsInteraction,
			@Value("${application.isReceiver}") Boolean isReceiver) {
		super();
		this.clearingHouseService = clearingHouseService;
		this.usageControlService = usageControlService;
		this.rejectionMessageService = rejectionMessageService;
		this.publisher = publisher;
		//checks if daps and clearing house are both true
		this.isEnabledClearingHouse = configuration.getIsEnabledClearingHouse() && isEnabledDapsInteraction;
		this.isReceiver = isReceiver;
	}


	@Override
	public void process(Exchange exchange) throws Exception {
		if (!isEnabledClearingHouse) {
            logger.info("CH registration not configured - continued with flow");
            return;
        }
		
		// Get "multipartMessageString" from the input "exchange"
		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);
		Message originalMessage = (Message) exchange.getProperty("Original-Message-Header");
		
		boolean registrationSuccessfull = false;
		
		if (multipartMessage.getHeaderContent() instanceof ArtifactRequestMessage
				|| multipartMessage.getHeaderContent() instanceof ArtifactResponseMessage) {
			registrationSuccessfull = clearingHouseService.map(service -> service.registerTransaction(multipartMessage.getHeaderContent(), null)).orElse(false);
			if (registrationSuccessfull) {
				publisher.publishEvent(new TrueConnectorEvent(TrueConnectorEventType.CONNECTOR_CLEARING_HOUSE_SUCCESS, multipartMessage));
				logger.info("Clearing house registered successfully");
				return;
			} else {
				logger.info("Failed to register to clearing house");
				publisher.publishEvent(new TrueConnectorEvent(TrueConnectorEventType.CONNECTOR_CLEARING_HOUSE_FAILURE, multipartMessage));
				rejectionMessageService.sendRejectionMessage(originalMessage, RejectionReason.INTERNAL_RECIPIENT_ERROR);
				return;
			}
		}
		
		if (multipartMessage.getHeaderContent() instanceof MessageProcessedNotificationMessage
				&& originalMessage instanceof ContractAgreementMessage) {
			//since this happens on response we need the payload(Contract Agreement) from the request
			String contractAgreement = (String) exchange.getProperty("Original-Message-Payload");
			ContractAgreement ca = null;
			try {
				ca = new Serializer().deserialize(contractAgreement, ContractAgreement.class);
			} catch (Exception e) {
				logger.error("No valid contract agreement - {}", e.getMessage());
			}
			String contractAgreementUUID = Helper.getUUID(ca.getId());
			if (isReceiver) {
				String pid = clearingHouseService.map(service -> service.createProcessIdAtClearingHouse(originalMessage.getSecurityToken().getTokenValue(), contractAgreementUUID)).orElse(null);
				if (pid != null) {
					registrationSuccessfull = clearingHouseService.map(service -> service.registerTransaction(originalMessage, contractAgreementUUID)).orElse(false);
				}
			} else {
				registrationSuccessfull = clearingHouseService.map(service -> service.registerTransaction(originalMessage, contractAgreementUUID)).orElse(false);
			}
			if (!registrationSuccessfull) {
				usageControlService.ifPresent(service -> service.rollbackPolicyUpload(contractAgreementUUID));
			}
			if (registrationSuccessfull) {
				publisher.publishEvent(new TrueConnectorEvent(TrueConnectorEventType.CONNECTOR_CLEARING_HOUSE_SUCCESS, multipartMessage));
				logger.info("Clearing house registered successfully");
				return;
			} else {
				logger.info("Failed to register to clearing house");
				publisher.publishEvent(new TrueConnectorEvent(TrueConnectorEventType.CONNECTOR_CLEARING_HOUSE_FAILURE, multipartMessage));
				rejectionMessageService.sendRejectionMessage(originalMessage, RejectionReason.INTERNAL_RECIPIENT_ERROR);
				return;
			}
		}
	}
}
