package it.eng.idsa.businesslogic.usagecontrol.service.impl;

import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.ContractAgreement;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.businesslogic.service.CommunicationService;
import it.eng.idsa.businesslogic.usagecontrol.service.UsageControlService;
import it.eng.idsa.businesslogic.util.Helper;

@Service
@ConditionalOnExpression("'${application.isEnabledUsageControl}' == 'true' && '${application.usageControlVersion}'=='platoon'")
public class PlatoonUsageControlServiceImpl implements UsageControlService {

	private static final Logger logger = LoggerFactory.getLogger(PlatoonUsageControlServiceImpl.class);

	@Value("${spring.ids.ucapp.baseUrl}")
	private String platoonURL;
	
	@Value("${application.isEnabledUsageControl}") 
	private boolean isEnabledUsageControl;
	
	private String policyEnforcementEndpoint = "enforce/usage/agreement";
	
	private String policyUploadEndpoint = "contractAgreement/";
	
	private final String CONTENT_TYPE = "application/json;charset=UTF-8";
	
	@Autowired
	private CommunicationService communicationService;
	
	@Override
	public String enforceUsageControl(URI uri, String ucObject) throws IOException {
		
		logger.info("enforcing contract agreement:" + uri.toString());
		
		StringBuffer ucUrl = new StringBuffer().append(platoonURL)
				.append(policyEnforcementEndpoint)
				.append("?contractAgreementUri=")
				.append(uri)
				.append("&consuming=true");
		
		
		return communicationService.sendDataAsJson(ucUrl.toString(), ucObject, CONTENT_TYPE);
	}

	@Override
	public String createUsageControlObject(ArtifactRequestMessage artifactRequestMessage,
			ArtifactResponseMessage artifactResponseMessage, String payloadContent) {
		// Nothing to do, just return payload, no need for meta-data wrapping
		return payloadContent;
	}

	@Override
	public String uploadPolicy(String payloadContent) {
		String ucUrl = platoonURL + policyUploadEndpoint;

		return communicationService.sendDataAsJson(ucUrl, payloadContent, CONTENT_TYPE);
	}

	@Override
	public void rollbackPolicyUpload(String contractAgreement) {
		ContractAgreement ca = null;
		try {
			ca = new Serializer().deserialize(contractAgreement, ContractAgreement.class);
		} catch (Exception e) {
			logger.error("Policy upload rollback interupted - {}", e.getMessage());
		}
		if(isEnabledUsageControl && ca != null) {
			logger.info("Rolling back policy upload");
			communicationService.deleteRequest(platoonURL + policyUploadEndpoint + Helper.getUUID(ca.getId()));
		}
	}
	
	@Override
	public boolean isUsageControlAvailable(String usageContolHealthEndpoint) {
		if(isEnabledUsageControl) {
			 return communicationService.getRequest(usageContolHealthEndpoint) != null ? true : false;
		}
		return true;
	}
}
