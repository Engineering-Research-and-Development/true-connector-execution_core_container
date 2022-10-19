package it.eng.idsa.businesslogic.processor.common;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.ContractAgreementMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessage;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.audit.TrueConnectorEvent;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.usagecontrol.service.UsageControlService;
import it.eng.idsa.multipart.domain.MultipartMessage;

/**
 * Processor that handles ContractAgreementMessage and sends ContractAgreement
 * (payload of MultipartMessage) to UsageControl DataApp</br>
 * This processor should be called before calling ClearingHouse registration
 * processor, in order to be compliant with IDS flow.
 * 
 * @author igor.balog
 *
 */
@Component
public class ContractAgreementProcessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(ContractAgreementProcessor.class);
	private UsageControlService usageControlService;
	private RejectionMessageService rejectionMessageService;
	private Boolean isEnabledUsageControl;
	private ApplicationEventPublisher publisher;

	public ContractAgreementProcessor(@Nullable UsageControlService usageControlService,
			@Value("#{new Boolean('${application.isEnabledUsageControl}')}") Boolean isEnabledUsageControl,
			RejectionMessageService rejectionMessageService,
			ApplicationEventPublisher publisher) {
		this.usageControlService = usageControlService;
		this.isEnabledUsageControl = isEnabledUsageControl;
		this.rejectionMessageService = rejectionMessageService;
		this.publisher = publisher;
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);

		if (!isEnabledUsageControl 
				|| !(multipartMessage.getHeaderContent() instanceof MessageProcessedNotificationMessage)
				|| null == exchange.getProperty("Original-Message-Header")
				|| !(exchange.getProperty("Original-Message-Header") instanceof ContractAgreementMessage)
				|| null == exchange.getProperty("Original-Message-Payload")) {
			logger.info("Policy upload interupted - IsEnabledUsegeControl is {} or requirements not met", isEnabledUsageControl);
			return;
		}
		logger.info("Uploading policy...");
		String contractAgreement = (String) exchange.getProperty("Original-Message-Payload");
		
		String response = null;
		try {
			response = usageControlService.uploadPolicy(contractAgreement);
			publisher.publishEvent(new TrueConnectorEvent(TrueConnectorEventType.CONNECTOR_CONTRACT_AGREEMENT_SUCCESS, multipartMessage));
		} catch (Exception e) {
			logger.warn("Policy not uploaded - {}", e.getMessage());
			publisher.publishEvent(new TrueConnectorEvent(TrueConnectorEventType.CONNECTOR_CONTRACT_AGREEMENT_FAILED, multipartMessage));
			rejectionMessageService.sendRejectionMessage((Message) exchange.getProperty("Original-Message-Header"), RejectionReason.NOT_AUTHORIZED);
		}
		logger.info("UsageControl DataApp response {}", response);
	}
}
