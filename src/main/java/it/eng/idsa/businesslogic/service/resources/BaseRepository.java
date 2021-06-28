package it.eng.idsa.businesslogic.service.resources;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository <T> extends JpaRepository<T, UUID> {

}
