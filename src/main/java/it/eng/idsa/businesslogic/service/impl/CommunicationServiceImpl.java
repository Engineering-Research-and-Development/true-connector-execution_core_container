/**
 * 
 */
package it.eng.idsa.businesslogic.service.impl;


import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import it.eng.idsa.businesslogic.service.CommunicationService;

/**
 * @author Milan Karajovic and Gabriele De Luca
 *
 */
@Service
@Transactional
public class CommunicationServiceImpl implements CommunicationService {

	private static final Logger logger = LogManager.getLogger(CommunicationServiceImpl.class);

	static {
	    disableSslVerification();
	}

	private static void disableSslVerification() {
	    try
	    {
	        // Create a trust manager that does not validate certificate chains
	        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
				
				@Override
				public X509Certificate[] getAcceptedIssuers() {
					// TODO Auto-generated method stub
					return null;
				}
				
				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					// TODO Auto-generated method stub
					
				}
	        }
	        };
	       
	        // Install the all-trusting trust manager
	        SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCerts, new java.security.SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

	        // Create all-trusting host name verifier
	        HostnameVerifier allHostsValid = new HostnameVerifier() {
	            public boolean verify(String hostname, SSLSession session) {
	                return true;
	            }
	        };

	        // Install the all-trusting host verifier
	        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    } catch (KeyManagementException e) {
	        e.printStackTrace();
	    }
	}

	@Override
	@Deprecated
	public String sendData(String endpoint, HttpEntity data) {
		RestTemplate restTemplate = new RestTemplate();
		
		String result;
		try {
			result = restTemplate.postForObject (endpoint, data, String.class); 
		} catch (RestClientException e) {
			logger.error(e);
			return null;
		}
		return result;
	}
	
	@Override
	@Deprecated
	public String sendData(String endpoint, org.springframework.http.HttpEntity<?> data) {
		RestTemplate restTemplate = new RestTemplate();
		
		String result;
		try {
			result = restTemplate.postForObject (endpoint, data, String.class); 
		} catch (RestClientException e) {
			logger.error(e);
			return null;
		}
		return result;
	}
	@Override
	public String sendData(String endpoint, String data) {
		RestTemplate restTemplate = new RestTemplate();
		
		String result;
		try {
			result = restTemplate.postForObject(endpoint, data, String.class); 
		} catch (RestClientException e) {
			logger.error(e);
			return null;
		}
		return result;
	}

}
