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
import de.fraunhofer.iais.eis.ContractOffer;
import it.eng.idsa.businesslogic.service.resources.ContractOfferService;
import it.eng.idsa.businesslogic.service.resources.SelfDescriptionManager;

public class ContractOfferServiceTest {

	private URI contractOfferURI = URI.create("contractOffer");

	@InjectMocks
	private ContractOfferService service;

	@Mock
	private SelfDescriptionManager sdManager;
	
	@Mock
	private ContractOffer contractOffer;
	@Mock
	private Connector connector;

	@BeforeEach
	public void init() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void getContractOffer() {
		when(sdManager.getContractOffer(contractOfferURI)).thenReturn(contractOffer);
		service.getContractOffer(contractOfferURI);
	}
	
	@Test
	public void addContractOfferToResource() throws JsonSyntaxException, IOException {
		when(sdManager.addContractOfferToResource(any(Connector.class), any(ContractOffer.class), any(URI.class))).thenReturn(connector);
		service.addContractOfferToResource(contractOffer, contractOfferURI);
		verify(sdManager).saveConnector();
	}
	
	@Test
	public void updateContractOfferToResource() throws JsonSyntaxException, IOException {
		when(sdManager.updateContractOfferToResource(any(Connector.class), any(ContractOffer.class), any(URI.class))).thenReturn(connector);
		service.updateContractOfferToResource(contractOffer, contractOfferURI);
		verify(sdManager).saveConnector();
	}
	
	@Test
	public void deleteContractOfferService() throws JsonSyntaxException, IOException {
		when(sdManager.removeContractOfferFromResource(any(Connector.class), any(URI.class))).thenReturn(connector);
		service.deleteContractOfferService(contractOfferURI);
		verify(sdManager).saveConnector();
	}
}
