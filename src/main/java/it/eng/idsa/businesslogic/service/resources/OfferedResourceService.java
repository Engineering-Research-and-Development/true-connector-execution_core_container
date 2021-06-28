package it.eng.idsa.businesslogic.service.resources;

import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.gson.JsonSyntaxException;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.Resource;
import it.eng.idsa.businesslogic.service.SelfDescriptionService;

@Service
public class OfferedResourceService {
	
	private static final Logger logger = LoggerFactory.getLogger(OfferedResourceService.class);

	private Connector connector;

	private SelfDescriptionManager sdManager;
	private SelfDescriptionService sdService;
	
	public OfferedResourceService(SelfDescriptionManager manager, SelfDescriptionService sdService) {
		this.sdManager = manager;
		this.sdService = sdService;
	}
	
	public Resource getOfferedResource(URI resourceCatalogId, URI resourceId) {
		try {
			return sdManager.getOfferedResource(sdService.getConnector(), resourceCatalogId, resourceId);
		} catch (JsonSyntaxException | IOException e) {
			logger.error("Error while trying to get offered resource", e);
			throw new ResourceNotFoundException("Error while trying to get offered resource");
		}
	}
	/*
	public boolean addOfferedResource(URI resourceCatalogId, Resource resource) {
		try {
			sdManager.addOrUpdateOfferedResource(null, resourceCatalogId, resource);
		} catch (JsonSyntaxException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean updateOfferedResource(URI resourceCatalogId, Resource resource) {
		try {
			sdManager.addOrUpdateOfferedResource(null, resourceCatalogId, resource);
		} catch (JsonSyntaxException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteOfferedResource(URI resourceCatalogId, URI resourceId) throws JsonSyntaxException, IOException {
		sdManager.deleteOfferedResource(connector, resourceCatalogId, resourceId);
	}
	*/
}
