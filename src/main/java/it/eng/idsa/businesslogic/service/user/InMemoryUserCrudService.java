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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import it.eng.idsa.businesslogic.audit.TrueConnectorEvent;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;

@Service
@DependsOn({"encoder"})
public class InMemoryUserCrudService implements TrueConnectorUserDetailsService {
	
	private static final Logger logger = LoggerFactory.getLogger(InMemoryUserCrudService.class);
	
	@Autowired
	private ApplicationEventPublisher publisher;
	@Autowired
	private UserConfiguration userConfiguration;
	@Autowired
	private LoginAttemptService loginAttemptService;
	@Autowired
    private HttpServletRequest request;
	@Autowired
	private PasswordEncoder encoder;

	Map<String, User> users = new HashMap<>();

	@PostConstruct
	public void setup() {
		users.put(userConfiguration.getApiUser().getUsername(), new User(UUID.randomUUID().toString(), 
				userConfiguration.getApiUser().getUsername(), userConfiguration.getApiUser().getPassword(), "ADMIN"));
	}
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		String ip = getClientIP();
		if (loginAttemptService.isBlocked(ip)) {
			logger.info("User '{}' is blocked!", username);
			publisher.publishEvent(new TrueConnectorEvent(request, TrueConnectorEventType.USER_BLOCKED));
			throw new RuntimeException("blocked");
		}
		return findByUsername(username).orElse(null);
	}
	
	@Override
	public UserDetails loadCamelUserByUsername(String username) {
		return findByUsername(username)
				.orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
	}

	public User save(User user) {
		return users.put(user.getId(), user);
	}

	public Optional<User> find(String id) {
		return ofNullable(users.get(id));
	}

	private Optional<User> findByUsername(String username) {
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
