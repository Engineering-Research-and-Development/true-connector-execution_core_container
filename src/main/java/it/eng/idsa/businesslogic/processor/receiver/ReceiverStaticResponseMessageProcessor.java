package it.eng.idsa.businesslogic.processor.receiver;

import java.io.File;
import java.net.URI;
import java.util.Base64;

import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.camel.Exchange;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import de.fraunhofer.iais.eis.ArtifactResponseMessageBuilder;
import de.fraunhofer.iais.eis.Message;
import org.apache.camel.Processor;

@Component
public class ReceiverStaticResponseMessageProcessor implements Processor {

	private static final Logger logger = LogManager.getLogger(ReceiverStaticResponseMessageProcessor.class);

	public static URI ISSUER_CONNECTOR = URI.create("http://w3id.org/engrd/connector");
	public static String MODEL_VERSION = "4.0.0";
	public static URI CORRELATION_MESSAGE = URI
			.create("http://w3id.org/connectorUnavailableMessage/1a421b8c-3407-44a8-aeb9-253f145c869a");
	public static URI TRANSFER_CONTRACT = URI.create("http://w3id.org/engrd/connector//examplecontract");
	public static XMLGregorianCalendar ISSUED;
	
	@Value("${application.sourceFileName}")
	private String sourceFileName;
	
	@Override
	public void process(Exchange exchange) throws Exception {

		try {
			// print response message
			// logger.info("RESPONSE SEMESSAGE " +
			// MultipartMessageProcessor.serializeToJsonLD(getArtifactRequestMessage()));

			//sample arifact response message
			Message msg = new ArtifactResponseMessageBuilder()

					._issued_(ISSUED)
					._correlationMessage_(CORRELATION_MESSAGE)
					._transferContract_(TRANSFER_CONTRACT)
					._issuerConnector_(ISSUER_CONNECTOR)
					._modelVersion_(MODEL_VERSION)
					.build();

			File pdf = new File(sourceFileName);

			exchange.getMessage().setBody(pdf, Base64.class);

			exchange.getMessage().setHeader("idscp2-header", msg);
			
			exchange.setProperty("ids-type", "ArtifactResponseMessage");

		} catch (Exception e) {
			logger.error("Error ArtifactResponseMessage:", e);

		}

	}

}
