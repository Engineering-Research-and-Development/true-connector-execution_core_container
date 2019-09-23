package it.eng.idsa.businesslogic.web.rest;

import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;
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
	private MultiPartMessageServiceImpl multiPartMessageServiceImpl;

	@Autowired
	private DapsServiceImpl dapsServiceImpl;

	@PostMapping("/message")
	public ResponseEntity<?> postMessage(@RequestHeader("Content-Type") String contentType,
			@RequestHeader("Forward-To") String forwardTo, @RequestBody String imcomingDataappMessageBody) {

		// EXAMPLE: How to use the object Logger
		logger.info("Enter to the end-point incoming-data-app/message");

		// EXAMPLE: How to use the object ApplicationConfiguration to get the value for
		// the property
		logger.info("property keyStoreName: {}", () -> configuration.getKeyStoreName());

		// EXAMPLE: How to read the Header
		logger.info(String.format("Header '%s' = %s", "Content-Type", contentType));
		logger.info(String.format("Header '%s' = %s", "Forward-To", forwardTo));

		// EXAMPLE: How to read the Body
		logger.info("Body: {}", () -> imcomingDataappMessageBody);

		// TODO: Parse the received imcomingDataappMessage and header and convert to the
		// MultiPartMessage
		logger.info("header=" + multiPartMessageServiceImpl.getHeader(imcomingDataappMessageBody));
		logger.info("payload=" + multiPartMessageServiceImpl.getPayload(imcomingDataappMessageBody));
		logger.info("message id=" + multiPartMessageServiceImpl.getMessage(imcomingDataappMessageBody).getId());

		// TODO: Get the Token from the DAPS
		dapsServiceImpl.getJwtToken();

		// TODO: Pull the Token into the MultiPartMessage (we will create Data -
		// customized MultiPartMessage (add token in the MultiPartMessage))

		// TODO: Send the Data to the Destination (end-point E on the ActiveMQ) (forward
		// to the destination which is in the MultiPartMessage header)

		return ResponseEntity.ok().build();
	}

}
