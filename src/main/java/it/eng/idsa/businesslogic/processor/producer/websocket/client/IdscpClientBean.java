package it.eng.idsa.businesslogic.processor.producer.websocket.client;

import java.util.Collections;
import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fhg.aisec.ids.comm.client.ClientConfiguration;
import de.fhg.aisec.ids.comm.client.IdscpClient;

public class IdscpClientBean {
	
	private static final Logger logger = LogManager.getLogger(IdscpClientBean.class);

	private String LEXICAL_XSD_HEX_BINARY = "4439DA49F320E3786319A5CF8D69F3A0831C4801B5CE3A14570EA84E0ECD82B0";
	
	private IdscpClient client;
	
	public IdscpClientBean() {
		this.client = this.createIdscpClient();
	}

	private IdscpClient createIdscpClient() {
		// Configure and start client (blocks until IDSCP has finished)
	    IdscpClient client =
	        new IdscpClient()
	            .config(
	                new ClientConfiguration.Builder()
	                    .setSha256CertificateHashes(
	                        Collections.singletonList(
	                            DatatypeConverter.parseHexBinary(
	                            		LEXICAL_XSD_HEX_BINARY)))
	                    .build());
	    
	    logger.info("Created the idscp client");
	    
	    return client;
	}

	public IdscpClient getClient() {
		return client;
	}
	
}
