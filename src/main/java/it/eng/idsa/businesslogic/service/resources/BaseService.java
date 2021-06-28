package it.eng.idsa.businesslogic.service.resources;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class BaseService<T> {

	@Autowired
	private BaseRepository<T> repository;

	public T get(UUID entityId) {
		final var entity = repository.findById(entityId);
		if (entity.isEmpty()) {
			 throw new ResourceNotFoundException(this.getClass().getSimpleName() + ": " + entityId);
		}
		return entity.get();
	}

	public T create(final T desc) {
		return persist(desc);
	}

	public T update(final T entity) {
		return persist(entity);
	}

	protected T persist(final T entity) {
		return repository.saveAndFlush(entity);
	}

	public void delete(final UUID entityId) {
		repository.deleteById(entityId);
	}

	public Page<T> getAll(final Pageable pageable) {
		return repository.findAll(pageable);
	}
}
