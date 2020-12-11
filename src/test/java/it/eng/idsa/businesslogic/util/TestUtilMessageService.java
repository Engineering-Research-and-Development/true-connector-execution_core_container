package it.eng.idsa.businesslogic.util;

import java.io.IOException;
import java.net.URI;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactRequestMessageBuilder;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessageBuilder;
import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.DescriptionRequestMessageBuilder;
import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

/**
 * Util class used for testing
 * Create methods for generating classes when required
 */
public class TestUtilMessageService {

	private static URI REQUESTED_ARTIFACT = URI.create("http://w3id.org/engrd/connector/artifact/1");
	private static URI ISSUER_CONNECTOR = URI.create("http://w3id.org/engrd/connector");
	private static String MODEL_VERSION = "4.0.0";

	private static XMLGregorianCalendar ISSUED;
	static {
		try {
			ISSUED = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
	}

	public static ArtifactRequestMessage getArtifactRequestMessage() {
		return new ArtifactRequestMessageBuilder()
				._issued_(ISSUED)
				._issuerConnector_(ISSUER_CONNECTOR)
				._modelVersion_(MODEL_VERSION)
				._requestedArtifact_(REQUESTED_ARTIFACT)
				.build();
	}

	public static ArtifactResponseMessage getArtifactResponseMessage() {
		return new ArtifactResponseMessageBuilder()
				._issued_(ISSUED)
				._issuerConnector_(ISSUER_CONNECTOR)
				._modelVersion_(MODEL_VERSION)
				.build();
	}

	public static DescriptionRequestMessage getDescriptionRequestMessage() {
		return new DescriptionRequestMessageBuilder()
				._issued_(ISSUED)
				._issuerConnector_(ISSUER_CONNECTOR)
				._modelVersion_(MODEL_VERSION)
				.build();
	}
	
	public static String getMessageAsString(Message message) {
		try {
			return MultipartMessageProcessor.serializeToJsonLD(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
