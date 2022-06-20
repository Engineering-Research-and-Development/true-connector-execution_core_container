package it.eng.idsa.businesslogic.usagecontrol.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import it.eng.idsa.businesslogic.usagecontrol.exception.PolicyDeniedException;
import it.eng.idsa.businesslogic.usagecontrol.model.IdsMsgTarget;
import it.eng.idsa.businesslogic.usagecontrol.model.IdsUseObject;
import it.eng.idsa.businesslogic.usagecontrol.model.Meta;
import it.eng.idsa.businesslogic.usagecontrol.model.TargetArtifact;
import it.eng.idsa.businesslogic.usagecontrol.model.UsageControlObject;
import it.eng.idsa.businesslogic.usagecontrol.model.UsageControlObjectToEnforce;
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

	@Autowired(required = false)
	private Gson gson;

	@Autowired
	public MyDataUsageControlServiceImpl(UcRestCallService ucRestCallService) {
		this.ucRestCallService = ucRestCallService;
	}

	@Override
	public String enforceUsageControl(JsonElement ucObject) throws Exception {
		UsageControlObjectToEnforce ucObj = gson.fromJson(ucObject, UsageControlObjectToEnforce.class);

		logger.info("Proceeding with Usage control enforcement");
		String provider = ucObj.getAssigner().toString();
		String consumer = ucObj.getAssignee().toString();
		String targetArtifact = ucObj.getTargetArtifactId().toString();
		logger.info("Provider:" + provider);
		logger.info("Consumer:" + consumer);
		logger.info("payload:" + ucObj.getPayload());
		logger.info("artifactID:" + targetArtifact);
		
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
//			ucObj.setPayload(jsonElement);
			logger.debug("Result from Usage Control: " + jsonElement.toString());
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

}
