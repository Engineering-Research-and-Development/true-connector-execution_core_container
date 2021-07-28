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

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.businesslogic.service.resources.ContractOfferService;
import it.eng.idsa.businesslogic.service.resources.JsonException;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

@RestController
@RequestMapping("/api/contractOffer/")
public class ContractOfferController {

	private static final Logger logger = LoggerFactory.getLogger(ContractOfferController.class);
	
	private ContractOfferService service;
	
	public ContractOfferController(ContractOfferService service) {
		this.service = service;
	}
	
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> getContractOffer(@RequestHeader("contractOffer") URI contractOffer) 
			throws IOException {
		logger.debug("Fetching contractOffer with id '{}'", contractOffer);
		return ResponseEntity.ok(MultipartMessageProcessor.serializeToJsonLD(service.getContractOffer(contractOffer)));
	}
	
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> addOrUpdateContractOffer(@RequestHeader("resource") URI resource,
			@RequestBody String contractOffer) throws IOException {
		Connector modifiedConnector = null;
		try {
			Serializer s = new Serializer();
			ContractOffer co = s.deserialize(contractOffer, ContractOffer.class);
			logger.info("Adding contract offer with id '{}' to resource '{}'", co.getId(), resource);
			modifiedConnector = service.addContractOfferToResource(co, resource);
		} catch (IOException e) {
			throw new JsonException("Error while processing request\n" + e.getMessage());
		}
		return ResponseEntity.ok(MultipartMessageProcessor.serializeToJsonLD(modifiedConnector));
	}
	
	@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> updateContractOffer(@RequestHeader("resource") URI resource,
			@RequestBody String contractOffer) throws IOException {
		Connector modifiedConnector = null;
		try {
			Serializer s = new Serializer();
			ContractOffer co = s.deserialize(contractOffer, ContractOffer.class);
			logger.info("Updatig contract offer with id '{}' to resource '{}'", co.getId(), resource);
			modifiedConnector = service.addContractOfferToResource(co, resource);
		} catch (IOException e) {
			throw new JsonException("Error while processing request\n" + e.getMessage());
		}
		return ResponseEntity.ok(MultipartMessageProcessor.serializeToJsonLD(modifiedConnector));
	}
	

	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> deleteContractOffer(@RequestHeader("contractOffer") URI contractOffer)
			throws IOException {
		Connector modifiedConnector = null;
		logger.info("Deleting offered resource with id '{}'", contractOffer);
		modifiedConnector = service.deleteContractOfferService(contractOffer);
		return ResponseEntity.ok(MultipartMessageProcessor.serializeToJsonLD(modifiedConnector));
	}

}
