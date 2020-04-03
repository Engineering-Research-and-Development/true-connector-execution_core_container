package it.eng.idsa.businesslogic.processor.consumer.websocket.server;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import de.fhg.aisec.ids.comm.server.IdscpServer;
import de.fhg.aisec.ids.comm.server.ServerConfiguration;
import de.fhg.aisec.ids.messages.AttestationProtos.IdsAttestationType;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import java.io.InputStream;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

public class IdscpServerBean {
	
	@Value("${application.idscp.server.port}")
	private int idscpServerPort;
	
	@Value("${server.ssl.key-store-type}")
	private String keyStoreType;
	
	@Value("${server.ssl.key-store}")
	private String keyStoreLoation;
	
	@Value("${server.ssl.key-password}")
	private String keyStorePassword;
	
	@Value("${application.idscp.server.ttpUri}")
	private String ttpUrl;
	
	@Autowired
    ResourceLoader resourceLoader;

	private IdscpServer idscpServer;
	
	InputStreamSocketListenerServer socketListener;
	
	public IdscpServerBean() {
	}

	public void createIdscpServer() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, URISyntaxException {
		
		if(idscpServer==null) {
			// Prepare keystore
			Resource resourceKeyStore = resourceLoader.getResource(keyStoreLoation);
			InputStream keyStore = resourceKeyStore.getInputStream();
			final KeyStore ks = KeyStore.getInstance(keyStoreType);
			ks.load(keyStore, keyStorePassword.toCharArray());
				
			// prepare server
			idscpServer =
			        new IdscpServer()
			            .config(
			                new ServerConfiguration.Builder()
			                    .port(idscpServerPort)
			                    .attestationType(IdsAttestationType.BASIC)
			                    .setKeyStore(ks)
			                    .setKeyPassword(keyStorePassword)
			                    .ttpUrl(new URI(ttpUrl))
			                    .build())
			            .setSocketListener(socketListener).start();
		}
	}

	public void setSocketListener(InputStreamSocketListenerServer socketListener) {
		this.socketListener = socketListener;
	}
	
	public IdscpServer getIdscpServer() {
		return this.idscpServer;
	}
	
	@PreDestroy
    public void onDestroy() throws Exception {
        if (idscpServer.getServer()!=null) {
        	idscpServer.getServer().stop();
        	idscpServer.getServer().destroy();
        }
    }
		
}
