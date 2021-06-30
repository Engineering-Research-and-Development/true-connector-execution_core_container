package it.eng.idsa.businesslogic.service.resources;

import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.gson.JsonSyntaxException;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ContractOffer;

@Service
public class ContractOfferService {
	
	private static final Logger logger = LoggerFactory.getLogger(ContractOfferService.class);

	private SelfDescriptionManager sdManager;
	
	public ContractOfferService(SelfDescriptionManager sdManager) {
		this.sdManager = sdManager;
	}

	public ContractOffer getContractOffer(URI contractOfferId) {
		logger.debug("About to search contractOffer with id '{}'", contractOfferId);
		return sdManager.getContractOffer(contractOfferId);
	}
	
	public Connector addOrUpdateContractOfferToResource(ContractOffer contractOffer, URI resourceId)
			throws JsonSyntaxException, IOException {
		return sdManager.addOrUpdateContractOfferToResource(SelfDescription.getInstance().getConnector(),
				contractOffer, resourceId);
	}

	public Connector deleteContractOfferService(URI contractOffer) 
			throws JsonSyntaxException, IOException {
		return sdManager.removeContractOfferFromResource(SelfDescription.getInstance().getConnector(), 
				contractOffer);
	}
}
