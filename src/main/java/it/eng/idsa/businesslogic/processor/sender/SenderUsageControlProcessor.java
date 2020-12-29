package it.eng.idsa.businesslogic.processor.sender;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.converter.stream.CachedOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedTreeMap;

import de.fraunhofer.dataspaces.iese.camel.interceptor.model.IdsMsgTarget;
import de.fraunhofer.dataspaces.iese.camel.interceptor.model.IdsUseObject;
import de.fraunhofer.dataspaces.iese.camel.interceptor.model.UsageControlObject;
import de.fraunhofer.dataspaces.iese.camel.interceptor.service.UcService;
import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.WebSocketServerConfigurationA;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverUsageControlProcessor;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.HeaderCleaner;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;

/**
 * @author Antonio Scatoloni and Gabriele De Luca
 */
@ComponentScan("de.fraunhofer.dataspaces.iese")
@Component
public class SenderUsageControlProcessor implements Processor {
    private Gson gson;
    private static final Logger logger = LoggerFactory.getLogger(SenderUsageControlProcessor.class);

    @Value("${application.isEnabledUsageControl:false}")
    private boolean isEnabledUsageControl;

    @Autowired
    private UcService ucService;

    @Autowired
    private RejectionMessageService rejectionMessageService;

    @Value("${application.dataApp.websocket.isEnabled}")
    private boolean isEnabledWebSocket;

    @Autowired(required = false)
    WebSocketServerConfigurationA webSocketServerConfiguration;
    
	@Value("${application.eccHttpSendRouter}")
	private String eccHttpSendRouter;

	@Value("${application.openDataAppReceiverRouter}")
	private String openDataAppReceiverRouter;
	
	@Autowired
	private HeaderCleaner headerCleaner;

    public SenderUsageControlProcessor() {
        gson = ReceiverUsageControlProcessor.createGson();
    }

    @Override
    public void process(Exchange exchange) {
        if (!isEnabledUsageControl) {
            logger.info("Usage control not configured - continued with flow");
            return;
        }
        Message message = null;
        String header = null;
        String payload = null;
        MultipartMessage multipartMessageResponse=null;
        try {
			MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);
			header = multipartMessage.getHeaderContentString();
			payload = multipartMessage.getPayloadContent();
			message = multipartMessage.getHeaderContent();
			logger.info("from: " + exchange.getFromEndpoint());
            logger.debug("Message Body: " + payload);

            JsonElement transferedDataObject = getDataObject(payload);
            UsageControlObject ucObj = gson.fromJson(transferedDataObject, UsageControlObject.class);
            boolean isUsageControlObject = true;

            if (isUsageControlObject
                    && null != ucObj
                    && null != ucObj.getMeta()
                    && null != ucObj.getPayload()) {
                String targetArtifactId = ucObj.getMeta().getTargetArtifact().getId().toString();
                IdsMsgTarget idsMsgTarget = getIdsMsgTarget();
                if (null != ucObj.getPayload() && !(CachedOutputStream.class.equals(ucObj.getPayload().getClass().getEnclosingClass()))) {
                    logger.debug("Message Body In: " + ucObj.getPayload().toString());

                    IdsUseObject idsUseObject = new IdsUseObject();
                    idsUseObject.setTargetDataUri(targetArtifactId);
                    idsUseObject.setMsgTarget(idsMsgTarget);
                    idsUseObject.setDataObject(ucObj.getPayload());

                    Object result = ucService.enforceUsageControl(idsUseObject);
                    if (result instanceof LinkedTreeMap<?, ?>) {
                        final LinkedTreeMap<?, ?> treeMap = (LinkedTreeMap<?, ?>) result;
                        final JsonElement jsonElement = gson.toJsonTree(treeMap);
                        ucObj.setPayload(jsonElement);
                        logger.debug("Result from Usage Control: " + jsonElement.toString());
                    } else if (null == result || StringUtils.isEmpty(result.toString())) {
                        throw new Exception("Usage Control Enforcement with EMPTY RESULT encountered.");
                    }
                    // Prepare Response
                     multipartMessageResponse = new MultipartMessageBuilder()
                            .withHeaderContent(header)
                            .withPayloadContent(extractPayloadFromJson(ucObj.getPayload()))
                            .build();
                }
            }
            else {
            	logger.info("Usage Control not applied - not ArtifactRequestMessage/ArtifactResponseMessage");
            	multipartMessageResponse = multipartMessage;
            	
            }
            headerCleaner.removeTechnicalHeaders(exchange.getMessage().getHeaders());
            exchange.getMessage().setBody(multipartMessageResponse);
            exchange.getMessage().setHeaders(exchange.getMessage().getHeaders());
    		logger.info("Sending response to DataApp");

        } catch (Exception e) {
            logger.error("Usage Control Enforcement has failed with MESSAGE: {}", e.getMessage());
            rejectionMessageService.sendRejectionMessage(
                    RejectionMessageType.REJECTION_USAGE_CONTROL,
                    message);
        }
    }
    
    /**
     * Used for purpose PIP
     * 	@ActionDescription(methodName = "purpose")
  		public String purpose(
      		@ActionParameterDescription(name = "MsgTargetAppUri", mandatory = true) String msgTargetAppUri) 
      		IdsMsgTarget.appUri is translated to msgTargetAppUri
     * @return
     */
    public static IdsMsgTarget getIdsMsgTarget() {
        IdsMsgTarget idsMsgTarget = new IdsMsgTarget();
        idsMsgTarget.setName("Anwendung A");
        // idsMsgTarget.setAppUri(target.toString());
        idsMsgTarget.setAppUri("http://ziel-app");
        return idsMsgTarget;
    }

    private JsonElement getDataObject(String s) {
        JsonElement obj = null;
        try {
            JsonElement jsonElement = gson.fromJson(s, JsonElement.class);
            if (null != jsonElement && !(jsonElement.isJsonArray() && jsonElement.getAsJsonArray().size() == 0)) {
                obj = jsonElement;
            }
        } catch (JsonSyntaxException jse) {
            obj = null;
        }
        return obj;
    }

    private String extractPayloadFromJson(JsonElement payload) {
        final JsonObject asJsonObject = payload.getAsJsonObject();
        JsonElement payloadInner = asJsonObject.get("payload");
        if (null != payloadInner) return payloadInner.getAsString();
        return payload.toString();
    }
}
