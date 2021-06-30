package it.eng.idsa.businesslogic.service.resources;

import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.gson.JsonSyntaxException;

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
		try {
			return sdManager.getOfferedResource(SelfDescription.getInstance().getConnector(), resourceId);
		} catch (JsonSyntaxException | IOException e) {
			logger.error("Error while trying to get offered resource", e);
			throw new ResourceNotFoundException("Error while trying to get offered resource");
		}
	}

	public Connector addOfferedResource(URI resourceCatalogId, Resource resource)
			throws JsonSyntaxException, IOException {
		return sdManager.addOrUpdateOfferedResource(SelfDescription.getInstance().getConnector(), resourceCatalogId,
				resource);
	}
	
	public Connector deleteOfferedResource(URI resourceId) throws JsonSyntaxException, IOException {
		return sdManager.deleteOfferedResource(SelfDescription.getInstance().getConnector(), 
				resourceId);
	}
}
