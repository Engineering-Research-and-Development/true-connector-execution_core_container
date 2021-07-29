package it.eng.idsa.businesslogic.web.rest;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import it.eng.idsa.businesslogic.service.resources.BadRequestException;
import it.eng.idsa.businesslogic.service.resources.JsonException;
import it.eng.idsa.businesslogic.service.resources.ResourceNotFoundException;

@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class RESTExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(RESTExceptionHandler.class);

	@ExceptionHandler(value = { ResourceNotFoundException.class })
	public ResponseEntity<?> handleResourceNotFoundException(final ResourceNotFoundException exception) {
		if (logger.isErrorEnabled()) {
			logger.error("A resource not found exception has been caught. [exception=({})]",
					exception == null ? "Passed null as exception" : exception.getMessage(), exception);
		}

		final var headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Error", "true");

		Map<String, String> map = new HashMap<>();
	    map.put("message", exception.getMessage());

		return new ResponseEntity<>(map, headers, HttpStatus.NOT_FOUND);
	}
	
	
	@ExceptionHandler(value = { BadRequestException.class })
	public ResponseEntity<?> handleBadRequestException(final BadRequestException exception) {
		if (logger.isErrorEnabled()) {
			logger.error("A bad request exception has been caught. [exception=({})]",
					exception == null ? "Passed null as exception" : exception.getMessage(), exception);
		}

		final var headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Error", "true");

		Map<String, String> map = new HashMap<>();
	    map.put("message", exception.getMessage());

		return new ResponseEntity<>(map, headers, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(value = { JsonException.class })
	public ResponseEntity<?> handleJsonException(final JsonException exception) {
		if (logger.isErrorEnabled()) {
			logger.error("Json exception has been caught. [exception=({})]",
					exception == null ? "Passed null as exception" : exception.getMessage(), exception);
		}

		final var headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Error", "true");

		Map<String, String> map = new HashMap<>();
	    map.put("message", exception.getMessage());

		return new ResponseEntity<>(map, headers, HttpStatus.BAD_REQUEST);
	}
}
