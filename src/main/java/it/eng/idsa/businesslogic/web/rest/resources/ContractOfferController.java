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
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.ContractOfferImpl;
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
import it.eng.idsa.businesslogic.service.resources.ContractOfferService;
import it.eng.idsa.businesslogic.service.resources.JsonException;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

@Tag(name = "Contract offer controller")
@RestController
@RequestMapping("/api/contractOffer/")
public class ContractOfferController {

	private static final Logger logger = LoggerFactory.getLogger(ContractOfferController.class);
	
	private ContractOfferService service;
	
	private ApplicationEventPublisher publisher;
	
	public ContractOfferController(ContractOfferService service, ApplicationEventPublisher publisher) {
		super();
		this.service = service;
		this.publisher = publisher;
	}

	@Operation(tags = "Contract offer controller", summary = "Get requested contract offer")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Returns requested contract offer", 
					content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ContractOfferImpl.class)) }) })
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@Auditable(eventType = TrueConnectorEventType.CONTRACT_OFFER)
	public ResponseEntity<String> getContractOffer(@RequestHeader("contractOffer") URI contractOffer) 
			throws IOException {
		logger.debug("Fetching contractOffer with id '{}'", contractOffer);
		return ResponseEntity.ok(MultipartMessageProcessor.serializeToJsonLD(service.getContractOffer(contractOffer)));
	}
	
	@Operation(tags = "Contract offer controller", summary = "Add new contract offer")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Returns modified connector", 
					content = { @Content(mediaType = "application/json", schema = @Schema(implementation = BaseConnectorImpl.class)) }) })
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> addOrUpdateContractOffer(@RequestHeader("resource") URI resource,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ContractOfferImpl.class)) })
			@RequestBody String contractOffer,
			HttpServletRequest request) throws IOException {
		String correlationId = UUID.randomUUID().toString();
		publisher.publishEvent(new TrueConnectorEvent(request, TrueConnectorEventType.HTTP_REQUEST_RECEIVED, correlationId, contractOffer));
		Connector modifiedConnector = null;
		try {
			Serializer s = new Serializer();
			ContractOffer co = s.deserialize(contractOffer, ContractOffer.class);
			logger.info("Adding contract offer with id '{}' to resource '{}'", co.getId(), resource);
			modifiedConnector = service.addContractOfferToResource(co, resource);
			publisher.publishEvent(new TrueConnectorEvent(request, TrueConnectorEventType.CONTRACT_OFFER_CREATED, correlationId));
		} catch (IOException e) {
			publisher.publishEvent(new TrueConnectorEvent(request, TrueConnectorEventType.CONTRACT_OFFER_CREATION_FAILED, correlationId));
			throw new JsonException("Error while processing request\n" + e.getMessage());
		}
		return ResponseEntity.ok(MultipartMessageProcessor.serializeToJsonLD(modifiedConnector));
	}
	
	@Operation(tags = "Contract offer controller", summary = "Update existing contract offer")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Returns modified connector", 
					content = { @Content(mediaType = "application/json", schema = @Schema(implementation = BaseConnectorImpl.class)) }) })
	@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> updateContractOffer(@RequestHeader("resource") URI resource,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ContractOfferImpl.class)) })
			@RequestBody String contractOffer,
			HttpServletRequest request) throws IOException {
		String correlationId = UUID.randomUUID().toString();
		publisher.publishEvent(new TrueConnectorEvent(request, TrueConnectorEventType.HTTP_REQUEST_RECEIVED, correlationId, contractOffer));
		Connector modifiedConnector = null;
		try {
			Serializer s = new Serializer();
			ContractOffer co = s.deserialize(contractOffer, ContractOffer.class);
			logger.info("Updatig contract offer with id '{}' to resource '{}'", co.getId(), resource);
			modifiedConnector = service.updateContractOfferToResource(co, resource);
			publisher.publishEvent(new TrueConnectorEvent(request, TrueConnectorEventType.CONTRACT_OFFER_UPDATED, correlationId));
		} catch (IOException e) {
			publisher.publishEvent(new TrueConnectorEvent(request, TrueConnectorEventType.CONTRACT_OFFER_UPDATE_FAILED, correlationId));
			throw new JsonException("Error while processing request\n" + e.getMessage());
		}
		return ResponseEntity.ok(MultipartMessageProcessor.serializeToJsonLD(modifiedConnector));
	}

	@Operation(tags = "Contract offer controller", summary = "Delete existing contract offer")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Returns modified connector", 
					content = { @Content(mediaType = "application/json", schema = @Schema(implementation = BaseConnectorImpl.class)) }) })
	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Auditable(eventType = TrueConnectorEventType.CONTRACT_OFFER_DELETED)
	@ResponseBody
	public ResponseEntity<String> deleteContractOffer(@RequestHeader("contractOffer") URI contractOffer,
			HttpServletRequest request)
			throws IOException {
		Connector modifiedConnector = null;
		logger.info("Deleting offered resource with id '{}'", contractOffer);
		modifiedConnector = service.deleteContractOfferService(contractOffer);
		return ResponseEntity.ok(MultipartMessageProcessor.serializeToJsonLD(modifiedConnector));
	}
}
