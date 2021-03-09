package it.eng.idsa.businesslogic.service;

import de.fraunhofer.iais.eis.Message;

public interface BrokerService {
	
	public void sendBrokerRequest(Message message, String payload);

}
