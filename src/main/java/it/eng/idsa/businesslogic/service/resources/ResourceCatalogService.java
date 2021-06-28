package it.eng.idsa.businesslogic.service.resources;

import java.net.URI;

import org.springframework.stereotype.Service;

import de.fraunhofer.iais.eis.ResourceCatalog;
import de.fraunhofer.iais.eis.ResourceCatalogBuilder;

@Service
public class ResourceCatalogService {
	
	public ResourceCatalog getResourceCatalog(URI id) {
		return new ResourceCatalogBuilder(id).build();
	}
	
	public ResourceCatalog createResourceCatalog(ResourceCatalog rc) {
		return new ResourceCatalogBuilder().build();
	}

}
