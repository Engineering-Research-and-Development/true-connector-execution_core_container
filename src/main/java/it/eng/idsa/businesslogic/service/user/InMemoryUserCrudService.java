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
import it.eng.idsa.businesslogic.util.TrueConnectorConstants;

@Service
@DependsOn({ "encoder" })
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
		userConfiguration.getUserCredentials().keySet().forEach(user -> {
			users.put(user, new User(UUID.randomUUID().toString(), user, userConfiguration.getPasswordForUser(user),
					TrueConnectorConstants.API_USER_ROLE));
		});
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		if (loginAttemptService.isBlocked(username)) {
			logger.info("User '{}' is blocked!", username);
			publisher.publishEvent(new TrueConnectorEvent(request, username, TrueConnectorEventType.USER_BLOCKED));
			throw new RuntimeException("blocked");
		}
		return findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
	}

	@Override
	public UserDetails loadCamelUserByUsername(String username) {
		return findByUsername(username).orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
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

		if (loginAttemptService.isBlocked(username)) {
			logger.info("User '{}' is blocked!", username);
			throw new RuntimeException("blocked");
		}
		return users.values().stream()
				.filter(u -> Objects.equals(username, u.getUsername()) && (encoder.matches(password, u.getPassword())))
				.findFirst();
	}
}
