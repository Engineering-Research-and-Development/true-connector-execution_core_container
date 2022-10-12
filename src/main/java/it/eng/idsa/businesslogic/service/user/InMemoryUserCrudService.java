package it.eng.idsa.businesslogic.service.user;

import static java.util.Optional.ofNullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import it.eng.idsa.businesslogic.listener.UserBlockedApplicationEvent;

@Service
public class InMemoryUserCrudService implements UserDetailsService {
	
	private static final Logger logger = LoggerFactory.getLogger(InMemoryUserCrudService.class);
	
	@Autowired
	private ApplicationEventPublisher publisher;
	
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
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		String ip = getClientIP();
		if (loginAttemptService.isBlocked(ip)) {
			logger.info("User '{}' is blocked!", username);
			publisher.publishEvent(new UserBlockedApplicationEvent(request, null));
			throw new RuntimeException("blocked");
		}
		return findByUsername(username).orElse(null);
	}

	public User save(User user) {
		return users.put(user.getId(), user);
	}

	public Optional<User> find(String id) {
		return ofNullable(users.get(id));
	}

	public Optional<User> findByUsername(String username) {
		String ip = getClientIP();
		if (loginAttemptService.isBlocked(ip)) {
			logger.info("User '{}' is blocked", username);
			throw new RuntimeException("blocked");
		}
		return users.values().stream().filter(u -> Objects.equals(username, u.getUsername())).findFirst();
	}

	public Optional<User> findByUsernameAndPassword(String username, String password) {
		String ip = getClientIP();
		if (loginAttemptService.isBlocked(ip)) {
			logger.info("User '{}' is blocked!", username);
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
