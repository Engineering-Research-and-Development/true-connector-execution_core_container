package it.eng.idsa.businesslogic.service.resources;

import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.gson.JsonSyntaxException;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.Representation;

@Service
public class RepresentationResourceService {
	
	private static final Logger logger = LoggerFactory.getLogger(RepresentationResourceService.class);

	private SelfDescriptionManager sdManager;
	
	public RepresentationResourceService(SelfDescriptionManager sdManager) {
		this.sdManager = sdManager;
	}

	public Representation getRepresentation(URI representationId) {
		logger.debug("About to search representation with id '{}'", representationId);
		return sdManager.getRepresentation(representationId);
	}
	
	public Connector addOrUpdateRepresentationToResource(Representation representation, URI resourceId)
			throws JsonSyntaxException, IOException {
		return sdManager.addOrUpdateRepresentationToResource(SelfDescription.getInstance().getConnector(),
				representation, resourceId);
	}

	public Connector deleteRepresentation(URI representation) 
			throws JsonSyntaxException, IOException {
		return sdManager.removeRepresentationFromResource(SelfDescription.getInstance().getConnector(), 
				representation);
	}
	

}
