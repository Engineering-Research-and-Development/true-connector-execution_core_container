/**
 * 
 */
package it.eng.idsa.businesslogic.service.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.UUID;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.MessageBuilder;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.service.ClearingHouseService;

/**
 * @author Milan Karajovic and Gabriele De Luca
 *
 */
@Service
@Transactional
public class ClearingHouseServiceImpl implements ClearingHouseService {
	@Autowired
	private ApplicationConfiguration configuration;

	private static final Logger logger = LogManager.getLogger(ClearingHouseServiceImpl.class);
	
	private static URI connectorURI;

	
	
	@Override
	public boolean registerTransaction(Message correlatedMessage) {
		// TODO Auto-generated method stub
		try {
			logger.debug("registerTransaction...");
			try {
				System.out.println("configuration old="+configuration.getDapsUrl());
				System.out.println("configuration uri="+configuration.getUriSchema());
				connectorURI=new URI(configuration.getUriSchema()+configuration.getUriAuthority()+configuration.getUriConnector()+UUID.randomUUID().toString());
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String endpoint=configuration.getClearingHouseUrl();
			RestTemplate restTemplate=new RestTemplate();
			//Create Message for Clearing House
			GregorianCalendar gcal = new GregorianCalendar();
			XMLGregorianCalendar xgcal = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(gcal);
			gcal = new GregorianCalendar();
			xgcal = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(gcal);
			ArrayList<URI> recipientConnectors = new ArrayList<URI>();
			recipientConnectors.add(connectorURI);
			Message message=new MessageBuilder()
					._modelVersion_("1.0.3")
					._issued_(xgcal)
					._correlationMessage_(correlatedMessage.getId())
					._issuerConnector_(connectorURI)
					._recipientConnectors_(recipientConnectors)
					._senderAgent_(null)
					._recipientAgents_(null)
					._transferContract_(null)
					.build();




			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			String msgSerialized = new Serializer().serializePlainJson(message);

			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(msgSerialized);


			HttpEntity<JSONObject> entity = new HttpEntity<>(jsonObject, headers);


			logger.info("Sending Data to the Clearing House "+endpoint+" ...");
			restTemplate.postForObject(endpoint, entity, String.class);
			logger.info("Data sent to the Clearing House "+endpoint);

			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
