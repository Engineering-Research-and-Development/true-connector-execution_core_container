package it.eng.idsa.businesslogic.processor.receiver;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.multipart.domain.MultipartMessage;

@Component
public class ReceiverVersionCheckProcessor implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(ReceiverVersionCheckProcessor.class);
	@Autowired
	private SelfDescriptionConfiguration selfDescriptionConfiguration;
	@Autowired
	private RejectionMessageService rejectionMessageService;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);
		String versionString = multipartMessage.getHeaderContent().getModelVersion();
		List<? extends String> inboundVersions = Stream.of(selfDescriptionConfiguration.getSelfDescription().getInboundModelVersion().split(","))
				  .map(String::trim)
				  .collect(Collectors.toList());
		boolean versionSupported = checkForVersionSupport(versionString, inboundVersions);
		
		if(!versionSupported) {
			rejectionMessageService.sendRejectionMessage(multipartMessage.getHeaderContent(), RejectionReason.VERSION_NOT_SUPPORTED);
		}
	}

	private boolean checkForVersionSupport(final String versionString,	final List<? extends String> inboundVersions) {
		boolean versionSupported = false;
		for (final var version : inboundVersions) {
			if (version.equals(versionString)) {
				versionSupported = true;
				break;
			}
		}

		if (!versionSupported && logger.isWarnEnabled()) {
			logger.warn("InfoModel version incompatibility detected! "
					+ "To disable this warning, add the request's version to the inbound "
					+ "version list in your property file. PLEASE NOTE that this is NO "
					+ "guarantee that this log message will not be followed by exceptions, "
					+ "e.g. due to deserialization errors.");
		}
		return versionSupported;
	}
}
