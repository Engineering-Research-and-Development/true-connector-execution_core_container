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
		return sdManager.getRepresentation(SelfDescription.getInstance().getConnector(), representationId);
	}
	
	public Connector addRepresentationToResource(Representation representation, URI resourceId)
			throws JsonSyntaxException, IOException {
		Connector connector = sdManager.addRepresentationToResource(SelfDescription.getInstance().getConnector(),
				representation, resourceId);
		SelfDescription.getInstance().setBaseConnector(connector);
		sdManager.saveConnector();
		return connector;
	}
	
	public Connector updateRepresentationToResource(Representation representation, URI resourceId)
			throws JsonSyntaxException, IOException {
		Connector connector = sdManager.updateRepresentationToResource(SelfDescription.getInstance().getConnector(),
				representation, resourceId);
		SelfDescription.getInstance().setBaseConnector(connector);
		sdManager.saveConnector();
		return connector;
	}

	public Connector deleteRepresentation(URI representation) 
			throws JsonSyntaxException, IOException {
		Connector connector = sdManager.removeRepresentationFromResource(SelfDescription.getInstance().getConnector(), 
				representation);
		SelfDescription.getInstance().setBaseConnector(connector);
		sdManager.saveConnector();
		return connector;
	}
	

}
