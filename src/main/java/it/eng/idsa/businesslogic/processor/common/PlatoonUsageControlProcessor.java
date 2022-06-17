package it.eng.idsa.businesslogic.processor.common;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class PlatoonUsageControlProcessor implements Processor {

	@Autowired
	private WebClient webClient;
	
	private String platoonURL = "https://localhost/platoontec/PlatoonDataUsage/1.0/enforce/usage/use";

	@Override
	public void process(Exchange exchange) throws Exception {
		
		String provider = "https://provider.com";
		
		String consumer = "https://consumer.com";
		
		String targetArtifact = "http://w3id.org/engrd/connector/artifact/1";
		
		StringBuffer ucUrl = new StringBuffer()
				.append(platoonURL)
				.append("?targetDataUri=")
				.append(targetArtifact)
				.append("&providerUri=")
				.append(provider)
				.append("&consumerUri=")
				.append(consumer)
				.append("&consuming=true");
		
		String resp = webClient.post()
				.uri(ucUrl.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("david")
				.retrieve()
				.bodyToMono(String.class)
				.block();
		
		System.out.println(resp);
		exchange.getMessage().setBody(resp);
	}

}
