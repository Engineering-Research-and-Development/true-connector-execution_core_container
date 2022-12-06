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
	
	private RestTemplate restTemplate;
	
	public CommunicationServiceImpl(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Override
	public String sendData(String endpoint, String data) {
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
	public String sendDataAsJson(String endpoint, String data, String contentType) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", contentType);

		HttpEntity<String> entity = new HttpEntity<>(data, headers);

		ResponseEntity<String> result;
		result = restTemplate.exchange(endpoint, HttpMethod.POST, entity, String.class);
		
		return result.getBody();
	}
	
	@Override
	public void deleteRequest(String endpoint) {
		try {
			restTemplate.delete(endpoint);
		} catch (Exception e) {
			logger.error("Error while making a delete request", e);
		}
	}

	@Override
	public String getRequest(String endpoint) {
		ResponseEntity<String> result;
		try {
			result = restTemplate.getForEntity(endpoint, String.class);
		} catch (Exception e) {
			logger.error("Error while making a request", e);
			return null;
		}
		return result.getBody();
	}

}
