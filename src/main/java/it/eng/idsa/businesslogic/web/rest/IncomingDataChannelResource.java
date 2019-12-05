package it.eng.idsa.businesslogic.web.rest;

import org.apache.http.HttpEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
//import it.eng.idsa.businesslogic.service.impl.ClearingHouseServiceImpl;
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

	//@Autowired
	//private ClearingHouseServiceImpl clearingHouseService;

	// Post for the end-point F
	@PostMapping("/message")
	public ResponseEntity<?> postMessage(@RequestBody String data){
		String header;
		String payload;
		Message message=null;
		try {
			header=multiPartMessageServiceImpl.getHeader(data);
			payload=multiPartMessageServiceImpl.getPayload(data);
			message=multiPartMessageServiceImpl.getMessage(data);
		}
		catch (Exception e) {
			// TODO: handle exception
			return ResponseEntity.ok(multiPartMessageServiceImpl.createRejectionMessage(message));
		}
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
			HttpEntity entity = multiPartMessageServiceImpl.createMultipartMessage(headerWithoutToken,payload, null);


			//TODO 
			//Send directly to the D endpoint (DataApp) configured into the properties file
			//using MultiPart 
			communicationServiceImpl.sendData(/*DEndpoint*/"http://localhost:8081/", entity);
			logger.info("data sent to destination DataApp");
			return ResponseEntity.ok(multiPartMessageServiceImpl.createResultMessage(message));
			// TODO: CH
			//clearingHouseService.registerTransaction(message);

		}else {
			return ResponseEntity.ok(multiPartMessageServiceImpl.createRejectionToken(message));

		}


		// TODO: Send the information about the transaction to the Clearing House (CH)

		//return ResponseEntity.ok().build();
	}





	// Alternative solution
	@PostMapping("/receivedMessage")
	public ResponseEntity<?> postReceivedMessage(@RequestBody String data){
		String header;
		String payload;
		Message message=null;
		try {
			header=multiPartMessageServiceImpl.getHeader(data);
			payload=multiPartMessageServiceImpl.getPayload(data);
			message=multiPartMessageServiceImpl.getMessage(data);
		}
		catch (Exception e) {
			// TODO: handle exception
			return ResponseEntity.ok(multiPartMessageServiceImpl.createRejectionMessage(message));
		}
		logger.info("Enter to the end-point: incoming-data-chanel/receivedMessage");
		// Get the token from the message
		String token = multiPartMessageServiceImpl.getToken(header);
		logger.info("tokenl: {}", () -> token);

		// Validate the Token with the DAPS from the received MultiPartMessage
		boolean isTokenValid = dapsServiceImpl.validateToken(token);
//		boolean isTokenValid = true;
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
			HttpEntity entity = multiPartMessageServiceImpl.createMultipartMessage(headerWithoutToken,payload, null);


			//TODO 
			//Send directly to the D endpoint (DataApp) configured into the properties file
			//using MultiPart 
			String response = communicationServiceImpl.sendData("http://"+configuration.getActivemqAddress()+"/api/message/outcoming?type=queue", entity);
			if (response==null) {
				logger.info("...communication error");
				return ResponseEntity.ok(multiPartMessageServiceImpl.createRejectionCommunicationLocalIssues(message));
			}
			else {
				logger.info("data sent to Data App");
				logger.info("response "+response);
			}
			
			// TODO: CH
			//clearingHouseService.registerTransaction(message);
			return ResponseEntity.ok(multiPartMessageServiceImpl.createResultMessage(message));


		}else {
			return ResponseEntity.ok(multiPartMessageServiceImpl.createRejectionToken(message));
		}

		// TODO: Send the information about the transaction to the Clearing House (CH)

		//return ResponseEntity.ok().build();
	}

}
