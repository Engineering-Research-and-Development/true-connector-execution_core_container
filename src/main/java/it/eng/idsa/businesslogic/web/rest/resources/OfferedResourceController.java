package it.eng.idsa.businesslogic.web.rest.resources;

import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.fraunhofer.iais.eis.BaseConnectorImpl;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceImpl;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.eng.idsa.businesslogic.audit.Auditable;
import it.eng.idsa.businesslogic.listener.TrueConnectorEventType;
import it.eng.idsa.businesslogic.service.resources.JsonException;
import it.eng.idsa.businesslogic.service.resources.OfferedResourceService;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

@Tag(name = "Offered resource controller")
@RestController
@RequestMapping("/api/offeredResource/")
public class OfferedResourceController {
	
	private static final Logger logger = LoggerFactory.getLogger(OfferedResourceController.class);
	
	private OfferedResourceService service;
	
	public OfferedResourceController(OfferedResourceService service) {
		this.service = service;
	}

	@Operation(tags = "Offered resource controller", summary = "Get requested resource")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Returns requested resource", 
					content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ResourceImpl.class)) }) })
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Auditable(eventType = TrueConnectorEventType.OFFERED_RESOURCE)
	@ResponseBody
	public ResponseEntity<String> getResource(@RequestHeader("resource") URI resource) throws IOException {
		logger.debug("Fetching offered resource with id '{}'", resource);
		Resource resourceGet = service.getOfferedResource(resource);
		return ResponseEntity.ok(MultipartMessageProcessor.serializeToJsonLD(resourceGet));
	}
	
	
	@Operation(tags = "Offered resource controller", summary = "Add new or update existing resource")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Returns modified connector", 
					content = { @Content(mediaType = "application/json", schema = @Schema(implementation = BaseConnectorImpl.class)) }) })
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Auditable(eventType = TrueConnectorEventType.OFFERED_RESOURCE_CREATED)
	@ResponseBody
	public ResponseEntity<String> addOrUpdateResource(@RequestHeader("catalog") URI catalog,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ResourceImpl.class)) })
			@RequestBody String resource) throws IOException {
		Connector modifiedConnector = null;
		try {
			Serializer s = new Serializer();
			Resource r = s.deserialize(resource, Resource.class);
			logger.info("Adding offered resource with id '{}' to catalog '{}'", r.getId(), catalog);
			modifiedConnector = service.addOfferedResource(catalog, r);
		} catch (IOException e) {
			throw new JsonException("Error while processing request\n" + e.getMessage());
		}
		return ResponseEntity.ok(MultipartMessageProcessor.serializeToJsonLD(modifiedConnector));
	}
	
	
	@Operation(tags = "Offered resource controller", summary = "Update existing resource")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Returns modified connector", 
					content = { @Content(mediaType = "application/json", schema = @Schema(implementation = BaseConnectorImpl.class)) }) })
	@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Auditable(eventType = TrueConnectorEventType.OFFERED_RESOURCE_UPDATED)
	@ResponseBody
	public ResponseEntity<String> updateResource(@RequestHeader("catalog") URI catalog,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ResourceImpl.class)) })
			@RequestBody String resource) throws IOException {
		Connector modifiedConnector = null;
		try {
			Serializer s = new Serializer();
			Resource r = s.deserialize(resource, Resource.class);
			logger.info("Updating offered resource with id '{}' to catalog '{}'", r.getId(), catalog);
			modifiedConnector = service.updateOfferedResource(catalog, r);
		} catch (IOException e) {
			throw new JsonException("Error while processing request\n" + e.getMessage());
		}
		return ResponseEntity.ok(MultipartMessageProcessor.serializeToJsonLD(modifiedConnector));
	}
	
	
	@Operation(tags = "Offered resource controller", summary = "Delete existing resource")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Returns modified connector", 
					content = { @Content(mediaType = "application/json", schema = @Schema(implementation = BaseConnectorImpl.class)) }) })
	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Auditable(eventType = TrueConnectorEventType.OFFERED_RESOURCE_DELETED)
	@ResponseBody
	public ResponseEntity<String> deleteResource(@RequestHeader("resource") URI offeredResource) throws IOException {
		Connector modifiedConnector = null;
		logger.info("Deleting offered resource with id '{}'", offeredResource);
		modifiedConnector = service.deleteOfferedResource(offeredResource);
		return ResponseEntity.ok(MultipartMessageProcessor.serializeToJsonLD(modifiedConnector));
	}
}
