package it.eng.idsa.businesslogic.web.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;

import it.eng.idsa.businesslogic.service.resources.BadRequestException;
import it.eng.idsa.businesslogic.service.resources.JsonException;
import it.eng.idsa.businesslogic.service.resources.ResourceNotFoundException;

public class RESTExceptionHandlerTest {
	
	@Mock
	private ApplicationEventPublisher publisher;
	@Mock
	private HttpServletRequest request;
	@Mock
	private Principal principal;
	private RESTExceptionHandler handler;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		handler = new RESTExceptionHandler(publisher);
		when(request.getUserPrincipal()).thenReturn(principal);
		when(principal.getName()).thenReturn("user");
		when(request.getMethod()).thenReturn("GET");
		List<String> headers = new ArrayList<>();
		headers.add("foo");
		Enumeration<String> headersEnumeration = Collections.enumeration(headers);
		when(request.getHeaderNames()).thenReturn(headersEnumeration);
	}

	@Test
	public void handleResourceNotFoundException() {
		var response = handler.handleResourceNotFoundException(new ResourceNotFoundException("NOT FOUND"), request);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}
	
	@Test
	public void handleBadRequestException() {
		var response = handler.handleBadRequestException(new BadRequestException("BAD REQUEST"), request);
		
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	
	@Test
	public void handleJsonException() {
		var response = handler.handleJsonException(new JsonException("JSON EXCEPTION"), request);
		
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
	
	@Test
	public void handleGenericException() {
		var response = handler.handleGeneralException(new Exception("SOMETHING WENT WRONG"), request);
		
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
	}
	
}
