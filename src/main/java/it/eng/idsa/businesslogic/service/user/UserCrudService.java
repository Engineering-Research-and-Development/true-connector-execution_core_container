package it.eng.idsa.businesslogic.service.user;

import java.util.Optional;

public interface UserCrudService {

	User save(User user);
	Optional<User> find(String id);
	Optional<User> findByUsername(String username);
	Optional<User> findByUsernameAndPassword(String username, String password);
}
