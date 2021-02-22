package it.eng.idsa.businesslogic.service;

import de.fraunhofer.iais.eis.Message;

public interface SelfRegistrationService {
	
	public void sendRegistrationRequest(Message message, String selfDescription, String brokerURL);

}
