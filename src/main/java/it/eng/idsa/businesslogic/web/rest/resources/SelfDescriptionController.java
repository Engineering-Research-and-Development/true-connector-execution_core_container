package it.eng.idsa.businesslogic.web.rest.resources;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fraunhofer.iais.eis.BaseConnectorImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.eng.idsa.businesslogic.audit.Auditable;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;
import it.eng.idsa.businesslogic.service.resources.JsonException;
import it.eng.idsa.businesslogic.service.resources.SelfDescription;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

@Tag(name = "Self description controller", description = "Returns document as is, regardless if it's valid or not")
@RestController
@RequestMapping("/api/selfDescription/")
public class SelfDescriptionController {
	
	private static final Logger logger = LoggerFactory.getLogger(SelfDescriptionController.class);
	
	@Operation(summary = "Self description document", tags = "Self description controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Self description document without validation", 
					content = { @Content(mediaType = "application/json", schema = @Schema(implementation = BaseConnectorImpl.class)) }) })
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Auditable(eventType = TrueConnectorEventType.SELF_DESCRIPTION)
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
