package it.eng.idsa.businesslogic.processor.receiver;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.usagecontrol.service.UsageControlService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;


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

    @Override
    public void process(Exchange exchange) {
        if (!isEnabledUsageControl) {
            logger.info("Usage control not configured - continued with flow");
            return;
        }
        
		Map<String, Object> headerParts = exchange.getMessage().getHeaders();
		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);
		String originalHeader = headerParts.get("Original-Message-Header").toString();
		requestMessage = MultipartMessageProcessor.getMessage(originalHeader);
		responseMessage = multipartMessage.getHeaderContent();
		
		if (!(requestMessage instanceof ArtifactRequestMessage) || !(responseMessage instanceof ArtifactResponseMessage)) {
			logger.info("Usage Control not applied - not ArtifactRequestMessage/ArtifactResponseMessage");
			headerParts.remove("Original-Message-Header");
			return;
		}

		try {
			ArtifactRequestMessage artifactRequestMessage = (ArtifactRequestMessage) requestMessage;
			ArtifactResponseMessage artifactResponseMessage = (ArtifactResponseMessage) responseMessage;
			logger.info("Proceeding with Usage control enforcement");
			
			String payloadToEnforce = usageControlService.createUsageControlObject(artifactRequestMessage, artifactResponseMessage,
			        multipartMessage.getPayloadContent());
			
			logger.info("from: " + exchange.getFromEndpoint());
			logger.debug("Message Body: " + payloadToEnforce);
			
			MultipartMessage reponseMultipartMessage = new MultipartMessageBuilder()
					.withHttpHeader(multipartMessage.getHttpHeaders())
					.withHeaderHeader(multipartMessage.getHeaderHeader())
					.withHeaderContent(responseMessage)
					.withPayloadHeader(multipartMessage.getPayloadHeader())
					.withPayloadContent(payloadToEnforce)
					.withToken(multipartMessage.getToken())
					.build();
			
			exchange.getMessage().setBody(reponseMultipartMessage);
			headerParts.remove("Original-Message-Header");
		} catch (Exception e) {
			logger.error("Usage Control Enforcement has failed with MESSAGE: {}", e.getMessage());
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_USAGE_CONTROL, requestMessage);
		}
    }
}
