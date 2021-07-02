package it.eng.idsa.businesslogic.web.rest.resources;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.eng.idsa.businesslogic.service.resources.JsonException;
import it.eng.idsa.businesslogic.service.resources.SelfDescription;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

@RestController
@RequestMapping("/api/selfDescripton/")
public class SelfDescriptionController {
	
	private static final Logger logger = LoggerFactory.getLogger(SelfDescriptionController.class);

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getSelfDescription()  {
		logger.debug("Fetching self description");
		try {
			return ResponseEntity.ok(
					MultipartMessageProcessor.serializeToJsonLD(
							SelfDescription.getInstance().getConnector()));
		} catch (IOException e) {
			throw new JsonException("Error while fetching self description document via API");
		}
	}
	
}
