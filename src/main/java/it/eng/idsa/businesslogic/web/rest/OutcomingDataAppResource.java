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
 * REST controller for managing Outcoming Data App
 */
@RestController
@RequestMapping({ "/outcoming-data-app" })
public class OutcomingDataAppResource {

	// Post for the End point G
	@PostMapping("/message")
	public ResponseEntity<?> postMessage(@RequestBody MultiPartMessage multipartMessage) {

		// TODO: Enable Camel to intercept the request. (Try to do this with the XML
		// anotation in the Camel)

		return ResponseEntity.ok().build();
	}
}
