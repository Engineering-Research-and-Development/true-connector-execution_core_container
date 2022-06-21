package it.eng.idsa.businesslogic.usagecontrol.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import it.eng.idsa.businesslogic.usagecontrol.model.UsageControlObjectToEnforce;
import it.eng.idsa.businesslogic.usagecontrol.service.UsageControlService;
import reactor.core.publisher.Mono;

@Service
@ConditionalOnExpression("'${application.isEnabledUsageControl}' == 'true' && '${application.usageControlVersion}'=='platoon'")
public class PlatoonUsageControlServiceImpl implements UsageControlService {

	private static final Logger logger = LoggerFactory.getLogger(PlatoonUsageControlServiceImpl.class);

	@Value("${spring.ids.ucapp.baseUrl}")
	private String platoonURL;
	
	private String policyEnforcementEndpoint = "enforce/usage/use";
	
	private String policyUploadEndpoint = "contractAgreement";
	
	@Autowired(required = false)
	private WebClient webClient;

	@Autowired(required = false)
	private Gson gson;

	@Override
	public String enforceUsageControl(JsonElement ucObject) {
		UsageControlObjectToEnforce ucObj = gson.fromJson(ucObject, UsageControlObjectToEnforce.class);

		logger.info("Proceeding with Usage control enforcement");
		String provider = ucObj.getAssigner().toString();
		String consumer = ucObj.getAssignee().toString();
		String targetArtifact = ucObj.getTargetArtifactId().toString();
		logger.info("Provider:" + provider);
		logger.info("Consumer:" + consumer);
		logger.info("payload:" + ucObj.getPayload());
		logger.info("artifactID:" + targetArtifact);

		StringBuffer ucUrl = new StringBuffer().append(platoonURL)
				.append(policyEnforcementEndpoint)
				.append("?targetDataUri=")
				.append(targetArtifact)
				.append("&providerUri=")
				.append(provider)
				.append("&consumerUri=")
				.append(consumer)
				.append("&consuming=true");

		String objectToEnforceAsJsonStr = webClient.post()
				.uri(ucUrl.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(ucObj.getPayload())
				.retrieve()
				.bodyToMono(String.class)
				.block();

		return objectToEnforceAsJsonStr;
	}

	@Override
	public String createUsageControlObject(ArtifactRequestMessage artifactRequestMessage,
			ArtifactResponseMessage artifactResponseMessage, String payloadContent) {
		UsageControlObjectToEnforce usageControlObject = new UsageControlObjectToEnforce();
        usageControlObject.setPayload(payloadContent);
        usageControlObject.setAssignee(artifactRequestMessage.getIssuerConnector());
        usageControlObject.setAssigner(artifactResponseMessage.getIssuerConnector());
        usageControlObject.setTargetArtifactId(artifactRequestMessage.getRequestedArtifact());
                    
        String usageControlObjectPayload = gson.toJson(usageControlObject, UsageControlObjectToEnforce.class);
                
        return usageControlObjectPayload;
	}

	@Override
	public String uploadPolicy(String payloadContent) {
		String ucUrl = platoonURL + policyUploadEndpoint;

		Mono<String> s = webClient.post()
				.uri(ucUrl)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(payloadContent)
		   .retrieve()
		   .bodyToMono(String.class)
		   .onErrorResume(WebClientResponseException.class,
		        ex -> ex.getRawStatusCode() == 400 ? Mono.empty() : Mono.error(ex));
		
		return s.block();
		
	}

}
