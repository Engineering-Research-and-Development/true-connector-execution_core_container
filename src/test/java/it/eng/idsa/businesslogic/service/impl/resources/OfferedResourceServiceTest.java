package it.eng.idsa.businesslogic.service.impl.resources;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.Resource;
import it.eng.idsa.businesslogic.service.resources.OfferedResourceService;
import it.eng.idsa.businesslogic.service.resources.SelfDescriptionManager;

public class OfferedResourceServiceTest {

	private URI offeredResource = URI.create("offeredResource");

	@InjectMocks
	private OfferedResourceService service;

	@Mock
	private SelfDescriptionManager sdManager;

	@Mock
	private Resource resource;
	@Mock
	private Connector connector;

	@BeforeEach
	public void init() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void getOfferedResource() {
		when(sdManager.getOfferedResource(any(Connector.class), any(URI.class))).thenReturn(resource);
		service.getOfferedResource(offeredResource);
	}
	
	@Test
	public void addOfferedResource() {
		when(sdManager.addOfferedResource(any(Connector.class), any(URI.class), any(Resource.class))).thenReturn(connector);
		service.addOfferedResource(offeredResource, resource);
		verify(sdManager).saveConnector();
	}
	
	@Test
	public void updateOfferedResource() {
		when(sdManager.updateOfferedResource(any(Connector.class), any(URI.class), any(Resource.class))).thenReturn(connector);
		service.updateOfferedResource(offeredResource, resource);
		verify(sdManager).saveConnector();
	}
	
	@Test
	public void deleteOfferedResource() {
		when(sdManager.deleteOfferedResource(any(Connector.class), any(URI.class))).thenReturn(connector);
		service.deleteOfferedResource(offeredResource);
		verify(sdManager).saveConnector();
	}
}
