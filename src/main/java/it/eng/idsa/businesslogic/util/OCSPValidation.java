package it.eng.idsa.businesslogic.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import it.eng.idsa.businesslogic.service.ProcessExecutor;
import it.eng.idsa.businesslogic.service.impl.ProcessExecutorImpl;
import it.eng.idsa.businesslogic.util.exception.OCSPValidationException;

public class OCSPValidation {

	/*
	 * @Autowired private KeystoreProvider keystoreProvider;
	 */

	private static final Logger logger = LoggerFactory.getLogger(OCSPValidation.class);

	public enum OCSP_STATUS { good, revoked, unknown }; 
	
	@Autowired
	private ProcessExecutor processExecutor = new ProcessExecutorImpl();

	public static void main(String[] args) {
		OCSPValidation sovs = new OCSPValidation();

		try {
			OCSP_STATUS result = sovs.testURL(new URL("https://lucalux.giize.com:8890/data"));
			//OCSP_STATUS result = sovs.testURL(new URL("https://wikipedia.org/wiki/Pagina_principale"));
			//OCSP_STATUS result = sovs.testConnectionTo(new URL("https://revoked.badssl.com/"));
			
			logger.info(result.toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (OCSPValidationException e) {
			e.printStackTrace();
		}
	}

	public OCSP_STATUS testURL(URL url) throws OCSPValidationException {
		//Calendar before = Calendar.getInstance();
		
		String host = url.getHost();
		int port = url.getPort();
		
		if(port == -1 && url.getProtocol().equals("https")) {
			port = 443;
		}
		
		String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;
		
		logger.debug("temp dir -> " + tmpdir);
		
		/* get Host Certificate */
		List<String> cmdList = new ArrayList<>();
		cmdList.add("/bin/sh");
		cmdList.add("-c");
		cmdList.add("openssl s_client -connect " + host + ":" + port + " 2>&1 < /dev/null | sed -n '/-----BEGIN/,/-----END/p' > " + tmpdir + host + ".pem");
		
		String writeCertificateResponse = processExecutor.executeProcess(cmdList);
		
		File hostCertificate = new File(tmpdir + host + ".pem");
		if(hostCertificate.length() > 0) {
			logger.info("File " + tmpdir + host + ".pem correctly saved.");
		} else {
			logger.error("File " + tmpdir + host + ".pem is empty.");
			
			hostCertificate.delete();
			
			return OCSP_STATUS.unknown;
		}
		
		/* get OCSP URI if exist */
		cmdList = new ArrayList<>();
		cmdList.add("/bin/sh");
		cmdList.add("-c");
		cmdList.add("openssl x509 -noout -ocsp_uri -in " + tmpdir + host + ".pem");
		
		String existOCSPUriResponse = processExecutor.executeProcess(cmdList);
		
		logger.info("Test to verify if exist a OCSP server Uri. Response " + existOCSPUriResponse);
		
		if(existOCSPUriResponse.isBlank()) {
			logger.error("NOT exist a OCSP server Uri. Response " + writeCertificateResponse);
			
			hostCertificate.delete();
			
			return OCSP_STATUS.unknown;
		}
		
		try {
			URL ocspUri = new URL(existOCSPUriResponse);
			
			/* get Certificate Chain */			
			cmdList = new ArrayList<>();
			cmdList.add("/bin/sh");
			cmdList.add("-c");
			cmdList.add("openssl s_client -connect " + host + ":" + port + " -showcerts 2>&1 < /dev/null | sed -n '/-----BEGIN/,/-----END/p' > " + tmpdir + host + "_chain.pem");
			String writeCertificateChainResponse = processExecutor.executeProcess(cmdList);
			
			if(new File(tmpdir + host + "_chain.pem").length() > 0) {
				logger.info("File" + tmpdir + host + "_chain.pem correctly saved.");				
			} else {
				logger.error("File " + tmpdir + host + "_chain.pem NOT saved.");
				return OCSP_STATUS.unknown;
			}
			
			try {
				Path pathChain = Paths.get(tmpdir + host + "_chain.pem");
				Charset charset = StandardCharsets.UTF_8;

				String chainContent = new String(Files.readAllBytes(pathChain), charset);
				
				Path pathCertificate = Paths.get(tmpdir + host + ".pem");

				String certificateContent = new String(Files.readAllBytes(pathCertificate), charset);
				
				logger.debug(host + "_chain.pem -->\n" + chainContent);
				logger.info(host + "_chain.pem downloaded hashcode " + chainContent.hashCode());
				chainContent = chainContent.replaceAll(Pattern.quote(certificateContent),"");
				logger.info(host + "_chain.pem hashcode after removing target certificate " + chainContent.hashCode());
				logger.debug(host + "_chain.pem after removing target certificate -->\n" + chainContent);
								
				Files.write(pathChain, chainContent.getBytes(charset));
			} catch (IOException e) {
				logger.warn(e.getMessage(), e.getCause());
				throw new OCSPValidationException("OCSPValidationException() cause:", e.getCause());
			}
			
			/* sending OCSP request */
			cmdList = new ArrayList<>();
			cmdList.add("/bin/sh");
			cmdList.add("-c");
			cmdList.add("openssl ocsp -issuer " + tmpdir + host + "_chain.pem -cert " + tmpdir + host + ".pem -text -url "+ ocspUri);
			
			String ocspTestResponse = processExecutor.executeProcess(cmdList);
			
			logger.debug("OCSP test response " + ocspTestResponse);
						
			if(ocspTestResponse.contains(": " + OCSP_STATUS.revoked.name())) {
				return OCSP_STATUS.revoked;
			} else if(ocspTestResponse.contains(": " + OCSP_STATUS.good.name())) {
				return OCSP_STATUS.good;
			} else if(ocspTestResponse.contains(": " + OCSP_STATUS.unknown.name())) {
				return OCSP_STATUS.unknown;
			}			
		} catch (MalformedURLException e) {
			logger.warn("No OCSP URI exist!!!");
			logger.warn(e.getMessage(), e.getCause());
			throw new OCSPValidationException("OCSPValidationException() cause:", e.getCause());
		} finally {
			/* delete cert files*/
			new File(tmpdir + host + ".pem").delete();
			new File(tmpdir + host + "_chain.pem").delete();
							
			logger.debug("delete .pem file from FileSystem");
		}
		
		/*
		 * StringBuffer certs = new StringBuffer();
		 * 
		 * processResponse = processResponse.substring(processResponse.
		 * indexOf("-----BEGIN CERTIFICATE-----"));
		 * 
		 * while(processResponse.indexOf("-----BEGIN CERTIFICATE-----") != -1) { String
		 * newcert = processResponse.substring(processResponse.
		 * indexOf("-----BEGIN CERTIFICATE-----"),
		 * processResponse.indexOf("-----END CERTIFICATE-----")+25);
		 * certs.append(newcert+"\n\n");
		 * 
		 * processResponse = processResponse.substring(processResponse.
		 * indexOf("-----BEGIN CERTIFICATE-----") + newcert.length()); }
		 * 
		 * System.err.println(certs);
		 */

		//logger.info((Calendar.getInstance().getTimeInMillis() - before.getTimeInMillis())/1000);
		
		return null;

	}		

}

