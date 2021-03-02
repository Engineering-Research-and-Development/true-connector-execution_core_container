package it.eng.idsa.businesslogic.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.DapsService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.BrokerService;
import it.eng.idsa.businesslogic.service.SendDataToBusinessLogicService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;

@Service
public class BrokerServiceImpl implements BrokerService {
	
	private static final Logger logger = LogManager.getLogger(BrokerServiceImpl.class);
	
	@Autowired
	private SendDataToBusinessLogicService sendDataToBusinessLogicService;
	
	@Autowired
	private DapsService dapsService;
	
	@Autowired
	private MultipartMessageService multiPartMessageService;
	
	@Value("${application.selfdescription.brokerURL}")
	private String brokerURL;
	
	private CloseableHttpResponse response;

	@Override
	public void sendBrokerRequest(Message message, String payload) {
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("Payload-Content-Type", ContentType.APPLICATION_JSON);
		try {
			
			String requestMessage = multiPartMessageService.addToken(message, dapsService.getJwtToken());
			
			MultipartMessage multipartMessage = new MultipartMessageBuilder()
					.withHeaderContent(requestMessage)
					.withPayloadContent(payload)
					.build();
			
			response = sendDataToBusinessLogicService.sendMessageBinary(brokerURL, multipartMessage, headers, true);
			if (response != null) {
				String responseString = new String(response.getEntity().getContent().readAllBytes());
				logger.info("Broker responded with {}", responseString);
			} 
		} catch (Exception e) {
			logger.error("Broker request failed exception reason = {}", e.getMessage());
			
		}
		
	}

}
