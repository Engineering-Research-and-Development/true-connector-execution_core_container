package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.fraunhofer.iais.eis.ContentType;
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.Language;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceBuilder;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import it.eng.idsa.businesslogic.service.CommunicationService;
import it.eng.idsa.businesslogic.service.ResourceDataAppService;

@Service
public class ResourceDataAppServiceImpl implements ResourceDataAppService {
	
	private static final Logger logger = LogManager.getLogger(ResourceDataAppService.class);
	
	private CommunicationService communicationService;
	private String dataAppUrl;

	@Autowired
	public ResourceDataAppServiceImpl(
			CommunicationService communicationService,
			@Value("${application.openDataAppReceiver}") String dataAppUrl) {
		this.communicationService = communicationService;
		this.dataAppUrl = dataAppUrl;
	}

	@Override
	public Resource getResourceFromDataApp() {
//		String response = communicationService.sendData(dataAppUrl + "resource", data);
//		return getResource(response, Resource.class);
		return getResource();
	}

	@Override
	public ContractOffer getContractOfferFromDataApp() {
		String response = communicationService.sendData(dataAppUrl + "contractOffer", "");
		return getResource(response, ContractOffer.class);
	}
	
	
	private <T> T getResource(String resourceString, Class<T> clazz) {
		T obj = null;
		try {
			obj = new Serializer().deserialize(resourceString, clazz);
		} catch (IOException e) {
			logger.error(e);
		}
		return (T) obj;
	}
	
	// TODO only for testing until dataAPP implements endpoints for getting information
	private Resource getResource() {
		String RESOURCE_TITLE = "Resource title";
		String RESOURCE_DESCRIPTION = "Resource description";

		Resource offeredResource = (new ResourceBuilder())
				._title_(Util.asList(
						new TypedLiteral(RESOURCE_TITLE )))
				._description_(Util.asList(
						new TypedLiteral(RESOURCE_DESCRIPTION )))
				._contentType_(ContentType.SCHEMA_DEFINITION)
				._keyword_(Util.asList(new TypedLiteral("Engineering Ingegneria Informatica SpA"), 
						new TypedLiteral("broker"), new TypedLiteral("trueConnector")))
				._version_("1.0.0")
				._language_(Util.asList(Language.EN, Language.IT))
				.build();
		return offeredResource;
	}

}
