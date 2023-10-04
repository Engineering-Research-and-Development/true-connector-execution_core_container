package it.eng.idsa.businesslogic.service.impl.resources;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.gson.JsonSyntaxException;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.Representation;
import de.fraunhofer.iais.eis.Resource;
import it.eng.idsa.businesslogic.service.resources.RepresentationResourceService;
import it.eng.idsa.businesslogic.service.resources.SelfDescriptionManager;

public class RepresentationResourceServiceTest {

	private URI representationURI = URI.create("representation");
	private URI resourceId = URI.create("resource");
	
	@InjectMocks
	private RepresentationResourceService service;
	
	@Mock
	private SelfDescriptionManager sdManager;
	
	@Mock
	private Representation representation;
	
	@Mock
	private Resource resource;
	@Mock
	private Connector connector;

	@BeforeEach
	public void init() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	public void getRepresentation() {
		service.getRepresentation(representationURI);
	}
	
	@Test
	public void addRepresentationToResource() throws JsonSyntaxException, IOException {
		when(sdManager.addRepresentationToResource(any(Connector.class), any(Representation.class), any(URI.class)))
			.thenReturn(connector);
		service.addRepresentationToResource(representation, resourceId);
		verify(sdManager).saveConnector();
	}
	
	@Test
	public void updateRepresentationToResource() throws JsonSyntaxException, IOException {
		when(sdManager.updateRepresentationToResource(any(Connector.class), any(Representation.class), any(URI.class)))
			.thenReturn(connector);
		service.updateRepresentationToResource(representation, resourceId);
		verify(sdManager).saveConnector();
	}
	
	@Test
	public void deleteRepresentation() throws JsonSyntaxException, IOException {
		when(sdManager.removeRepresentationFromResource(any(Connector.class), any(URI.class)))
			.thenReturn(connector);
		service.deleteRepresentation(representationURI);
		verify(sdManager).saveConnector();
	}
}
