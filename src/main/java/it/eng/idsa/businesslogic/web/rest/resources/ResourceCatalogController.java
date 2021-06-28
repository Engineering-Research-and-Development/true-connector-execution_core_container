package it.eng.idsa.businesslogic.web.rest.resources;

import java.net.URI;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import it.eng.idsa.businesslogic.service.resources.ResourceCatalogService;

@RestController(value = "/resoureController")
public class ResourceCatalogController {
	
	@Autowired
	private ResourceCatalogService resourceCatalogService;

	@GetMapping
	public ResponseEntity<String> getResourceCatalog(UUID id) {
		resourceCatalogService.getResourceCatalog(URI.create("https://w3id.org/idsa/autogen/resourceCatalog/" + id));
		
		return new ResponseEntity<>("aaaa", null, HttpStatus.CREATED);
	}
}
