package it.eng.idsa.businesslogic.usagecontrol.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import it.eng.idsa.businesslogic.usagecontrol.model.UsageControlObjectToEnforce;
import it.eng.idsa.businesslogic.usagecontrol.service.UsageControlService;

@Service
@ConditionalOnExpression("'${application.isEnabledUsageControl}' == 'true' && '${application.usageControlVersion}'=='platoon'")
public class PlatoonUsageControlServiceImpl implements UsageControlService {

	private static final Logger logger = LoggerFactory.getLogger(PlatoonUsageControlServiceImpl.class);

	private String platoonURL = "https://localhost/platoontec/PlatoonDataUsage/1.0/enforce/usage/use";

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
				.append("?targetDataUri=")
				.append(targetArtifact)
				.append("&providerUri=")
				.append(provider)
				.append("&consumerUri=")
				.append(consumer)
				.append("&consuming=true");

		String objectToEnforceAsJsonStr = webClient.post().uri(ucUrl.toString()).contentType(MediaType.APPLICATION_JSON)
				.bodyValue(ucObj.getPayload()).retrieve().bodyToMono(String.class).block();

		return objectToEnforceAsJsonStr;
	}

}
