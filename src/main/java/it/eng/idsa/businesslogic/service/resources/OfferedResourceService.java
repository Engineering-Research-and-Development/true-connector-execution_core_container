package it.eng.idsa.businesslogic.service.resources;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.Resource;

@Service
public class OfferedResourceService {
	
	private static final Logger logger = LoggerFactory.getLogger(OfferedResourceService.class);

	private SelfDescriptionManager sdManager;
	
	public OfferedResourceService(SelfDescriptionManager manager) {
		this.sdManager = manager;
	}
	
	public Resource getOfferedResource(URI resourceId) {
		logger.debug("Fetching offered resource with id '{}'", resourceId);
		return sdManager.getOfferedResource(SelfDescription.getInstance().getConnector(), resourceId);
	}

	public Connector addOfferedResource(URI resourceCatalogId, Resource resource) {
		logger.debug("Adding resource '{}' to catalog '{}'", resource.getId(), resourceCatalogId);
		Connector connector = sdManager.addOfferedResource(SelfDescription.getInstance().getConnector(), resourceCatalogId,
				resource);
		SelfDescription.getInstance().setBaseConnector(connector);
		sdManager.saveConnector();
		return connector;
	}
	
	public Connector updateOfferedResource(URI resourceCatalogId, Resource resource) {
		logger.debug("Updating resource '{}' for catalog '{}'", resource.getId(), resourceCatalogId);
		Connector connector = sdManager.updateOfferedResource(SelfDescription.getInstance().getConnector(), resourceCatalogId,
				resource);
		SelfDescription.getInstance().setBaseConnector(connector);
		sdManager.saveConnector();
		return connector;
	}
	
	public Connector deleteOfferedResource(URI resourceId) {
		logger.debug("Deleting resource '{}'", resourceId);
		Connector connector = sdManager.deleteOfferedResource(SelfDescription.getInstance().getConnector(), 
				resourceId);
		SelfDescription.getInstance().setBaseConnector(connector);
		sdManager.saveConnector();
		return connector;
	}
}
