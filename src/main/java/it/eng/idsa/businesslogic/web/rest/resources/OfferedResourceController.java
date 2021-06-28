package it.eng.idsa.businesslogic.web.rest.resources;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.fraunhofer.iais.eis.Resource;
import it.eng.idsa.businesslogic.service.resources.OfferedResourceService;
import it.eng.idsa.businesslogic.service.resources.ResourceNotFoundException;

@RestController
@RequestMapping("/offeredResource/catalog")
public class OfferedResourceController {
	
	private static final Logger logger = LoggerFactory.getLogger(OfferedResourceController.class);
	
	private OfferedResourceService service;
	
	public OfferedResourceController(OfferedResourceService service) {
		this.service = service;
	}
	
	@GetMapping("/igor")
	@ResponseBody
	public String returnIgor() {
		return "Igor";
	}

	@GetMapping("{catalogId}/resource/{resourceId}")
	@ResponseBody
	public Resource getResource(@PathVariable String catalogId, @PathVariable String resourceId) {
		try {
			URI catalog = URI.create(URLDecoder.decode(catalogId, "UTF-8"));
			URI resource = URI.create(URLDecoder.decode(resourceId, "UTF-8"));
			logger.debug("Fetching from catalog with id '{}' offered resource with id '{}'", catalog, resource);
			return service.getOfferedResource(catalog, resource);
		} catch (UnsupportedEncodingException e) {
			throw new ResourceNotFoundException("Error while trying to retrieve offered resource");
		}
	}
	
	/*
	@PostMapping
	public boolean addResource() {
		service.addOfferedResource(null, getResource());
	}
	
	@PutMapping
	public void updateResource() {
		service.updateOfferedResource(null, getResource());
	}

	@DeleteMapping
	public void deleteResource() {
		service.deleteOfferedResource(null, null);
	}
	*/
}
