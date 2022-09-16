package it.eng.idsa.businesslogic.web.rest.resources;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceBuilder;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.businesslogic.service.resources.JsonException;
import it.eng.idsa.businesslogic.service.resources.OfferedResourceService;

public class OfferedResourceControllerTest {

	@InjectMocks
	private OfferedResourceController controller;
	@Mock
	private OfferedResourceService service;
	@Mock
	private Resource resource;
	
	private URI resourceURI = URI.create("resource.uri");
	
	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void getResource() throws IOException {
		when(service.getOfferedResource(resourceURI)).thenReturn(resource);
		var response = controller.getResource(resourceURI);
		
		assertNotNull(response);
		assertTrue(HttpStatus.OK.equals(response.getStatusCode()));
		verify(service).getOfferedResource(resourceURI);
	}
	
	@Test
	public void addOrUpdateResource() throws IOException {
		URI catalogURI = URI.create("catalog.uri");
		Resource resource = new ResourceBuilder().build();
		Serializer s = new Serializer();
		controller.addOrUpdateResource(catalogURI, s.serialize(resource));
		
		verify(service).addOfferedResource(catalogURI, resource);
	}
	
	@Test
	public void addOrUpdateResource_invalid_json() throws IOException {
		URI catalogURI = URI.create("catalog.uri");
		
		assertThrows(JsonException.class,
				() -> controller.addOrUpdateResource(catalogURI, "RESOURCE"));
		
		verify(service, times(0)).addOfferedResource(catalogURI, resource);
	}
	
	@Test
	public void updateResource() throws IOException {
		URI catalogURI = URI.create("catalog.uri");
		Resource resource = new ResourceBuilder().build();
		Serializer s = new Serializer();
		controller.updateResource(catalogURI, s.serialize(resource));
		
		verify(service).updateOfferedResource(catalogURI, resource);
	}
	
	@Test
	public void updateResource_invalid_json() throws IOException {
		URI catalogURI = URI.create("catalog.uri");
		
		assertThrows(JsonException.class,
				() -> controller.updateResource(catalogURI, "RESOURCE"));
		
		verify(service, times(0)).updateOfferedResource(catalogURI, resource);
	}
	
	@Test
	public void deleteResource() throws IOException {
		controller.deleteResource(resourceURI);
		
		verify(service).deleteOfferedResource(resourceURI);
	}
}
