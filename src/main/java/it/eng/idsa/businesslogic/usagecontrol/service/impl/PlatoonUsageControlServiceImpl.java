package it.eng.idsa.businesslogic.usagecontrol.service.impl;

import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import it.eng.idsa.businesslogic.service.CommunicationService;
import it.eng.idsa.businesslogic.usagecontrol.model.UsageControlObjectToEnforce;
import it.eng.idsa.businesslogic.usagecontrol.service.UsageControlService;

@Service
@ConditionalOnExpression("'${application.isEnabledUsageControl}' == 'true' && '${application.usageControlVersion}'=='platoon'")
public class PlatoonUsageControlServiceImpl implements UsageControlService {

	private static final Logger logger = LoggerFactory.getLogger(PlatoonUsageControlServiceImpl.class);

	@Value("${spring.ids.ucapp.baseUrl}")
	private String platoonURL;
	
	private String policyEnforcementEndpoint = "enforce/usage/agreement";
	
	private String policyUploadEndpoint = "contractAgreement";
	
	@Autowired
	private CommunicationService communicationService;
	
	@Autowired(required = false)
	private Gson gson;

	@Override
	public String enforceUsageControl(URI uri, String ucObject) throws IOException {
		
		logger.info("enforcing contract agreement:" + uri.toString());
		
		StringBuffer ucUrl = new StringBuffer().append(platoonURL)
				.append(policyEnforcementEndpoint)
				.append("?contractAgreementUri=")
				.append(uri)
				.append("&consuming=true");
		
		
		return communicationService.sendDataAsJson(ucUrl.toString(), ucObject);
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

		return communicationService.sendDataAsJson(ucUrl, payloadContent);
	}

}
