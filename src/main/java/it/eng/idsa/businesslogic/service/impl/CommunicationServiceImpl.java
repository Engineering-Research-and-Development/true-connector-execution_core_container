/**
 * 
 */
package it.eng.idsa.businesslogic.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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

	private static final Logger logger = LoggerFactory.getLogger(CommunicationServiceImpl.class);

	@Override
	@Deprecated
	public String sendData(String endpoint, org.apache.http.HttpEntity data) {
		RestTemplate restTemplate = new RestTemplate();

		String result;
		try {
			result = restTemplate.postForObject(endpoint, data, String.class);
		} catch (RestClientException e) {
			logger.error("Error while making a request", e);
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
			result = restTemplate.postForObject(endpoint, data, String.class);
		} catch (RestClientException e) {
			logger.error("Error while making a request", e);
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
			logger.error("Error while making a request", e);
			return null;
		}
		return result;
	}

	@Override
	public String sendDataAsJson(String endpoint, String data) {
		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
//		application/ld+json is used when using mydata usage control
//		headers.set("Content-Type", "application/ld+json;charset=UTF-8");
		headers.set("Content-Type", "application/json;charset=UTF-8");

		HttpEntity<String> entity = new HttpEntity<>(data, headers);
		
		ResponseEntity<String> result;
		try {
			result = restTemplate.exchange(endpoint, HttpMethod.POST, entity, String.class);
		} catch (Exception e) {
			logger.error("Error while making a request", e.getMessage());
			throw e;
		}
		return result.getBody();
	}

}
