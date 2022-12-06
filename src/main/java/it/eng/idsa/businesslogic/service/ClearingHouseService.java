package it.eng.idsa.businesslogic.service;

import de.fraunhofer.iais.eis.Message;


/**
 * @author Milan Karajovic and Gabriele De Luca
 */

/**
 * Service Interface for managing Clearing House.
 */
public interface ClearingHouseService {

	String createProcessIdAtClearingHouse(String senderToken, String contractAgreementUUID);
	
	boolean registerTransaction(Message message, String contractAgreementUUID);

	boolean isClearingHouseAvailable(String clearingHouseHealthEndpoint);
}
