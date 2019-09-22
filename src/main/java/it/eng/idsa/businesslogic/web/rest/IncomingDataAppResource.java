package it.eng.idsa.businesslogic.web.rest;

import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.eng.idsa.businesslogic.configuration.ApplicationConfiguration;

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

	@PostMapping("/message")
	public ResponseEntity<?> postMessage(@RequestHeader MultiValueMap<String, String> imcomingDataappMessageHeader,
			@RequestBody String imcomingDataappMessageBody) {
		
		// EXAMPLE: How to use the object Logger
		logger.info("Enter to the end-point incoming-data-app/message");
		
		// EXAMPLE: How to use the object ApplicationConfiguration to get the value for the property
		logger.info("property keyStoreName: {}", () -> configuration.getKeyStoreName());
		
		// EXAMPLE: How to read the Header
		imcomingDataappMessageHeader.forEach((key, value) -> {
			logger.info(String.format("Header '%s' = %s", key, value.stream().collect(Collectors.joining("|"))));
	    });
		
		// EXAMPLE: How to read the Body
		logger.info("Body: {}", () -> imcomingDataappMessageBody);
		
		// TODO: Parse the received imcomingDataappMessage and header and convert to the
		// MultiPartMessage

		// TODO: Get the Token from the DAPS

		// TODO: Pull the Token into the MultiPartMessage (we will create Data -
		// customized MultiPartMessage (add token in the MultiPartMessage))

		// TODO: Send the Data to the Destination (end-point E on the ActiveMQ) (forward
		// to the destination which is in the MultiPartMessage header)

		return ResponseEntity.ok().build();
	}

}
