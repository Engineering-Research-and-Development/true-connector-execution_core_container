package it.eng.idsa.businesslogic.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nl.tno.ids.common.multipart.MultiPartMessage;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

/**
 * REST controller for managing Incoming Data Chanel.
 */
@RestController
@RequestMapping({ "/incoming-data-chanel" })
public class IncomingDataChanelResource {

	// Post for the end-point F
	@PostMapping("/message")
	public ResponseEntity<?> postMessage(@RequestBody MultiPartMessage multipartMessage) {

		// TODO: Validate the Token with the DAPS from the received MultiPartMessage

		// TODO: If the Token from the MultiPartMessage pass the DAPS validation, Send
		// the customized received MultiPartMessage(in the MultiPartMessage remove the
		// part in the header "Forward-to") to the end-point G (G is end-point in the
		// Camel. Camel should rout the message from the end-point G to the end-point D
		// of the API Data App)

		// TODO: Send the information about the transaction to the Clearing House (CH)

		return ResponseEntity.ok().build();
	}
}
