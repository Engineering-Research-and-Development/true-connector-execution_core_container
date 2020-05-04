package it.eng.idsa.businesslogic.processor.producer.websocket.client;

import java.util.Collections;

import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import de.fhg.aisec.ids.comm.client.ClientConfiguration;
import de.fhg.aisec.ids.comm.client.IdscpClient;

public class IdscpClientBean {
	
	private static final Logger logger = LogManager.getLogger(IdscpClientBean.class);

	@Value("${server.ssl.SHA256}")
	private String lexicalXsdHexBinary;
	
	private IdscpClient client;
	
	public IdscpClientBean() {
		
	}

	public void createIdscpClient() {
		// Configure and start client (blocks until IDSCP has finished)
		if(client==null) {
		    client =
		        new IdscpClient()
		            .config(
		                new ClientConfiguration.Builder()
		                    .setSha256CertificateHashes(
		                        Collections.singletonList(
		                            DatatypeConverter.parseHexBinary(
		                            		lexicalXsdHexBinary)))
		                    .build());
		    
		    logger.info("Created the idscp client");
		}
	}

	public IdscpClient getClient() {
		return client;
	}
	
}
