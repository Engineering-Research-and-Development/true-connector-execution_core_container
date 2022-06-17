package it.eng.idsa.businesslogic.configuration;


import javax.net.ssl.SSLException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.UnAuthenticatedServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfiguration {
	
	@Value("${spring.security.oauth2.client.provider.platoon.token-uri}")
	private String accessTokenUri;
	
	@Value("${spring.security.oauth2.client.registration.platoon.client-id}")
	private String clientId;
	
	@Value("${spring.security.oauth2.client.registration.platoon.client-secret}")
	private String clientSecret;
	
	@Bean
	WebClient webClient(ReactiveClientRegistrationRepository clientRegistrations) throws SSLException {
		ServerOAuth2AuthorizedClientExchangeFilterFunction oauth = new ServerOAuth2AuthorizedClientExchangeFilterFunction(
				clientRegistrations, new UnAuthenticatedServerOAuth2AuthorizedClientRepository());
		oauth.setDefaultClientRegistrationId("platoon");
		
		SslContext sslContext = SslContextBuilder.forClient()
				.trustManager(InsecureTrustManagerFactory.INSTANCE)
				.build();
		
		return WebClient.builder()
				.clientConnector(
						new ReactorClientHttpConnector(HttpClient.create()
								.secure(t -> t.sslContext(sslContext))))
				.filter(oauth)
				.build();
	}
	
	@Bean
	ReactiveClientRegistrationRepository reactiveClientRegistrationRepository () {
		var registration = ClientRegistration
                .withRegistrationId("platoon")
                .tokenUri(accessTokenUri)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();
        var repo = new InMemoryReactiveClientRegistrationRepository(registration);
		return repo;
	}

}
