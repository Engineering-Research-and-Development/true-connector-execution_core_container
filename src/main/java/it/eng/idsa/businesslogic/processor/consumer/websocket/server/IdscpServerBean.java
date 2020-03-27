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
	
	InputStreamSocketListenerServer socketListener;
	
	public IdscpServerBean() {
	}

	public IdscpServer createIdscpServer() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, URISyntaxException {
		try {
		// Prepare keystore
	    final Path jssePath = FileSystems.getDefault().getPath("src/main/resources");
	    final KeyStore ks = KeyStore.getInstance("JKS");
	    ks.load(
	        Files.newInputStream(jssePath.resolve("ssl-server.jks")), "changeit".toCharArray());
		
		// prepare server
		IdscpServer server =
		        new IdscpServer()
		            .config(
		                new ServerConfiguration.Builder()
		                    .port(idscpServerPort)
		                    .attestationType(IdsAttestationType.BASIC)
		                    .setKeyStore(ks)
		                    .setKeyPassword("changeit")
		                    .ttpUrl(new URI("https://localhost/nonexistingdummy_ttp"))
		                    .build())
		            .setSocketListener(socketListener).start();
		
		return server;
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}

	public InputStreamSocketListenerServer getSocketListener() {
		return socketListener;
	}

	public void setSocketListener(InputStreamSocketListenerServer socketListener) {
		this.socketListener = socketListener;
	}
		
}
