package it.eng.idsa.businesslogic.service;

import org.apache.camel.Exchange;

import it.eng.idsa.multipart.domain.MultipartMessage;

public interface ReceiveDataFromBusinessLogicService {
	
	MultipartMessage receiveMessageBinary(Exchange exchange);

}
