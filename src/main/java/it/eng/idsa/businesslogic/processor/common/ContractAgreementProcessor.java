package it.eng.idsa.businesslogic.processor.common;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.ContractAgreementMessage;
import it.eng.idsa.businesslogic.service.CommunicationService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.usagecontrol.service.UsageControlService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
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

	public ContractAgreementProcessor(@Nullable UsageControlService usageControlService,
			CommunicationService communicationService,
			@Value("#{new Boolean('${application.isEnabledUsageControl}')}") Boolean isEnabledUsageControl,
			RejectionMessageService rejectionMessageService) {
		this.usageControlService = usageControlService;
		this.isEnabledUsageControl = isEnabledUsageControl;
		this.rejectionMessageService = rejectionMessageService;
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);

		if (!(isEnabledUsageControl && multipartMessage.getHeaderContent() instanceof ContractAgreementMessage)) {
			logger.info("IsEnabledUsegeControl is {} or Not ContractAgreementMessage - skipping logic and continuing with the flow", isEnabledUsageControl);
			return;
		}
		if (StringUtils.isBlank(multipartMessage.getPayloadContent())) {
			logger.warn("Payload not present but mandatory for this logic");
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON, multipartMessage.getHeaderContent());
			return;
		}
		String response = null;
		try {
			response = usageControlService.uploadPolicy(multipartMessage.getPayloadContent());
		} catch (Exception e) {
			logger.warn("Policy not uploaded - {}", e.getMessage());
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON, multipartMessage.getHeaderContent());
		}
		logger.info("UsageControl DataApp response {}", response);
	}
}
