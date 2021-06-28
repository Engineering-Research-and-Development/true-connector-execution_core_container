package it.eng.idsa.businesslogic.web.rest.resources;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import it.eng.idsa.businesslogic.service.resources.BaseService;

@RestController(value = "/resoures")
public abstract class BaseResourcesController<T> {

	private final BaseService<T> service;

	public BaseResourcesController(BaseService<T> service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<T> create(@RequestBody final T desc) {
		final var obj = service.create(desc);

		final var headers = new HttpHeaders();
//		headers.setLocation(obj.getLink("self").get().toUri());

		return new ResponseEntity<>(obj, headers, HttpStatus.CREATED);
	}
}
