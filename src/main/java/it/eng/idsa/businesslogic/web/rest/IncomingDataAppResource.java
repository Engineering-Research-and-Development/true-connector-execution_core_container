package it.eng.idsa.businesslogic.web.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
 * REST controller for managing Incoming Data App.
 */
@RestController
@RequestMapping({ "/incoming-data-app" })
public class IncomingDataAppResource {

	// EXAMPLE: How to create the object Logger
	private static final Logger logger = LogManager.getLogger(IncomingDataAppResource.class);
	// EXAMPLE: How to create the object ApplicationConfiguration
	@Autowired
	private ApplicationConfiguration configuration;

	@Autowired
	private MultiPartMessageServiceImpl multiPartMessageService;

	@Autowired
	private CommunicationServiceImpl communicationMessageService;

	@Autowired
	private DapsServiceImpl dapsServiceImpl;

	//@Autowired
	//private ClearingHouseServiceImpl clearingHouseService;

	@PostMapping(value="/JSONMessage", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> postMessage(@RequestHeader("Content-Type") String contentType,
			@RequestHeader("Forward-To") String forwardTo, @RequestBody String incomingDataAppMessageBody) {

		// EXAMPLE: How to use the object Logger
		logger.info("Enter to the end-point incoming-data-app/JSONMessage");

		// EXAMPLE: How to use the object ApplicationConfiguration to get the value for
		// the property
		logger.info("property keyStoreName: {}", () -> configuration.getKeyStoreName());

		// EXAMPLE: How to read the Header
		logger.info(String.format("Header '%s' = %s", "Content-Type", contentType));
		logger.info(String.format("Header '%s' = %s", "Forward-To", forwardTo));

		// EXAMPLE: How to read the Body
		logger.info("Body: {}", () -> incomingDataAppMessageBody);

		// TODO: Parse the received imcomingDataappMessage and header and convert to the
		// MultiPartMessage
		Message message=null;
		try {
			message=multiPartMessageService.getMessage(incomingDataAppMessageBody);
			logger.info("header=" + multiPartMessageService.getHeader(incomingDataAppMessageBody));
			logger.info("payload=" + multiPartMessageService.getPayload(incomingDataAppMessageBody));
			logger.info("message id=" + message.getId());
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return ResponseEntity.ok(multiPartMessageService.createRejectionMessageLocalIssues(message));

		}

		//if (message==null)
		//return ResponseEntity.ok(multiPartMessageService.createRejectionMessageLocalIssues(message));

		// TODO: Get the Token from the DAPS
		String token="";
		try {
			token=dapsServiceImpl.getJwtToken();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return ResponseEntity.ok(multiPartMessageService.createRejectionTokenLocalIssues(message));

		}
		if (token.isEmpty()) 
			return ResponseEntity.ok(multiPartMessageService.createRejectionTokenLocalIssues(message));

		logger.info("token=" + token);

		// TODO: Pull the Token into the MultiPartMessage (we will create Data -
		// customized MultiPartMessage (add token in the MultiPartMessage))
		String messageStringWithToken=multiPartMessageService.addToken(message, token);
		logger.info("messageStringWithToken=" + messageStringWithToken);

		// TODO: Send the Data to the Destination (end-point E on the ActiveMQ) (forward
		// to the destination which is in the MultiPartMessage header)


		return ResponseEntity.ok().build();
	}

	@PostMapping(value="/MultipartMessage", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE, "multipart/mixed" }, produces= /*MediaType.MULTIPART_FORM_DATA_VALUE*/ MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> postMessage(@RequestHeader("Content-Type") String contentType,
			@RequestHeader("Forward-To") String forwardTo,  @RequestParam("header")  Object header,             
			@RequestParam("payload") Object payload   ) {



		// EXAMPLE: How to use the object Logger
		logger.info("Enter to the end-point incoming-data-app/MultipartMessage");

		// EXAMPLE: How to use the object ApplicationConfiguration to get the value for
		// the property
		logger.info("property keyStoreName: {}", () -> configuration.getKeyStoreName());

		// EXAMPLE: How to read the Header
		logger.info(String.format("Header '%s' = %s", "Content-Type", contentType));
		logger.info(String.format("Header '%s' = %s", "Forward-To", forwardTo));

		// EXAMPLE: How to read the Body
		logger.info("header: {}", () -> header);
		logger.info("payload: {}", () -> payload);

		// TODO: Parse the received imcomingDataappMessage and header and convert to the
		// MultiPartMessage
		Message message=null;
		try {
			message=multiPartMessageService.getMessage(header);

		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return ResponseEntity.ok(multiPartMessageService.createRejectionMessageLocalIssues(message));

		}
		if (message==null) {
			return ResponseEntity.ok(multiPartMessageService.createRejectionMessageLocalIssues(message));

		}



		logger.info("message id=" + message.getId());

		// TODO: Get the Token from the DAPS
		String token="";
		try {
			token=dapsServiceImpl.getJwtToken();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return ResponseEntity.ok(multiPartMessageService.createRejectionTokenLocalIssues(message));

		}
		if (token==null) 
			return ResponseEntity.ok(multiPartMessageService.createRejectionTokenLocalIssues(message));

		logger.info("token=" + token);
		// TODO: Pull the Token into the MultiPartMessage (we will create Data -
		// customized MultiPartMessage (add token in the MultiPartMessage))
		String messageStringWithToken=multiPartMessageService.addToken(message, token);
		logger.info("messageStringWithToken=" + messageStringWithToken);

		// TODO: Send the Data to the Destination (end-point E on the ActiveMQ) (forward
		// to the destination which is in the MultiPartMessage header)
		org.apache.http.HttpEntity entity = multiPartMessageService.createMultipartMessage(messageStringWithToken, String.valueOf(payload));
		String response = communicationMessageService.sendData(forwardTo, entity);
		if (response==null) {
			logger.info("...communication error");
			return ResponseEntity.ok(multiPartMessageService.createRejectionCommunicationLocalIssues(message));
		}
		else {
			logger.info("data sent to destination "+forwardTo);
			logger.info("response "+response);
		}
		// TODO: CH
		//clearingHouseService.registerTransaction(message);

		return ResponseEntity.ok(response);
	}


}