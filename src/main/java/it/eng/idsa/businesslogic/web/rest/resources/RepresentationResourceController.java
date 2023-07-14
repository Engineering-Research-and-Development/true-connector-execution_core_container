package it.eng.idsa.businesslogic.web.rest.resources;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
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
import de.fraunhofer.iais.eis.Representation;
import de.fraunhofer.iais.eis.RepresentationImpl;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.eng.idsa.businesslogic.audit.Auditable;
import it.eng.idsa.businesslogic.audit.TrueConnectorEvent;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;
import it.eng.idsa.businesslogic.service.resources.JsonException;
import it.eng.idsa.businesslogic.service.resources.RepresentationResourceService;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

@Tag(name = "Resource representation controller")
@RestController
@RequestMapping("/api/representation/")
public class RepresentationResourceController {
	
	private static final Logger logger = LoggerFactory.getLogger(OfferedResourceController.class);

	private RepresentationResourceService resourceCatalogService;
	
	private ApplicationEventPublisher publisher;
	

	public RepresentationResourceController(RepresentationResourceService resourceCatalogService,
			ApplicationEventPublisher publisher) {
		super();
		this.resourceCatalogService = resourceCatalogService;
		this.publisher = publisher;
	}


	@Operation(tags = "Resource representation controller", summary = "Get requested resource representation")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Returns requested resource representation", 
					content = { @Content(mediaType = "application/json", schema = @Schema(implementation = RepresentationImpl.class)) }) })
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Auditable(eventType = TrueConnectorEventType.REPRESENTATION)
	@ResponseBody
	public ResponseEntity<Object> getRepresentationForResource(@RequestHeader("representation") URI representation)
			throws IOException {
		logger.debug("Fetching representation '{}'", representation);
		return ResponseEntity.ok(
				MultipartMessageProcessor.serializeToJsonLD(resourceCatalogService.getRepresentation(representation)));
	}
	
	
	@Operation(tags = "Resource representation controller", summary = "Add new or update existing resource representation")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Returns modified connector", 
					content = { @Content(mediaType = "application/json", schema = @Schema(implementation = BaseConnectorImpl.class)) }) })
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> addRepresentationToResource(@RequestHeader("resource") URI resource,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(content = { @Content(mediaType = "application/json", schema = @Schema(implementation = RepresentationImpl.class)) })
			@RequestBody String representation,
			HttpServletRequest request) throws IOException {
		String correlationId = UUID.randomUUID().toString();
		publisher.publishEvent(new TrueConnectorEvent(request, TrueConnectorEventType.HTTP_REQUEST_RECEIVED, correlationId, representation));
		Connector modifiedConnector = null;
		try {
			Serializer s = new Serializer();
			Representation r = s.deserialize(representation, Representation.class);
			logger.info("Adding representation with id '{}' to resource '{}'", r.getId(), resource);
			modifiedConnector = resourceCatalogService.addRepresentationToResource(r, resource);
			publisher.publishEvent(new TrueConnectorEvent(request, TrueConnectorEventType.REPRESENTATION_CREATED, correlationId));
		} catch (IOException e) {
			publisher.publishEvent(new TrueConnectorEvent(request, TrueConnectorEventType.REPRESENTATION_CREATION_FAILED, correlationId));
			throw new JsonException("Error while processing request\n" + e.getMessage());
		}
		return ResponseEntity.ok(MultipartMessageProcessor.serializeToJsonLD(modifiedConnector));
	}
	
	
	@Operation(tags = "Resource representation controller", summary = "Update existing resource representation")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Returns modified connector", 
					content = { @Content(mediaType = "application/json", schema = @Schema(implementation = BaseConnectorImpl.class)) }) })
	@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> updateRepresentationToResource(@RequestHeader("resource") URI resource,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(content = { @Content(mediaType = "application/json", schema = @Schema(implementation = RepresentationImpl.class)) })
			@RequestBody String representation,
			HttpServletRequest request) throws IOException {
		String correlationId = UUID.randomUUID().toString();
		publisher.publishEvent(new TrueConnectorEvent(request, TrueConnectorEventType.HTTP_REQUEST_RECEIVED, correlationId, representation));
		Connector modifiedConnector = null;
		try {
			Serializer s = new Serializer();
			Representation r = s.deserialize(representation, Representation.class);
			logger.info("Update representation with id '{}' to resource '{}'", r.getId(), resource);
			modifiedConnector = resourceCatalogService.updateRepresentationToResource(r, resource);
			publisher.publishEvent(new TrueConnectorEvent(request, TrueConnectorEventType.REPRESENTATION_UPDATED, correlationId));
		} catch (IOException e) {
			publisher.publishEvent(new TrueConnectorEvent(request, TrueConnectorEventType.REPRESENTATION_UPDATE_FAILED, correlationId));
			throw new JsonException("Error while processing request\n" + e.getMessage());
		}
		return ResponseEntity.ok(MultipartMessageProcessor.serializeToJsonLD(modifiedConnector));
	}
	
	
	@Operation(tags = "Resource representation controller", summary = "Delete existing resource representation")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Returns modified connector", 
					content = { @Content(mediaType = "application/json", schema = @Schema(implementation = BaseConnectorImpl.class)) }) })
	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Auditable(eventType = TrueConnectorEventType.REPRESENTATION_DELETED)
	@ResponseBody
	public ResponseEntity<String> deleteRepresentation(@RequestHeader("representation") URI representation)
			throws IOException {
		Connector modifiedConnector = null;
		logger.info("Deleting representation with id '{}'", representation);
		modifiedConnector = resourceCatalogService.deleteRepresentation(representation);
		return ResponseEntity.ok(MultipartMessageProcessor.serializeToJsonLD(modifiedConnector));
	}

}
