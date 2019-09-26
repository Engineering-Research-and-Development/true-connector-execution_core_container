package it.eng.idsa.businesslogic.web.rest;

import org.apache.http.HttpEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
import it.eng.idsa.businesslogic.service.impl.ClearingHouseServiceImpl;
import it.eng.idsa.businesslogic.service.impl.CommunicationServiceImpl;
import it.eng.idsa.businesslogic.service.impl.DapsServiceImpl;
import it.eng.idsa.businesslogic.service.impl.MultiPartMessageServiceImpl;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

/**
 * REST controller for managing Incoming Data Chanel.
 */
@RestController
@RequestMapping({ "/incoming-data-channel" })
public class IncomingDataChannelResource {
	
	private static final Logger logger = LogManager.getLogger(IncomingDataChannelResource.class);
	
	@Autowired
	private ApplicationConfiguration configuration;
	
	@Autowired
	private MultiPartMessageServiceImpl multiPartMessageServiceImpl;
	
	@Autowired
	private DapsServiceImpl dapsServiceImpl;

	
	@Autowired
	private CommunicationServiceImpl communicationServiceImpl;

	@Autowired
	private ClearingHouseServiceImpl clearingHouseService;
	
	// Example for the Message:
	//		{
	//		  "authorizationToken" : {
	//		    "@type" : "ids:Token",
	//		    "@id" : "https://w3id.org/idsa/autogen/token/9c3e543a-6f29-46be-b456-b1cd846e0c78",
	//		    "tokenFormat" : {
	//		      "@id" : "https://w3id.org/idsa/code/tokenformat/JWT"
	//		    },
	//		    "tokenValue" : "123"
	//		  },
	//		  "issuerConnector" : "https://ids.tno.nl/test",
	//		  "@type" : "ids:BrokerQueryMessage",
	//		  "modelVersion" : "1.0.2",
	//		  "@id" : "https://w3id.org/idsa/autogen/brokerQueryMessage/6bed5855-489b-4f47-82dc-08c5f1656101",
	//		  "issued" : "2019-07-18T09:51:03.604Z"
	//		}

	// Post for the end-point F
	@PostMapping("/message")
	public ResponseEntity<?> postMessage(@RequestBody String data){
		String header=multiPartMessageServiceImpl.getHeader(data);
		String payload=multiPartMessageServiceImpl.getPayload(data);
		Message message=multiPartMessageServiceImpl.getMessage(data);
		logger.debug("Enter to the end-point: incoming-data-chanel/message");
		// Get the token from the message
		String token = multiPartMessageServiceImpl.getToken(header);
		logger.info("tokenl: {}", () -> token);
		
		// Validate the Token with the DAPS from the received MultiPartMessage
		boolean isTokenValid = dapsServiceImpl.validateToken(token);
		logger.info("isTokenValid="+isTokenValid);
		// TODO: If the Token from the MultiPartMessage pass the DAPS validation, Send
		// the customized received MultiPartMessage(in the MultiPartMessage remove the
		// part in the header "Forward-to") to the end-point G (G is end-point in the
		// Camel. Camel should rout the message from the end-point G to the end-point D
		// of the API Data App)
		
		if(isTokenValid) {
			logger.info("token valid");
			String headerWithoutToken=multiPartMessageServiceImpl.removeToken(message);
			HttpEntity entity = multiPartMessageServiceImpl.createMultipartMessage(headerWithoutToken,payload);
		
			
			//TODO 
			//Send directly to the D endpoint (DataApp) configured into the properties file
			//using MultiPart 
			communicationServiceImpl.sendData(/*DEndpoint*/"http://localhost:8081/", entity);
			logger.info("data sent to destination DataApp");
			// TODO: CH
			clearingHouseService.registerTransaction(message);
			
		}

		// TODO: Send the information about the transaction to the Clearing House (CH)

		return ResponseEntity.ok().build();
	}
	
	
	
	
	
		// Alternative solution
		@PostMapping("/receivedMessage")
		public ResponseEntity<?> postReceivedMessage(@RequestBody String data){
			String header=multiPartMessageServiceImpl.getHeader(data);
			String payload=multiPartMessageServiceImpl.getPayload(data);
			Message message=multiPartMessageServiceImpl.getMessage(data);
			logger.info("Enter to the end-point: incoming-data-chanel/receivedMessage");
			// Get the token from the message
			String token = multiPartMessageServiceImpl.getToken(header);
			logger.info("tokenl: {}", () -> token);
			
			// Validate the Token with the DAPS from the received MultiPartMessage
			boolean isTokenValid = dapsServiceImpl.validateToken(token);
			logger.info("isTokenValid="+isTokenValid);
			// TODO: If the Token from the MultiPartMessage pass the DAPS validation, Send
			// the customized received MultiPartMessage(in the MultiPartMessage remove the
			// part in the header "Forward-to") to the end-point G (G is end-point in the
			// Camel. Camel should rout the message from the end-point G to the end-point D
			// of the API Data App)
			
			if(isTokenValid) {
				logger.info("token valid");
				//return ResponseEntity.ok().build();

				String headerWithoutToken=multiPartMessageServiceImpl.removeToken(message);
				HttpEntity entity = multiPartMessageServiceImpl.createMultipartMessage(headerWithoutToken,payload);
			
				
				//TODO 
				//Send directly to the D endpoint (DataApp) configured into the properties file
				//using MultiPart 
				communicationServiceImpl.sendData("http://"+configuration.getActivemqAddress()+"/api/message/outcoming?type=queue", entity);
				logger.info("data sent to destination DataApp");
				// TODO: CH
				clearingHouseService.registerTransaction(message);
				
			}else {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			}

			// TODO: Send the information about the transaction to the Clearing House (CH)

			return ResponseEntity.ok().build();
		}
		
}
