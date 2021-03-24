package it.eng.idsa.businesslogic.processor.common;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.ContractAgreementMessage;
import it.eng.idsa.businesslogic.service.CommunicationService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.multipart.domain.MultipartMessage;

/**
 * Processor that handles ContractAgreementMessage and sends ContractAgreement (payload of MultipartMessage) to UsageControl DataApp</br>
 * This processor should be called before calling ClearingHouse registration processor, in order to be compliant with IDS flow.
 * @author igor.balog
 *
 */
@Component
public class ContractAgreementProcessor implements Processor {
	
	private static final Logger logger = LogManager.getLogger(ContractAgreementProcessor.class);
	private CommunicationService communicationService;
	private String usageControlDataAppURL;
	private String addPolicyEndpoint;
	private RejectionMessageService rejectionMessageService;
	
	public ContractAgreementProcessor(CommunicationService communicationService, 
			@Value("${spring.ids.ucapp.baseUrl}") String usageControlDataAppURL,
			@Value("${spring.ids.ucapp.addPolicyEndpoint}") String addPolicyEndpoint,
			RejectionMessageService rejectionMessageService) {
		this.communicationService = communicationService;
		this.usageControlDataAppURL = usageControlDataAppURL;
		this.addPolicyEndpoint = addPolicyEndpoint;
		this.rejectionMessageService = rejectionMessageService;
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);
		
		if(multipartMessage.getHeaderContent() instanceof ContractAgreementMessage) {
			if(StringUtils.isNotBlank(multipartMessage.getPayloadContent())) {
				String ucDataAppAddPolicyEndpoint = usageControlDataAppURL + addPolicyEndpoint;
				logger.info("ContractAgreementMessage detected, sending payload to Usage Contol DataApp at '{}'", ucDataAppAddPolicyEndpoint);
				String response = communicationService.sendDataAsJson(ucDataAppAddPolicyEndpoint, multipartMessage.getPayloadContent());
				logger.info("UsageControl DataApp response {}", response);
			} else {
				logger.warn("Payload not present but mandatory for this logic");
				rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON, multipartMessage.getHeaderContent());
			}
		} else {
			logger.info("Not ContractAgreementMessage - skipping logic and continuing with the flow");
		}
	}
}
