package it.eng.idsa.businesslogic.processor.sender;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.audit.TrueConnectorEvent;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.usagecontrol.service.UsageControlService;
import it.eng.idsa.businesslogic.util.HeaderCleaner;
import it.eng.idsa.businesslogic.util.TrueConnectorConstants;
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

	@Autowired
	private HeaderCleaner headerCleaner;

	@Autowired
	private ApplicationEventPublisher publisher;

	@Override
	public void process(Exchange exchange) {
		if (!isEnabledUsageControl) {
			logger.info("Usage control not configured - continued with flow");
			return;
		}

		
		ArtifactRequestMessage requestMessage = exchange.getProperty("Original-Message-Header",
				ArtifactRequestMessage.class);
		Message responseMessage = null;
		String payload = null;
		MultipartMessage multipartMessageResponse = null;
		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);
		payload = multipartMessage.getPayloadContent();
		responseMessage = multipartMessage.getHeaderContent();
		String correlationId = (String) exchange.getMessage().getHeader(TrueConnectorConstants.CORRELATION_ID);

		// TODO should we check if request instanceof ArtifactRequestMessage?
		if (!(responseMessage instanceof ArtifactResponseMessage)) {
			logger.info("Usage Control not applied - not ArtifactResponseMessage");
			return;
		}

		logger.info("Proceeding with Usage control enforcement");

		try {

			String objectToEnforce = usageControlService.enforceUsageControl(responseMessage.getTransferContract(),
					requestMessage.getRequestedArtifact(), payload);

			multipartMessageResponse = new MultipartMessageBuilder().withHeaderContent(responseMessage)
					.withPayloadContent(objectToEnforce).build();

			headerCleaner.removeTechnicalHeaders(exchange.getMessage().getHeaders());
			exchange.getMessage().setBody(multipartMessageResponse);
			exchange.getMessage().setHeaders(exchange.getMessage().getHeaders());
			logger.info("Usage control policy enforcementd - completed");
			publisher.publishEvent(new TrueConnectorEvent(TrueConnectorEventType.CONNECTOR_POLICY_ENFORCEMENT_SUCCESS,
					multipartMessage, correlationId));

		} catch (Exception e) {
			logger.error("Usage Control Enforcement has failed with MESSAGE: {}", e.getMessage());
			publisher.publishEvent(new TrueConnectorEvent(TrueConnectorEventType.CONNECTOR_POLICY_ENFORCEMENT_FAILED,
					multipartMessage, correlationId));
			rejectionMessageService.sendRejectionMessage((Message) exchange.getProperty("Original-Message-Header"),
					RejectionReason.NOT_AUTHORIZED);
		}
	}
}
