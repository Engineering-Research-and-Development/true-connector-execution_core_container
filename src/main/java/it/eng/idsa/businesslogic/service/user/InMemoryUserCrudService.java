package it.eng.idsa.businesslogic.service.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static java.util.Optional.ofNullable;

@Service
public class InMemoryUserCrudService implements UserCrudService {
	
	/**
	 * Username defined in application.properties.
	 */
	@Value("${spring.security.user.name}")
	private String username;

	/**
	 * Password defined in application.properties.
	 */
	@Value("${spring.security.user.password}")
	private String password;

	@Autowired
	private LoginAttemptService loginAttemptService;
	@Autowired
    private HttpServletRequest request;
	@Autowired
	private PasswordEncoder encoder;

	Map<String, User> users = new HashMap<>();

	@PostConstruct
	public void setup() {
		users.put(username, new User(UUID.randomUUID().toString(), username, password));
	}

	@Override
	public User save(User user) {
		return users.put(user.getId(), user);
	}

	@Override
	public Optional<User> find(String id) {
		return ofNullable(users.get(id));
	}

	@Override
	public Optional<User> findByUsername(String username) {
		String ip = getClientIP();
		if (loginAttemptService.isBlocked(ip)) {
			throw new RuntimeException("blocked");
		}
		return users.values().stream().filter(u -> Objects.equals(username, u.getUsername())).findFirst();
	}

	@Override
	public Optional<User> findByUsernameAndPassword(String username, String password) {
		String ip = getClientIP();
		if (loginAttemptService.isBlocked(ip)) {
			throw new RuntimeException("blocked");
		}
		return users.values().stream().filter(u -> Objects.equals(username, u.getUsername()) 
				&& (encoder.matches(password, u.getPassword())))
				.findFirst();
	}

	private String getClientIP() {
	    String xfHeader = request.getHeader("X-Forwarded-For");
	    if (xfHeader == null){
	        return request.getRemoteAddr();
	    }
	    return xfHeader.split(",")[0];
	}

}
