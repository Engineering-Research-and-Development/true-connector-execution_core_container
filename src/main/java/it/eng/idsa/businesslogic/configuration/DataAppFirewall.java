package it.eng.idsa.businesslogic.configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.util.CollectionUtils;

@Configuration
@PropertySource("classpath:firewall.properties")
public class DataAppFirewall {

	@Value("${allowedMethods}")
	private List<String> allowedMethods;
	@Value("${allowBackSlash}")
	private Boolean allowBackSlash;
	@Value("${allowUrlEncodedSlash}")
	private boolean allowUrlEncodedSlash;
	@Value("${allowUrlEncodedDoubleSlash}")
	private boolean allowUrlEncodedDoubleSlash;
	@Value("${allowSemicolon}")
	private boolean allowSemicolon;
	@Value("${allowUrlEncodedPercent}")
	private boolean allowUrlEncodedPercent;
	@Value("${allowUrlEncodedPeriod}")
	private boolean allowUrlEncodedPeriod;

	@Bean
	@ConditionalOnProperty(name = "application.firewall.isEnabled", havingValue = "true", matchIfMissing = false)
	public StrictHttpFirewall httpFirewall() {
		StrictHttpFirewall firewall = new StrictHttpFirewall();
		if (!CollectionUtils.isEmpty(allowedMethods)) {
			firewall.setAllowedHttpMethods(allowedMethods);
		}
		firewall.setAllowBackSlash(allowBackSlash);
		firewall.setAllowUrlEncodedSlash(allowUrlEncodedSlash);
		firewall.setAllowUrlEncodedDoubleSlash(allowUrlEncodedDoubleSlash);
		firewall.setAllowSemicolon(allowSemicolon);
		firewall.setAllowUrlEncodedPercent(allowUrlEncodedPercent);
		firewall.setAllowUrlEncodedPeriod(allowUrlEncodedPeriod);
		return firewall;
	}
}
