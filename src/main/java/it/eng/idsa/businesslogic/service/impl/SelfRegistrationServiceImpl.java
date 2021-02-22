package it.eng.idsa.businesslogic.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.DapsService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.SelfRegistrationService;
import it.eng.idsa.businesslogic.service.SendDataToBusinessLogicService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;

@Service
public class SelfRegistrationServiceImpl implements SelfRegistrationService {
	
	private static final Logger logger = LogManager.getLogger(SelfRegistrationServiceImpl.class);
	
	@Autowired
	private SendDataToBusinessLogicService sendDataToBusinessLogicService;
	
	@Autowired
	private DapsService dapsService;
	
	@Autowired
	private MultipartMessageService multiPartMessageService;

	@Override
	public void sendRegistrationRequest(Message message, String selfDescription, String brokerURL) {
		CloseableHttpResponse response = null;
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("Payload-Content-Type", ContentType.APPLICATION_JSON);
		try {
			
			String requestMessage = multiPartMessageService.addToken(message, dapsService.getJwtToken());
			
			MultipartMessage multipartMessage = new MultipartMessageBuilder()
					.withHeaderContent(requestMessage)
					.withPayloadContent(selfDescription)
					.build();
			
			response = sendDataToBusinessLogicService.sendMessageBinary(brokerURL, multipartMessage, headers, true);
			if (response != null) {
				String responseString = new String(response.getEntity().getContent().readAllBytes());
				logger.info("Registration request is succesfull {}", responseString);
			}
		} catch (Exception e) {
			logger.error("Registration request unsuccesfull with broker response: {}", response);
			logger.error("Exception reason = {}", e.getMessage());
			
		}
		
	}

}
