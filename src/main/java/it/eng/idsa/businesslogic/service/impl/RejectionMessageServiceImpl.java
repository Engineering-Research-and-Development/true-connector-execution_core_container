package it.eng.idsa.businesslogic.service.impl;

import static de.fraunhofer.iais.eis.util.Util.asList;

import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

import org.apache.maven.project.MavenProject;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionMessageBuilder;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.ResultMessageBuilder;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import nl.tno.ids.common.multipart.MultiPart;
import nl.tno.ids.common.multipart.MultiPartMessage;
import nl.tno.ids.common.multipart.MultiPartMessage.Builder;
import nl.tno.ids.common.serialization.DateUtil;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Service
@Transactional
public class RejectionMessageServiceImpl implements RejectionMessageService{

	@Value("${information.model.version}")
    private String informationModelVersion;
	
	@Override 
	public void sendRejectionMessage(RejectionMessageType rejectionMessageType, Message message) {
		Message rejectionMessage = createRejectionMessage(rejectionMessageType.toString(), message);
		Builder builder = new MultiPartMessage.Builder();
		builder.setHeader(rejectionMessage);
		MultiPartMessage builtMessage = builder.build();
		String stringMessage = MultiPart.toString(builtMessage, false);
		throw new ExceptionForProcessor(stringMessage);
	}
	
	private Message createRejectionMessage(String rejectionMessageType, Message message) {
		Message rejectionMessage = null;
		switch(rejectionMessageType) {
			case "RESULT_MESSAGE":
				rejectionMessage = createResultMessage(message);
				break;
			case "REJECTION_MESSAGE_COMMON":
				rejectionMessage = createRejectionMessageCommon(message);
				break;
			case "REJECTION_TOKEN":
				rejectionMessage = createRejectionToken(message);
				break;
			case "REJECTION_MESSAGE_LOCAL_ISSUES":
				rejectionMessage = createRejectionMessageLocalIssues(message);
				break;
			case "REJECTION_TOKEN_LOCAL_ISSUES":
				rejectionMessage = createRejectionTokenLocalIssues(message);
				break;
			case "REJECTION_COMMUNICATION_LOCAL_ISSUES":
				rejectionMessage = createRejectionCommunicationLocalIssues(message);
				break;	
			default:
				rejectionMessage = createResultMessage(message);
				break;
		}
		return rejectionMessage;
	}

	private String getInformationModelVersion() {
		String currentInformationModelVersion = null;
		try {
			
			InputStream is = RejectionMessageServiceImpl.class.getClassLoader().getResourceAsStream("META-INF/maven/it.eng.idsa/market4.0-execution_core_container_business_logic/pom.xml");
			MavenXpp3Reader reader = new MavenXpp3Reader();
			Model model = reader.read(is);
			MavenProject project = new MavenProject(model);
			Properties props = project.getProperties(); 
			if (props.get("information.model.version")!=null) {
				return props.get("information.model.version").toString();
			}
			for (int i = 0; i < model.getDependencies().size(); i++) {
				if (model.getDependencies().get(i).getGroupId().equalsIgnoreCase("de.fraunhofer.iais.eis.ids.infomodel")){
					String version=model.getDependencies().get(i).getVersion();
					// If we want, we can delete "-SNAPSHOT" from the version
//					if (version.contains("-SNAPSHOT")) {
//						version=version.substring(0,version.indexOf("-SNAPSHOT"));
//					}
					currentInformationModelVersion=version;
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}

		return currentInformationModelVersion;
	}

	/*
	 * public String getInformationModelVersion() { return informationModelVersion;
	 * }
	 */

	public void setInformationModelVersion(String informationModelVersion) {
		this.informationModelVersion = informationModelVersion;
	}

	private Message createResultMessage(Message header) {
		return new ResultMessageBuilder()
				._issuerConnector_(whoIAm())
				._issued_(DateUtil.now())
                ._modelVersion_(getInformationModelVersion())
				._recipientConnector_(asList(header.getIssuerConnector()))
				._correlationMessage_(header.getId())
				.build();
	}

	private Message createRejectionMessageCommon(Message header) {
		return new RejectionMessageBuilder()
				._issuerConnector_(whoIAm())
				._issued_(DateUtil.now())
                ._modelVersion_(getInformationModelVersion())
				._recipientConnector_(header!=null?asList(header.getIssuerConnector()):asList(URI.create("auto-generated")))
				._correlationMessage_(header!=null?header.getId():URI.create(""))
				._rejectionReason_(RejectionReason.MALFORMED_MESSAGE)
				.build();
	}
	
	private Message createRejectionToken(Message header) {
		return new RejectionMessageBuilder()
				._issuerConnector_(whoIAm())
				._issued_(DateUtil.now())
                ._modelVersion_(getInformationModelVersion())
				._recipientConnector_(asList(header.getIssuerConnector()))
				._correlationMessage_(header.getId())
				._rejectionReason_(RejectionReason.NOT_AUTHENTICATED)
				.build();
	}


	private URI whoIAm() {
		//TODO 
		return URI.create("auto-generated");
	}

	private Message createRejectionMessageLocalIssues(Message header) {
		return new RejectionMessageBuilder()
				._issuerConnector_(URI.create("auto-generated"))
				._issued_(DateUtil.now())
                ._modelVersion_(getInformationModelVersion())
				//._recipientConnectors_(header!=null?asList(header.getIssuerConnector()):asList(URI.create("auto-generated")))
				._correlationMessage_(URI.create("auto-generated"))
				._rejectionReason_(RejectionReason.MALFORMED_MESSAGE)
				.build();
	}
	
	private Message createRejectionTokenLocalIssues(Message header) {
		return new RejectionMessageBuilder()
				._issuerConnector_(header.getIssuerConnector())
				._issued_(DateUtil.now())
                ._modelVersion_(getInformationModelVersion())
				._recipientConnector_(asList(header.getIssuerConnector()))
				._correlationMessage_(header.getId())
				._rejectionReason_(RejectionReason.NOT_AUTHENTICATED)
				.build();
	}
	
	private Message createRejectionCommunicationLocalIssues(Message header) {
		return new RejectionMessageBuilder()
				._issuerConnector_(header.getIssuerConnector())
				._issued_(DateUtil.now())
                ._modelVersion_(getInformationModelVersion())
				._recipientConnector_(asList(header.getIssuerConnector()))
				._correlationMessage_(header.getId())
				._rejectionReason_(RejectionReason.NOT_FOUND)
				.build();
	}
}
