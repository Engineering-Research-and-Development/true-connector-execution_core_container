package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.fraunhofer.iais.eis.Action;
import de.fraunhofer.iais.eis.Artifact;
import de.fraunhofer.iais.eis.ArtifactBuilder;
import de.fraunhofer.iais.eis.BinaryOperator;
import de.fraunhofer.iais.eis.Constraint;
import de.fraunhofer.iais.eis.ConstraintBuilder;
import de.fraunhofer.iais.eis.ContentType;
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.ContractOfferBuilder;
import de.fraunhofer.iais.eis.DataRepresentationBuilder;
import de.fraunhofer.iais.eis.DataResourceBuilder;
import de.fraunhofer.iais.eis.Language;
import de.fraunhofer.iais.eis.LeftOperand;
import de.fraunhofer.iais.eis.Permission;
import de.fraunhofer.iais.eis.PermissionBuilder;
import de.fraunhofer.iais.eis.Representation;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.RdfResource;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import it.eng.idsa.businesslogic.service.CommunicationService;
import it.eng.idsa.businesslogic.service.ResourceDataAppService;
import it.eng.idsa.multipart.util.DateUtil;

@Service
public class ResourceDataAppServiceImpl implements ResourceDataAppService {
	
	private static final Logger logger = LoggerFactory.getLogger(ResourceDataAppService.class);
	
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
			logger.error("Exception while serializing resource", e);
		}
		return (T) obj;
	}
	
	// TODO only for testing until dataAPP implements endpoints for getting information
	private Resource getResource() {
		String RESOURCE_TITLE = "Resource title";
		String RESOURCE_DESCRIPTION = "Resource description";
		
		Artifact artifact= new ArtifactBuilder(URI.create("http://w3id.org/engrd/connector/artifact/1"))
				._creationDate_(DateUtil.now())
				._fileName_("some_file.pdf")
				.build();
		
		Representation defaultRepresentation = new DataRepresentationBuilder()
				._created_(DateUtil.now())
				._instance_(Util.asList(artifact))
				.build();
		
		Constraint before = new ConstraintBuilder()
				._leftOperand_(LeftOperand.POLICY_EVALUATION_TIME)
				._operator_(BinaryOperator.AFTER)
				._rightOperand_(new RdfResource("2020-10-01T00:00:00Z", URI.create("xsd:datetime")))
				.build();
		Constraint after = new ConstraintBuilder()
				._leftOperand_(LeftOperand.POLICY_EVALUATION_TIME)
				._operator_(BinaryOperator.BEFORE)
				._rightOperand_(new RdfResource("2021-31-12T23:59:00Z", URI.create("xsd:datetime")))
				.build();
		Permission permission = new PermissionBuilder(URI.create("http://example.com/policy/restrict-access-interval"))
				._target_(URI.create("http://w3id.org/engrd/connector/artifact/1"))
				._assignee_(Util.asList(URI.create("https://assignee.com")))
				._assigner_(Util.asList(URI.create("https://assigner.com")))
				._action_(Util.asList(Action.USE))
				._constraint_(Util.asList(before, after))
				.build();
		
		ContractOffer offer = new ContractOfferBuilder()
				._consumer_(URI.create("https://consumer.com"))
				._provider_(URI.create("https://provider.com"))
				._permission_(Util.asList(permission))
				._contractDate_(DateUtil.now())
				.build();
		
		Resource offeredResource = (new DataResourceBuilder())
				._title_(Util.asList(
						new TypedLiteral(RESOURCE_TITLE )))
				._description_(Util.asList(
						new TypedLiteral(RESOURCE_DESCRIPTION )))
				._contentType_(ContentType.SCHEMA_DEFINITION)
				._keyword_(Util.asList(new TypedLiteral("Engineering Ingegneria Informatica SpA"), 
						new TypedLiteral("broker"), new TypedLiteral("trueConnector")))
				._version_("1.0.0")
				._language_(Util.asList(Language.EN, Language.IT))
				._modified_(DateUtil.now())
				._created_(DateUtil.now())
				._sovereign_(URI.create("https://soverign.com"))
				._representation_(Util.asList(defaultRepresentation))
				._contractOffer_(Util.asList(offer))
				.build();
		return offeredResource;
	}

}
