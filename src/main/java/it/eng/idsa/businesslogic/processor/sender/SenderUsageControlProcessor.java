package it.eng.idsa.businesslogic.processor.sender;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.usagecontrol.service.UsageControlService;
import it.eng.idsa.businesslogic.util.HeaderCleaner;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;

/**
 * @author Antonio Scatoloni and Gabriele De Luca
 */
@Component
public class SenderUsageControlProcessor implements Processor {
	private static final Logger logger = LoggerFactory.getLogger(SenderUsageControlProcessor.class);

	@Value("#{new Boolean('${application.isEnabledUsageControl}')}")
	private boolean isEnabledUsageControl;
	
	@Autowired(required = false)
	private UsageControlService usageControlService;

	@Autowired
	private RejectionMessageService rejectionMessageService;

	@Value("${application.dataApp.websocket.isEnabled}")
	private boolean isEnabledWebSocket;

	@Value("${application.eccHttpSendRouter}")
	private String eccHttpSendRouter;

	@Value("${application.openDataAppReceiverRouter}")
	private String openDataAppReceiverRouter;
	
	@Autowired(required = false)
	private Gson gson;

	@Autowired
	private HeaderCleaner headerCleaner;

	@Override
	public void process(Exchange exchange) {
		if (!isEnabledUsageControl) {
			logger.info("Usage control not configured - continued with flow");
			return;
		}
		Message responseMessage = null;
		String payload = null;
		MultipartMessage multipartMessageResponse = null;
		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);
		payload = multipartMessage.getPayloadContent();
		responseMessage = multipartMessage.getHeaderContent();

		//TODO should we check if request instanceof ArtifactRequestMessage?
		if (!(responseMessage instanceof ArtifactResponseMessage)) {
			logger.info("Usage Control not applied - not ArtifactResponseMessage");
			return;
		}

		logger.info("Proceeding with Usage control enforcement");
		logger.info("from: " + exchange.getFromEndpoint());
		logger.debug("Message Body: " + payload);

		try {

			JsonElement transferedDataObject = getDataObject(payload);
			
			String objectToEnforce = usageControlService.enforceUsageControl(transferedDataObject);
			
			multipartMessageResponse = new MultipartMessageBuilder()
					.withHeaderContent(responseMessage)
					.withPayloadContent(objectToEnforce)
					.build();
			
			headerCleaner.removeTechnicalHeaders(exchange.getMessage().getHeaders());
			exchange.getMessage().setBody(multipartMessageResponse);
			exchange.getMessage().setHeaders(exchange.getMessage().getHeaders());
			logger.info("Usage control policy enforcementd - completed");

		} catch (Exception e) {
			logger.error("Usage Control Enforcement has failed with MESSAGE: {}", e.getMessage());
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_USAGE_CONTROL, responseMessage);
		}
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

}
