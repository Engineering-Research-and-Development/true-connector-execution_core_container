package it.eng.idsa.businesslogic.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.internal.constraintvalidators.hv.URLValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.idsa.businesslogic.service.ProcessExecutor;
import it.eng.idsa.businesslogic.service.impl.ProcessExecutorImpl;
import it.eng.idsa.businesslogic.util.exception.OCSPValidationException;

public class OCSPValidation {

	private static final Logger logger = LoggerFactory.getLogger(OCSPValidation.class);

	public enum OCSP_STATUS { good, none, unknown, revoked }; 
	
	public static boolean checkOCSPCerificate(String url, OCSP_STATUS desideredOCSPRevocationCheckValue)
			throws OCSPValidationException {
		logger.info("desidered OCSP Revocation Check Value " + desideredOCSPRevocationCheckValue);

		URLValidator validator = new URLValidator();

		URL forwardToURL = null;
		try {
			if (validator.isValid(url, null)) {
				forwardToURL = new URL(url);
			} else {
				forwardToURL = new URL(url.replaceFirst("wss", "https"));
			}
		} catch (MalformedURLException e) {
			logger.error("Invalid URL '%'", url);
			return false;
		}

		OCSPValidation validation = new OCSPValidation();
		OCSP_STATUS result = validation.validateRemoteCertificate(forwardToURL);

		logger.info("OCSP test result " + result);

		if (result.equals(OCSP_STATUS.revoked)) {
			logger.error("The target certificate is 'revoked'!!!");
			return false;
		} else if (result.equals(OCSP_STATUS.unknown)
				&& desideredOCSPRevocationCheckValue.equals(OCSPValidation.OCSP_STATUS.good)) {
			logger.error("The target certificate is 'unknown' but 'good' is required!!!");

			return false;
		}

		return true;
	}
	
	private OCSP_STATUS validateRemoteCertificate(URL url) throws OCSPValidationException {
		ProcessExecutor processExecutor = new ProcessExecutorImpl();
				
		String host = url.getHost();
		int port = url.getPort();
		
		if(port == -1 && url.getProtocol().equals("https")) {
			port = 443;
		}
		
		String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;
		
		logger.debug("temp dir -> " + tmpdir);
		
		List<String> startCmdList = getStartCmdList();
				
		/* get Host Certificate */
		List<String> cmdList = new ArrayList<String>(startCmdList);
		cmdList.add("echo | openssl s_client -connect " + host + ":" + port + " -servername " + host);
		String getCertificateResponse = processExecutor.executeProcess(cmdList);
		String serverCertificate = getCertificatesInString(getCertificateResponse, null);
		
		if(serverCertificate.length() > 0) {
			try {
				Files.write(Paths.get(tmpdir + host + ".pem"), serverCertificate.getBytes(StandardCharsets.UTF_8));
				logger.info("File " + tmpdir + host + ".pem correctly saved.");
			} catch (IOException e) {
				logger.warn(e.getMessage(), e.getCause());
				throw new OCSPValidationException("OCSPValidationException() cause:", e.getCause());
			}			
		} else {
			logger.error("File " + tmpdir + host + ".pem is empty.");
			new File(tmpdir + host + ".pem").delete();
			return OCSP_STATUS.unknown;
		}
		
		/* get OCSP URI if exist */
		cmdList = new ArrayList<String>(startCmdList);
		cmdList.add("openssl x509 -noout -ocsp_uri -in " + tmpdir + host + ".pem");
		String existOCSPUriResponse = processExecutor.executeProcess(cmdList);
		logger.info("Checking for OCSP server Uri. Response " + existOCSPUriResponse);
		
		if(existOCSPUriResponse.isBlank()) {
			logger.error("No OCSP server Uri found. Response " + getCertificateResponse);
			
			File hostCertificate = new File(tmpdir + host + ".pem");
			hostCertificate.delete();
			
			return OCSP_STATUS.unknown;
		}
		
		try {
			URL ocspUri = new URL(existOCSPUriResponse);
			
			/* get Certificate Chain */			
			cmdList = new ArrayList<String>(startCmdList);
			cmdList.add("echo | openssl s_client -connect " + host + ":" + port + " -showcerts");
			String getChainResponse = processExecutor.executeProcess(cmdList);
			String chainCertificate = getCertificatesInString(getChainResponse, serverCertificate);
			if(!chainCertificate.isBlank()) {
				try {
					Files.write(Paths.get(tmpdir + host + "_chain.pem"), chainCertificate.getBytes(StandardCharsets.UTF_8));
					logger.info("File" + tmpdir + host + "_chain.pem correctly saved.");				
				} catch (IOException e) {
					logger.warn(e.getMessage(), e.getCause());
					throw new OCSPValidationException("OCSPValidationException() cause:", e.getCause());
				}
			} else {
				logger.error("File " + tmpdir + host + "_chain.pem NOT saved.");
				return OCSP_STATUS.unknown;
			}
					
			/* sending OCSP request */
			cmdList = new ArrayList<String>(startCmdList);
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
			logger.warn("No OCSP URI found!!!");
			logger.warn(e.getMessage(), e.getCause());
			throw new OCSPValidationException("OCSPValidationException() cause:", e.getCause());
		} finally {
			/* delete cert files*/
			new File(tmpdir + host + ".pem").delete();
			new File(tmpdir + host + "_chain.pem").delete();
							
			logger.debug("delete .pem file from FileSystem");
		}
				
		return OCSP_STATUS.unknown;
	}

	private List<String> getStartCmdList() {
		List<String> startCmdList = new ArrayList<String>();
		if(System.getProperty("os.name").toLowerCase().contains("win")) {
			logger.info("Running on windows");
			startCmdList.add("cmd.exe");
			startCmdList.add("/c");
		} else {
			startCmdList.add("/bin/sh");
			startCmdList.add("-c");
		}
		return startCmdList;
	}		

	private String getCertificatesInString(String startingChain, String certificateToExclude) {
		String remainString = startingChain;
		
		List<String> certificatesInChain = new ArrayList<String>();
		
		while(remainString.indexOf("-----BEGIN CERTIFICATE-----") != -1) {
			int indexEndCerificate = remainString.indexOf("-----END CERTIFICATE-----") + 25;
			certificatesInChain.add(
					remainString.substring(remainString.indexOf("-----BEGIN CERTIFICATE-----"), 
							indexEndCerificate));
			remainString = remainString.substring(indexEndCerificate);			
		}
		
		StringBuffer result = new StringBuffer();
		
		for (String certificateCurr : certificatesInChain) {
			if(!(certificateCurr + "\n").equals(certificateToExclude)) {
				result.append(certificateCurr + "\n");
			}
		}
		
		return result.toString();		
	}
	
}

