package it.eng.idsa.businesslogic.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.service.BrokerService;
import it.eng.idsa.businesslogic.service.DapsTokenProviderService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.SendDataToBusinessLogicService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import okhttp3.Response;

@Service
public class BrokerServiceImpl implements BrokerService {
	
	private static final Logger logger = LoggerFactory.getLogger(BrokerServiceImpl.class);
	
	@Autowired
	private SendDataToBusinessLogicService sendDataToBusinessLogicService;
	
	@Autowired
	private DapsTokenProviderService dapsTokenProviderService;
	
	@Autowired
	private MultipartMessageService multiPartMessageService;
	
	@Value("${application.selfdescription.brokerURL}")
	private String brokerURL;
	
	@Autowired
	private RejectionMessageService rejectionMessageService;

	@Override
	public void sendBrokerRequest(Message message, String payload) {
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("Payload-Content-Type", ContentType.APPLICATION_JSON);
		try {
			String requestMessage = null;
			String token = dapsTokenProviderService.provideToken();
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
					String responseString = new String(response.body().string());
					logger.info("Broker responded with {}", responseString);
				} 
			}
		} catch (Exception e) {
			logger.error("Broker request failed exception reason = {}", e.getMessage());
		}
	}

}
