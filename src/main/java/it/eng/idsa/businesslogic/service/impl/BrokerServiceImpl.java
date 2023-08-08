package it.eng.idsa.businesslogic.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.service.BrokerService;
import it.eng.idsa.businesslogic.service.DapsTokenProviderService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.SendDataToBusinessLogicService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.util.UtilMessageService;
import okhttp3.Response;

@Service
public class BrokerServiceImpl implements BrokerService {
	
	private static final Logger logger = LoggerFactory.getLogger(BrokerServiceImpl.class);
	
	private SendDataToBusinessLogicService sendDataToBusinessLogicService;
	private Optional<DapsTokenProviderService> dapsTokenProviderService;
	private MultipartMessageService multiPartMessageService;
	private RejectionMessageService rejectionMessageService;
	private String brokerURL;
	
	public BrokerServiceImpl(SendDataToBusinessLogicService sendDataToBusinessLogicService,
			Optional<DapsTokenProviderService> dapsTokenProviderService,
			MultipartMessageService multiPartMessageService,
			RejectionMessageService rejectionMessageService,
			@Value("${application.selfdescription.brokerURL}") String brokerURL) {
		this.sendDataToBusinessLogicService = sendDataToBusinessLogicService;
		this.dapsTokenProviderService = dapsTokenProviderService;
		this.multiPartMessageService = multiPartMessageService;
		this.rejectionMessageService = rejectionMessageService;
		this.brokerURL = brokerURL;
	}

	@Override
	public void sendBrokerRequest(Message message, String payload) {
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("Payload-Content-Type", ContentType.APPLICATION_JSON);
		try {
			String requestMessage = null;
			String token = getDynamicAtributeToken().getTokenValue();
			if(StringUtils.isNotEmpty(token)) {
				requestMessage = multiPartMessageService.addToken(message, token);
			} else {
				rejectionMessageService.sendRejectionMessage(message, RejectionReason.NOT_AUTHENTICATED);
			}
			
			MultipartMessage multipartMessage = new MultipartMessageBuilder()
					.withHeaderContent(requestMessage)
					.withPayloadContent(payload)
					.build();
			
			try(Response response = sendDataToBusinessLogicService.sendMessageBinary(brokerURL, multipartMessage, headers)) {
				if (response != null) {
					logger.info("Recived response from Broker");
				} 
			}
		} catch (Exception e) {
			logger.error("Broker request failed exception reason = {}", e.getMessage());
		}
	}
	
	private DynamicAttributeToken getDynamicAtributeToken() {
		return dapsTokenProviderService.map(DapsTokenProviderService::getDynamicAtributeToken)
				.orElse(UtilMessageService.getDynamicAttributeToken());
	}

}
