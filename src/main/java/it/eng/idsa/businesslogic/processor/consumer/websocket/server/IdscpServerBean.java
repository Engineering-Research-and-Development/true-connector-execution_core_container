package it.eng.idsa.businesslogic.processor.consumer.websocket.server;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.springframework.beans.factory.annotation.Value;

import de.fhg.aisec.ids.comm.server.IdscpServer;
import de.fhg.aisec.ids.comm.server.ServerConfiguration;
import de.fhg.aisec.ids.messages.AttestationProtos.IdsAttestationType;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

public class IdscpServerBean {
	
	@Value("${application.idscp.server.port}")
	private int idscpServerPort;

	private IdscpServer server;
	
	InputStreamSocketListener socketListener;
	
	public IdscpServerBean() {
	}

	public IdscpServer createIdscpServer() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, URISyntaxException {
		
		// Prepare keystore
	    final Path jssePath = FileSystems.getDefault().getPath("src/main/resources/jsse");
	    final KeyStore ks = KeyStore.getInstance("JKS");
	    ks.load(
	        Files.newInputStream(jssePath.resolve("server-keystore.jks")), "password".toCharArray());
		
		// prepare server
		IdscpServer server =
		        new IdscpServer()
		            .config(
		                new ServerConfiguration.Builder()
		                    .port(idscpServerPort)
		                    .attestationType(IdsAttestationType.BASIC)
		                    .setKeyStore(ks)
		                    .ttpUrl(new URI("https://localhost/nonexistingdummy_ttp"))
		                    .build())
		            .setSocketListener(socketListener).start();
		
		return server;
	}

	public InputStreamSocketListener getSocketListener() {
		return socketListener;
	}

	public void setSocketListener(InputStreamSocketListener socketListener) {
		this.socketListener = socketListener;
	}
		
}
