package it.eng.idsa.businesslogic.web.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
	private HttpServletResponse response;
	@Mock
	private Principal principal;
	private RESTExceptionHandler handler;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
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
		var responseEntity = handler.handleResourceNotFoundException(new ResourceNotFoundException("NOT FOUND"), request, response);

		assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
	}
	
	@Test
	public void handleBadRequestException() {
		var responseEntity = handler.handleBadRequestException(new BadRequestException("BAD REQUEST"), request, response);
		
		assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
	}
	
	@Test
	public void handleJsonException() {
		var responseEntity = handler.handleJsonException(new JsonException("JSON EXCEPTION"), request, response);
		
		assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
	}
	
	@Test
	public void handleGenericException() {
		var responseEntity = handler.handleGeneralException(new Exception("SOMETHING WENT WRONG"), request, response);
		
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
	}
	
}
