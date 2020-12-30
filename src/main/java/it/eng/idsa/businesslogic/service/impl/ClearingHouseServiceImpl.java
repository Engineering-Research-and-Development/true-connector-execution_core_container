/**
 *
 */
package it.eng.idsa.businesslogic.service.impl;

import java.net.URI;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

//import org.apache.camel.util.json.Jsoner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import de.fraunhofer.iais.eis.LogMessage;
import de.fraunhofer.iais.eis.LogMessageBuilder;
import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.service.ClearingHouseService;
import it.eng.idsa.businesslogic.service.HashFileService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.clearinghouse.model.Body;
import it.eng.idsa.clearinghouse.model.NotificationContent;

/**
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Service
@Transactional
public class ClearingHouseServiceImpl implements ClearingHouseService {
	private static final Logger logger = LogManager.getLogger(ClearingHouseServiceImpl.class);

	@Autowired
	private ApplicationConfiguration configuration;
	
	@Autowired
	private RejectionMessageService rejectionMessageService;
	
	@Value("${information.model.version}")
	private String informationModelVersion;
	
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
			XMLGregorianCalendar xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());

			// Infomodel version 4.0.0
			LogMessage logInfo = new LogMessageBuilder()
					._modelVersion_(informationModelVersion)
					._issuerConnector_(whoIAm())
					._issued_(xgcal)
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
			String msgSerialized = MultipartMessageServiceImpl.serializeMessage(notificationContent);
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
			logger.error("Could not register the following message to clearing house:", e);
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES, correlatedMessage);
		}
		
		return success;
	}

	private URI whoIAm() {
		return URI.create("auto-generated");
	}

}
