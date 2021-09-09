/**
 *
 */
package it.eng.idsa.businesslogic.service.impl;

import java.net.URI;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.fraunhofer.iais.eis.LogMessage;
import de.fraunhofer.iais.eis.LogMessageBuilder;
import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration;
import it.eng.idsa.businesslogic.service.ClearingHouseService;
import it.eng.idsa.businesslogic.service.DapsTokenProviderService;
import it.eng.idsa.businesslogic.service.HashFileService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.clearinghouse.model.Body;
import it.eng.idsa.clearinghouse.model.NotificationContent;
import it.eng.idsa.multipart.util.DateUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

/**
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Service
@Transactional
public class ClearingHouseServiceImpl implements ClearingHouseService {
	private static final Logger logger = LoggerFactory.getLogger(ClearingHouseServiceImpl.class);

	@Autowired
	private ApplicationConfiguration configuration;

	@Autowired
	private RejectionMessageService rejectionMessageService;
	
	@Autowired
	private SelfDescriptionConfiguration selfDescriptionConfiguration;
	
	@Autowired
	private DapsTokenProviderService dapsProvider;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private HashFileService hashService;

	@Override
	public boolean registerTransaction(Message correlatedMessage, String payload) {

		boolean success = false;
		try {
			logger.info("registerTransaction...");
			String endpoint = configuration.getClearingHouseUrl();
			// Create Message for Clearing House

			LogMessage logInfo = new LogMessageBuilder()
					._modelVersion_(UtilMessageService.MODEL_VERSION)
					._issuerConnector_(whoIAm())
					._issued_(DateUtil.now())
					._senderAgent_(correlatedMessage.getSenderAgent())
					._securityToken_(dapsProvider.getDynamicAtributeToken())
					.build();

			NotificationContent notificationContent = new NotificationContent();
			notificationContent.setHeader(logInfo);
			Body body = new Body();
			body.setHeader(correlatedMessage);
			String hash = hashService.hash(payload);
			body.setPayload(hash);

			notificationContent.setBody(body);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
//			String msgSerialized = MultipartMessageProcessor.serializeMessage(notificationContent);
			ObjectMapper mapper = new ObjectMapper();
			mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
	        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
			
	        String msgSerialized = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(notificationContent);
			logger.info("msgSerialized to CH=" + msgSerialized);
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(msgSerialized);
			HttpEntity<JSONObject> entity = new HttpEntity<>(jsonObject, headers);

			logger.info("Sending Data to the Clearing House " + endpoint + " ...");
			restTemplate.postForObject(endpoint, entity, String.class);
			logger.info("Data [LogMessage.id=" + logInfo.getId() + "] sent to the Clearing House " + endpoint);
			hashService.recordHash(hash, payload, notificationContent);

			success = true;
		} catch (Exception e) {
			logger.error("Could not register the following message to clearing house", e);
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES,
					correlatedMessage);
		}

		return success;
	}

	private URI whoIAm() {
		return selfDescriptionConfiguration.getConnectorURI();
	}

}
