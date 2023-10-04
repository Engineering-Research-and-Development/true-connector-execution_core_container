package it.eng.idsa.businesslogic.usagecontrol.service.impl;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedTreeMap;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import it.eng.idsa.businesslogic.service.CommunicationService;
import it.eng.idsa.businesslogic.usagecontrol.exception.PolicyDeniedException;
import it.eng.idsa.businesslogic.usagecontrol.model.IdsMsgTarget;
import it.eng.idsa.businesslogic.usagecontrol.model.IdsUseObject;
import it.eng.idsa.businesslogic.usagecontrol.model.Meta;
import it.eng.idsa.businesslogic.usagecontrol.model.TargetArtifact;
import it.eng.idsa.businesslogic.usagecontrol.model.UsageControlObject;
import it.eng.idsa.businesslogic.usagecontrol.service.UcRestCallService;
import it.eng.idsa.businesslogic.usagecontrol.service.UsageControlService;
import it.eng.idsa.businesslogic.util.MessagePart;
import retrofit2.Call;
import retrofit2.Response;

@ComponentScan("de.fraunhofer.dataspaces.iese")
@Service
@ConditionalOnExpression("'${application.isEnabledUsageControl}' == 'true' && '${application.usageControlVersion}'=='mydata'")
public class MyDataUsageControlServiceImpl implements UsageControlService {

	private static final Logger logger = LoggerFactory.getLogger(MyDataUsageControlServiceImpl.class);

	private UcRestCallService ucRestCallService;
	
	private CommunicationService communicationService;
	
	private String usageControlDataAppURL;
	
	private String policyEndpoint = "policy/usage/odrl";
	
	@Value("${application.isEnabledUsageControl}") 
	private boolean isEnabledUsageControl;

	private Gson gson;

	public MyDataUsageControlServiceImpl(UcRestCallService ucRestCallService, 
			CommunicationService communicationService,
			@Value("${spring.ids.ucapp.baseUrl}") String usageControlDataAppURL,
			Gson gson) {
		super();
		this.ucRestCallService = ucRestCallService;
		this.communicationService = communicationService;
		this.usageControlDataAppURL = usageControlDataAppURL;
		this.gson = gson;
	}


	@Override
	public String enforceUsageControl(URI contractAgreementUri, URI requestedArtifact, String payload) throws Exception {
		JsonElement transferedDataObject = getDataObject(payload);
		UsageControlObject ucObj = gson.fromJson(transferedDataObject, UsageControlObject.class);

		logger.info("Proceeding with Usage control enforcement");
		String targetArtifact = ucObj.getMeta().getTargetArtifact().getId().toString();
		
		IdsUseObject idsUseObject = new IdsUseObject();
		idsUseObject.setTargetDataUri(targetArtifact);
		// Is it needed?? IdsMsgTarget
		idsUseObject.setMsgTarget(getIdsMsgTarget());
		idsUseObject.setDataObject(ucObj.getPayload());

		Object result = null;
		try {
			Call<Object> callSync = ucRestCallService.enforceUsageControl(idsUseObject);
			Response<Object> response = callSync.execute();
			if (!response.isSuccessful()) {
				throw new PolicyDeniedException(response);
			}
			result = response.body();
		} catch (IOException ioe) {
			logger.error("Uc Service has failed: {}", ioe);
			result = "";
		}

		JsonElement jsonElement = null;
		if (result instanceof LinkedTreeMap<?, ?>) {
			final LinkedTreeMap<?, ?> treeMap = (LinkedTreeMap<?, ?>) result;
			jsonElement = gson.toJsonTree(treeMap);
		} else if (null == result || StringUtils.isEmpty(result.toString())) {
			throw new Exception("Usage Control Enforcement with EMPTY RESULT encountered.");
		}

		return extractPayloadFromJson(jsonElement);
	}

	/**
	 * Used for purpose PIP
	 * 
	 * @ActionDescription(methodName = "purpose") public String purpose(
	 * @ActionParameterDescription(name = "MsgTargetAppUri", mandatory = true)
	 *                                  String msgTargetAppUri) IdsMsgTarget.appUri
	 *                                  is translated to msgTargetAppUri
	 * @return
	 */
	private IdsMsgTarget getIdsMsgTarget() {
		IdsMsgTarget idsMsgTarget = new IdsMsgTarget();
		idsMsgTarget.setName("Anwendung A");
		// idsMsgTarget.setAppUri(target.toString());
		idsMsgTarget.setAppUri("http://ziel-app");
		return idsMsgTarget;
	}

	private String extractPayloadFromJson(JsonElement payload) {
		final JsonObject asJsonObject = payload.getAsJsonObject();
		JsonElement payloadInner = asJsonObject.get(MessagePart.PAYLOAD);
		if (null != payloadInner)
			return payloadInner.getAsString();
		return payload.toString();
	}

	@Override
	public String createUsageControlObject(ArtifactRequestMessage artifactRequestMessage,
			ArtifactResponseMessage artifactResponseMessage, String payloadContent) {
		UsageControlObject usageControlObject = new UsageControlObject();
        JsonElement jsonElement = gson.fromJson(createJsonPayload(payloadContent), JsonElement.class);
        usageControlObject.setPayload(jsonElement);
        Meta meta = new Meta();
        meta.setAssignee(artifactRequestMessage.getIssuerConnector());
        meta.setAssigner(artifactResponseMessage.getIssuerConnector());
        TargetArtifact targetArtifact = new TargetArtifact();
        LocalDateTime localDateTime = LocalDateTime.now();
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("CET"));
        targetArtifact.setCreationDate(zonedDateTime);
        targetArtifact.setId(artifactRequestMessage.getRequestedArtifact());
        meta.setTargetArtifact(targetArtifact);
        usageControlObject.setMeta(meta);
        String usageControlObjectPayload = gson.toJson(usageControlObject, UsageControlObject.class);
        return usageControlObjectPayload;
	}
	
	private String createJsonPayload(String payload) {
        boolean isJson = true;
        try {
            JsonParser.parseString(payload);
        } catch (JsonSyntaxException e) {
            isJson = false;
        }
        if (!isJson) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("{");
            stringBuilder.append("\"payload\":" + "\"" + payload + "\"");
            stringBuilder.append("}");
            return stringBuilder.toString();
        }
        return payload;
    }

	@Override
	public String uploadPolicy(String payloadContent) {
		String ucDataAppAddPolicyEndpoint = usageControlDataAppURL + policyEndpoint;
		logger.info("ContractAgreementMessage detected, sending payload to Usage Contol DataApp at '{}'", ucDataAppAddPolicyEndpoint);
		return communicationService.sendDataAsJson(ucDataAppAddPolicyEndpoint, payloadContent, "application/ld+json;charset=UTF-8");
	}
	
	private JsonElement getDataObject(String s) {
		JsonElement obj = null;
		try {
			JsonElement jsonElement = gson.fromJson(s, JsonElement.class);
			if (null != jsonElement && !(jsonElement.isJsonArray() && jsonElement.getAsJsonArray().size() == 0)) {
				obj = jsonElement;
			}
		} catch (JsonSyntaxException jse) {
			logger.error("Usage control object is not JSON");
			obj = null;
		}
		return obj;
	}


	@Override
	public void rollbackPolicyUpload(String contractAgreementUUID) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean isUsageControlAvailable(String usageContolHealthEndpoint) {
		if (isEnabledUsageControl) {
			return communicationService.getRequest(usageContolHealthEndpoint) != null ? true : false;
		}
		return true;
	}
}
