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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.Representation;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.businesslogic.service.resources.RepresentationResourceService;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

@RestController
@RequestMapping("/api/representation/")
public class RepresentationResourceController {
	
	private static final Logger logger = LoggerFactory.getLogger(OfferedResourceController.class);

	private RepresentationResourceService resourceCatalogService;
	
	public RepresentationResourceController(RepresentationResourceService resourceCatalogService) {
		this.resourceCatalogService = resourceCatalogService;
	}

	@GetMapping
	@ResponseBody
	public ResponseEntity<String> getRepresentationForResource(@RequestHeader("representation") URI representation)
			throws IOException {
		logger.debug("Fetching representation '{}'", representation);
		return ResponseEntity.ok(
				MultipartMessageProcessor.serializeToJsonLD(resourceCatalogService.getRepresentation(representation)));
	}
	
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> addOrUpdateRepresentationToResource(@RequestHeader("resource") URI resource,
			@RequestBody String representation) throws IOException {
		Connector modifiedConnector = null;
		try {
			Serializer s = new Serializer();
			Representation r = s.deserialize(representation, Representation.class);
			logger.debug("Adding/Update representation with id '{}' to resource '{}'", r.getId(), resource);
			modifiedConnector = resourceCatalogService.addOrUpdateRepresentationToResource(r, resource);
		} catch (IOException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		return ResponseEntity.ok(MultipartMessageProcessor.serializeToJsonLD(modifiedConnector));
	}
	
	@DeleteMapping
	@ResponseBody
	public ResponseEntity<String> deleteRepresentation(@RequestHeader("representation") URI representation)
			throws IOException {
		Connector modifiedConnector = null;
		logger.debug("Deleting representation with id '{}'", representation);
		modifiedConnector = resourceCatalogService.deleteRepresentation(representation);
		return ResponseEntity.ok(MultipartMessageProcessor.serializeToJsonLD(modifiedConnector));
	}

}
