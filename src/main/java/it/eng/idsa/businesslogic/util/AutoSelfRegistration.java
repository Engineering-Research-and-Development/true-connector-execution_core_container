package it.eng.idsa.businesslogic.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.service.DapsService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.SelfDescriptionService;
import it.eng.idsa.businesslogic.service.SendDataToBusinessLogicService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;


@ConditionalOnProperty(name="application.selfdescription.registrateOnStartup", havingValue="true")
@Component
public class AutoSelfRegistration{
	
	private static final Logger logger = LogManager.getLogger(AutoSelfRegistration.class);
	
	@Value("${application.selfdescription.brokerURL}")
	private String brokerURL;
	
	@Autowired
	private SendDataToBusinessLogicService sendDataToBusinessLogicService;
	
	@Autowired
	private SelfDescriptionService selfDescriptionService;
	
	@Autowired
	private DapsService dapsService;
	@Autowired
	private MultipartMessageService multiPartMessageService;
	
	@EventListener(ApplicationReadyEvent.class)
	public void selfRegistrate() {
		logger.info("Starting AutoSelfRegistration");
		CloseableHttpResponse response = null;
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("Payload-Content-Type", ContentType.APPLICATION_JSON);
		try {
			
			String message = multiPartMessageService.addToken(selfDescriptionService.getConnectorAvailbilityMessage(), dapsService.getJwtToken());
			
			MultipartMessage multipartMessage = new MultipartMessageBuilder()
					.withHeaderContent(message)
					.withPayloadContent(selfDescriptionService.getConnectorAsString())
					.build();
			
			response = sendDataToBusinessLogicService.sendMessageBinary(brokerURL, multipartMessage, headers, true);
			if (response != null) {
				String responseString = new String(response.getEntity().getContent().readAllBytes());
				logger.info("AutoSelfRegistration is succesfull {}", responseString);
			}
		} catch (Exception e) {
			logger.info("AutoSelfRegistration is unsuccesfull with response: {}", response);
			logger.error("Could not registrate to broker, {}", e.getMessage());
			
		}
		
		logger.info("AutoSelfRegistration finished");
	}


}
