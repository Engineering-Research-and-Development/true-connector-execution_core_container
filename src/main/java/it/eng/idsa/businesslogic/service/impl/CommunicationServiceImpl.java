/**
 * 
 */
package it.eng.idsa.businesslogic.service.impl;


import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
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

	
	@Override
	public String sendData(String endpoint, HttpEntity data) {
		// TODO Auto-generated method stub
		RestTemplate restTemplate = new RestTemplate();
		
		
		String result;
		try {
			result = restTemplate.postForObject (endpoint, EntityUtils.toString(data), String.class);
			return result;
		} catch (RestClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
