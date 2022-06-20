package it.eng.idsa.businesslogic.usagecontrol.service.impl;

import java.io.IOException;

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
import com.google.gson.internal.LinkedTreeMap;

import it.eng.idsa.businesslogic.usagecontrol.exception.PolicyDeniedException;
import it.eng.idsa.businesslogic.usagecontrol.model.IdsMsgTarget;
import it.eng.idsa.businesslogic.usagecontrol.model.IdsUseObject;
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

	@Autowired(required = false)
	private Gson gson;

	@Autowired
	public MyDataUsageControlServiceImpl(UcRestCallService ucRestCallService) {
		this.ucRestCallService = ucRestCallService;
	}

	@Override
	public String enforceUsageControl(JsonElement ucObject) throws Exception {
		UsageControlObject ucObj = gson.fromJson(ucObject, UsageControlObject.class);

		String targetArtifactId = ucObj.getMeta().getTargetArtifact().getId().toString();
		IdsMsgTarget idsMsgTarget = getIdsMsgTarget();
		logger.debug("Message Body In: " + ucObj.getPayload().toString());

		IdsUseObject idsUseObject = new IdsUseObject();
		idsUseObject.setTargetDataUri(targetArtifactId);
		idsUseObject.setMsgTarget(idsMsgTarget);
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

		if (result instanceof LinkedTreeMap<?, ?>) {
			final LinkedTreeMap<?, ?> treeMap = (LinkedTreeMap<?, ?>) result;
			final JsonElement jsonElement = gson.toJsonTree(treeMap);
			ucObj.setPayload(jsonElement);
			logger.debug("Result from Usage Control: " + jsonElement.toString());
		} else if (null == result || StringUtils.isEmpty(result.toString())) {
			throw new Exception("Usage Control Enforcement with EMPTY RESULT encountered.");
		}

		return extractPayloadFromJson(ucObj.getPayload());
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

}
