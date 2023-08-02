package it.eng.idsa.businesslogic.processor.receiver;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.audit.TrueConnectorEvent;
import it.eng.idsa.businesslogic.audit.TrueConnectorEventType;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.usagecontrol.service.UsageControlService;
import it.eng.idsa.businesslogic.util.TrueConnectorConstants;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;


/**
 * @author Antonio Scatoloni and Gabriele De Luca
 */
@ComponentScan("de.fraunhofer.dataspaces.iese")
@Component
public class ReceiverUsageControlProcessor implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(ReceiverUsageControlProcessor.class);

    private Message requestMessage;
    private Message responseMessage;

    @Value("#{new Boolean('${application.isEnabledUsageControl}')}")
    private boolean isEnabledUsageControl;

    @Autowired
    private RejectionMessageService rejectionMessageService;

    @Autowired(required = false)
	private UsageControlService usageControlService;
    
    @Autowired
	private ApplicationEventPublisher publisher;

    @Override
    public void process(Exchange exchange) {
        if (!isEnabledUsageControl) {
            logger.info("Usage control not configured - continued with flow");
            return;
        }
        
		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);
		requestMessage = (Message) exchange.getProperty("Original-Message-Header");
		responseMessage = multipartMessage.getHeaderContent();
		String correlationId = (String) exchange.getMessage().getHeader(TrueConnectorConstants.CORRELATION_ID);
		
		if (!(requestMessage instanceof ArtifactRequestMessage) || !(responseMessage instanceof ArtifactResponseMessage)) {
			logger.info("Usage Control not applied - not ArtifactRequestMessage/ArtifactResponseMessage");
			return;
		}

		try {
			ArtifactRequestMessage artifactRequestMessage = (ArtifactRequestMessage) requestMessage;
			ArtifactResponseMessage artifactResponseMessage = (ArtifactResponseMessage) responseMessage;
			logger.info("Proceeding with Usage control enforcement");
			
			String payloadToEnforce = usageControlService.createUsageControlObject(artifactRequestMessage, artifactResponseMessage,
			        multipartMessage.getPayloadContent());
			
			MultipartMessage reponseMultipartMessage = new MultipartMessageBuilder()
					.withHttpHeader(multipartMessage.getHttpHeaders())
					.withHeaderHeader(multipartMessage.getHeaderHeader())
					.withHeaderContent(responseMessage)
					.withPayloadHeader(multipartMessage.getPayloadHeader())
					.withPayloadContent(payloadToEnforce)
					.withToken(multipartMessage.getToken())
					.build();
			
			exchange.getMessage().setBody(reponseMultipartMessage);
			publisher.publishEvent(new TrueConnectorEvent(TrueConnectorEventType.CONNECTOR_POLICY_ENFORCEMENT_SUCCESS, 
					multipartMessage, correlationId));
		} catch (Exception e) {
			logger.error("Usage Control Enforcement has failed with MESSAGE: {}", e.getMessage());
			publisher.publishEvent(new TrueConnectorEvent(TrueConnectorEventType.CONNECTOR_POLICY_ENFORCEMENT_FAILED, 
					multipartMessage, correlationId));
			rejectionMessageService.sendRejectionMessage((Message) exchange.getProperty("Original-Message-Header"), RejectionReason.NOT_AUTHORIZED);
		}
    }
}
