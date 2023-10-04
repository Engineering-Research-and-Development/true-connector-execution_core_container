package it.eng.idsa.businesslogic.web.rest.resources;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

public class SelfDescriptionControllerTest {

	@InjectMocks
	private SelfDescriptionController controller;
	
	@BeforeEach
	public void init() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	public void getSelfDescription() {
		var response = controller.getSelfDescription();
		assertNotNull(response);
		assertTrue(HttpStatus.OK.equals(response.getStatusCode()));
	}
}
