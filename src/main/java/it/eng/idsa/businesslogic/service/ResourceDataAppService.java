package it.eng.idsa.businesslogic.service;

import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.Resource;

/**
 * Service responsible for fetching Resource and ContractOffer data from connected DataApp
 * @author igor.balog
 *
 */
public interface ResourceDataAppService {

	Resource getResourceFromDataApp();
	
	Resource[] getResourcesFromDataApp(String catalogNumber);
	
	ContractOffer getContractOfferFromDataApp();
}
