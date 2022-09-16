package it.eng.idsa.businesslogic.web.rest.resources;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;


import java.io.IOException;
import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import de.fraunhofer.iais.eis.Representation;
import de.fraunhofer.iais.eis.RepresentationBuilder;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.businesslogic.service.resources.JsonException;
import it.eng.idsa.businesslogic.service.resources.RepresentationResourceService;

public class RepresentationResourceControllerTest {

	@InjectMocks
	private RepresentationResourceController controller;
	
	@Mock
	private RepresentationResourceService service;
	
	private URI representationURI = URI.create("representation.uri");
	private URI resourceURI = URI.create("resource.uri");

	private Representation r = new RepresentationBuilder().build();
	
	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void getRepresentationForResource() throws IOException {
		when(service.getRepresentation(representationURI)).thenReturn(r);
		
		var response = controller.getRepresentationForResource(representationURI);
		
		assertNotNull(response);
		assertTrue(HttpStatus.OK.equals(response.getStatusCode()));
	}
	
	@Test
	public void addRepresentationToResource() throws IOException {
		Serializer s = new Serializer();
		controller.addRepresentationToResource(resourceURI, s.serialize(r));
		
		verify(service).addRepresentationToResource(r, resourceURI);
	}
	
	@Test
	public void addRepresentationToResource_invalid_json() throws IOException {
		assertThrows(JsonException.class,
				() -> controller.addRepresentationToResource(resourceURI, "REPRESENTATION"));
		
		verify(service, times(0)).addRepresentationToResource(any(Representation.class), any(URI.class));
	}
	
	@Test
	public void updateRepresentationToResource() throws IOException {
		Serializer s = new Serializer();
		controller.updateRepresentationToResource(resourceURI, s.serialize(r));
		
		verify(service).updateRepresentationToResource(r, resourceURI);
	}
	
	@Test
	public void updateRepresentationToResource_invalid_json() throws IOException {
		assertThrows(JsonException.class,
				() -> controller.updateRepresentationToResource(resourceURI, "REPRESENTATION"));
		
		verify(service, times(0)).updateRepresentationToResource(r, resourceURI);
	}
	
	@Test
	public void deleteRepresentation() throws IOException {
		controller.deleteRepresentation(representationURI);
		
		verify(service).deleteRepresentation(representationURI);
	}
}
