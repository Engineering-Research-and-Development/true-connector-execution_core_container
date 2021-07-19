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
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.businesslogic.service.resources.JsonException;
import it.eng.idsa.businesslogic.service.resources.OfferedResourceService;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

@RestController
@RequestMapping("/api/offeredResource/")
public class OfferedResourceController {
	
	private static final Logger logger = LoggerFactory.getLogger(OfferedResourceController.class);
	
	private OfferedResourceService service;
	
	public OfferedResourceController(OfferedResourceService service) {
		this.service = service;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> getResource(@RequestHeader("resource") URI resource) throws IOException {
		logger.debug("Fetching offered resource with id '{}'", resource);
		Resource resourceGet = service.getOfferedResource(resource);
		return ResponseEntity.ok(MultipartMessageProcessor.serializeToJsonLD(resourceGet));
	}
	
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> addOrUpdateResource(@RequestHeader("catalog") URI catalog,
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
	
	@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> updateResource(@RequestHeader("catalog") URI catalog,
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
	
	@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> deleteResource(@RequestHeader("resource") URI resource) throws IOException {
		Connector modifiedConnector = null;
		logger.info("Deleting offered resource with id '{}'", resource);
		modifiedConnector = service.deleteOfferedResource(resource);
		return ResponseEntity.ok(MultipartMessageProcessor.serializeToJsonLD(modifiedConnector));
	}
}
