package it.eng.idsa.businesslogic.web.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import it.eng.idsa.businesslogic.service.resources.BadRequestException;
import it.eng.idsa.businesslogic.service.resources.JsonException;
import it.eng.idsa.businesslogic.service.resources.ResourceNotFoundException;

public class RESTExceptionHandlerTest {

	private RESTExceptionHandler handler;

	@BeforeEach
	public void setup() {
		handler = new RESTExceptionHandler();
	}

	@Test
	public void handleResourceNotFoundException() {
		var response = handler.handleResourceNotFoundException(new ResourceNotFoundException("NOT FOUND"));

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}
	
	@Test
	public void handleBadRequestException() {
		var response = handler.handleBadRequestException(new BadRequestException("BAD REQUEST"));
		
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	
	@Test
	public void handleJsonException() {
		var response = handler.handleJsonException(new JsonException("JSON EXCEPTION"));
		
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	
}
