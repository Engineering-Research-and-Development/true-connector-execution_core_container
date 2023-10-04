package it.eng.idsa.businesslogic.web.rest.resources;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;

import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.ContractOfferBuilder;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.businesslogic.service.resources.ContractOfferService;
import it.eng.idsa.businesslogic.service.resources.JsonException;

public class ContractOfferControllerTest {
	
	@InjectMocks
	private ContractOfferController controller;
	
	@Mock
	private ContractOfferService service;
	@Mock
	private HttpServletRequest request;
	@Mock
	private ApplicationEventPublisher publisher;
	@Mock
	private Principal principal;
	
	private URI contractOfferURI = URI.create("contract.offer.uri");
	private URI resourceURI = URI.create("resource.uri");
	
	@BeforeEach
	public void init() {
		MockitoAnnotations.openMocks(this);
		when(request.getUserPrincipal()).thenReturn(principal);
		when(principal.getName()).thenReturn("testUser");
		when(request.getMethod()).thenReturn("POST");
		when(request.getRequestURL()).thenReturn(new StringBuffer("http://test.com"));
		when(request.getHeaderNames()).thenReturn(Collections.enumeration(List.of("Authorization")));
	}
	
	@Test
	public void getContractOffer() throws IOException {
		ContractOffer co = new ContractOfferBuilder().build();
		when(service.getContractOffer(contractOfferURI)).thenReturn(co);
		
		var response = controller.getContractOffer(contractOfferURI);
		
		assertNotNull(response);
		assertTrue(HttpStatus.OK.equals(response.getStatusCode()));
	}
	
	@Test
	public void addOrUpdateContractOffer() throws IOException {
		ContractOffer co = new ContractOfferBuilder().build();
		Serializer s = new Serializer();
		
		controller.addOrUpdateContractOffer(resourceURI, s.serialize(co), request);
		
		verify(service).addContractOfferToResource(co, resourceURI);
	}
	
	@Test
	public void addOrUpdateContractOffer_invalid_json() throws IOException {
		assertThrows(JsonException.class,
				() -> controller.addOrUpdateContractOffer(resourceURI, "CONTRACT_OFFER", request));
		
		verify(service, times(0)).addContractOfferToResource(any(ContractOffer.class), any(URI.class));
	}
	
	@Test
	public void updateContractOffer() throws IOException {
		ContractOffer co = new ContractOfferBuilder().build();
		Serializer s = new Serializer();
		controller.updateContractOffer(resourceURI, s.serialize(co), request);
		
		verify(service).updateContractOfferToResource(co, resourceURI);
	}
	
	@Test
	public void updateContractOffer_invalid_json() throws IOException {
		assertThrows(JsonException.class,
				() -> controller.updateContractOffer(resourceURI, "CONTRACT_OFFER", request));
		
		verify(service, times(0)).addContractOfferToResource(any(ContractOffer.class), any(URI.class));
	}
	
	@Test
	public void deleteContractOffer() throws IOException {
		controller.deleteContractOffer(contractOfferURI, request);
		
		verify(service).deleteContractOfferService(contractOfferURI);
	}

}
