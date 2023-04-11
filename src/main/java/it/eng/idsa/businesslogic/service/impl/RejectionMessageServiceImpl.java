package it.eng.idsa.businesslogic.service.impl;

import static de.fraunhofer.iais.eis.util.Util.asList;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionMessageBuilder;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.DateUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

/**
 *
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Service
@Transactional
public class RejectionMessageServiceImpl implements RejectionMessageService{
	
	private static final Logger logger = LoggerFactory.getLogger(RejectionMessageServiceImpl.class);
	
	private SelfDescriptionConfiguration selfDescriptionConfiguration;
	
	public RejectionMessageServiceImpl(SelfDescriptionConfiguration selfDescriptionConfiguration) {
		super();
		this.selfDescriptionConfiguration = selfDescriptionConfiguration;
	}

	@Override
	public void sendRejectionMessage(Message requestMessage, RejectionReason rejectionReason) {
		logger.info("Creating rejection message of reason {}", rejectionReason);
		if(requestMessage == null) {
			logger.info("Could not get original message, creating default rejectionMessage");
			requestMessage = UtilMessageService.getRejectionMessage(RejectionReason.MALFORMED_MESSAGE);
		}
		Message rejectionMessage = createRejectionMessage(requestMessage, rejectionReason);

		MultipartMessage multipartMessage = new MultipartMessageBuilder()
				.withHeaderContent(rejectionMessage)
				.build();
		String multipartMessageString = MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false, Boolean.TRUE);

		throw new ExceptionForProcessor(multipartMessageString);
	}

	private Message createRejectionMessage(Message header, RejectionReason rejectionReason) {
		return new RejectionMessageBuilder()
				._issuerConnector_(whoIAm())
				._issued_(DateUtil.normalizedDateTime())
				._modelVersion_(UtilMessageService.MODEL_VERSION)
				._recipientConnector_(header!=null?asList(header.getIssuerConnector()):asList(URI.create("http://auto-generated.com")))
				._correlationMessage_(header!=null?header.getId():URI.create("http://auto-generated.com"))
				._rejectionReason_(rejectionReason)
				._securityToken_(UtilMessageService.getDynamicAttributeToken())
				._senderAgent_(whoIAm())
				.build();
	}

	private URI whoIAm() {
		return selfDescriptionConfiguration.getConnectorURI();
	}

}
