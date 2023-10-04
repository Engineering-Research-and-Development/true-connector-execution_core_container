package it.eng.idsa.businesslogic.web.rest;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.eng.idsa.businesslogic.service.SelfDescriptionService;

public class SelfDescriptionTest {

	@InjectMocks
	private SelfDescription selfDescription;

	@Mock
	private SelfDescriptionService selfDescriptionService;

	@BeforeEach
	public void init() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	public void selfDescriptions() {
		selfDescription.selfDescriptions();
		
		verify(selfDescriptionService).getConnectorSelfDescription();
	}
}
