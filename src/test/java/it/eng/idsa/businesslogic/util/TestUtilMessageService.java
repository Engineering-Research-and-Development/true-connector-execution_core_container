package it.eng.idsa.businesslogic.util;

import java.io.IOException;
import java.net.URI;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import de.fraunhofer.iais.eis.Action;
import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactRequestMessageBuilder;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessageBuilder;
import de.fraunhofer.iais.eis.BinaryOperator;
import de.fraunhofer.iais.eis.Constraint;
import de.fraunhofer.iais.eis.ConstraintBuilder;
import de.fraunhofer.iais.eis.ContractAgreement;
import de.fraunhofer.iais.eis.ContractAgreementBuilder;
import de.fraunhofer.iais.eis.ContractAgreementMessage;
import de.fraunhofer.iais.eis.ContractAgreementMessageBuilder;
import de.fraunhofer.iais.eis.ContractRequestMessage;
import de.fraunhofer.iais.eis.ContractRequestMessageBuilder;
import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.DescriptionRequestMessageBuilder;
import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.LeftOperand;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.Permission;
import de.fraunhofer.iais.eis.PermissionBuilder;
import de.fraunhofer.iais.eis.RejectionMessage;
import de.fraunhofer.iais.eis.RejectionMessageBuilder;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.iais.eis.util.RdfResource;
import de.fraunhofer.iais.eis.util.Util;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

public class TestUtilMessageService {

	public static final String TOKEN_VALUE = "DummyTokenValue";

	public static URI REQUESTED_ARTIFACT = URI.create("http://w3id.org/engrd/connector/artifact/1");

	public static URI ISSUER_CONNECTOR = URI.create("http://w3id.org/engrd/connector");
	public static URI RECIPIENT_CONNECTOR = URI.create("http://w3id.org/engrd/connector/recipient");
	
	public static String MODEL_VERSION = "4.0.0";
	
	public static URI CORRELATION_MESSAGE = URI.create("http://w3id.org/connectorUnavailableMessage/1a421b8c-3407-44a8-aeb9-253f145c869a");
	
	public static URI TRANSFER_CONTRACT = URI.create("http://w3id.org/engrd/connector//examplecontract");
	
	public static XMLGregorianCalendar ISSUED;
	
	static {
		try {
			ISSUED = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public static ArtifactRequestMessage getArtifactRequestMessage() {
		return new ArtifactRequestMessageBuilder()
				._issued_(ISSUED)
				._correlationMessage_(CORRELATION_MESSAGE)
				._transferContract_(TRANSFER_CONTRACT)
				._issuerConnector_(ISSUER_CONNECTOR)
				._modelVersion_(MODEL_VERSION)
				._requestedArtifact_(REQUESTED_ARTIFACT)
				.build();
	}
	
	public static ArtifactRequestMessage getArtifactRequestMessageWithToken() {
		return new ArtifactRequestMessageBuilder()
				._issued_(ISSUED)
				._correlationMessage_(CORRELATION_MESSAGE)
				._transferContract_(TRANSFER_CONTRACT)
				._issuerConnector_(ISSUER_CONNECTOR)
				._modelVersion_(MODEL_VERSION)
				._requestedArtifact_(REQUESTED_ARTIFACT)
				._securityToken_(getDynamicAttributeToken())
				.build();
	}

	public static ArtifactResponseMessage getArtifactResponseMessage() {
		return new ArtifactResponseMessageBuilder()
				._issued_(ISSUED)
				._correlationMessage_(CORRELATION_MESSAGE)
				._transferContract_(TRANSFER_CONTRACT)
				._issuerConnector_(ISSUER_CONNECTOR)
				._modelVersion_(MODEL_VERSION)
				.build();
	}
	
	public static DynamicAttributeToken getDynamicAttributeToken() {
		return new DynamicAttributeTokenBuilder()
				._tokenFormat_(TokenFormat.JWT)
				._tokenValue_("DummyTokenValue")
				.build();		
	}
	
	public static DescriptionRequestMessage getDescriptionRequestMessage() {
		return new DescriptionRequestMessageBuilder()
				._issued_(ISSUED)
				._issuerConnector_(ISSUER_CONNECTOR)
				._modelVersion_(MODEL_VERSION)
				.build();
	}
	
	public static RejectionMessage getRejectionMessage() {
		return new RejectionMessageBuilder()
				._issuerConnector_(ISSUER_CONNECTOR)
				._issued_(ISSUED)
				._modelVersion_(MODEL_VERSION)
				._transferContract_(TRANSFER_CONTRACT)
				._correlationMessage_(URI.create("http://correlationMessage"))
				._rejectionReason_(RejectionReason.NOT_AUTHENTICATED)
				.build();
	}
	
	/**
	 * Used as header in contract agreement flow
	 * @return
	 */
	public static ContractAgreementMessage createContractAgreementMessage() {
		return new ContractAgreementMessageBuilder()
				._modelVersion_(MODEL_VERSION)
				._transferContract_(URI.create("http://transferedContract"))
				._correlationMessage_(URI.create("http://correlationMessage"))
				._issued_(ISSUED)
				._issuerConnector_(ISSUER_CONNECTOR)
				.build();
	}
	
	/**
	 * Used as payload in contract agreement flow
	 * @return
	 */
	public static ContractAgreement createContractAgreement() {
		Constraint constraint = new ConstraintBuilder()
				._leftOperand_(LeftOperand.POLICY_EVALUATION_TIME)
				._operator_(BinaryOperator.EQUALS)
				._rightOperand_(new RdfResource("2021-04-01T00:00:00Z", URI.create("xsd:datetimeStamp")))
				._pipEndpoint_(URI.create("https//pip.com/policy_evaluation_time"))
				.build();
		
		Permission permission = new PermissionBuilder()
				._action_(Util.asList(Action.USE))
				._target_(REQUESTED_ARTIFACT)
				._assignee_(Util.asList(URI.create("https://assignee.com")))
				._assigner_(Util.asList(URI.create("https://assigner.com")))
				._constraint_(Util.asList(constraint))
				.build();
		
		return new ContractAgreementBuilder()
				._provider_(ISSUER_CONNECTOR)
				._consumer_(RECIPIENT_CONNECTOR)
				._permission_(Util.asList(permission))
				.build();
	}
	
	public static ContractRequestMessage createContractRequestMessage() {
		return new ContractRequestMessageBuilder()
				._issued_(ISSUED)
				._modelVersion_(MODEL_VERSION)
				._issuerConnector_(ISSUER_CONNECTOR)
				._recipientConnector_(Util.asList(RECIPIENT_CONNECTOR))
				._correlationMessage_(CORRELATION_MESSAGE)
				.build();
	}
	
	public static Map<String, Object> getArtifactResponseMessageAsMap() {
		Map<String, Object> messageAsMap = new HashMap<>();
		messageAsMap.put("IDS-Messagetype","ids:ArtifactResponseMessage");
		messageAsMap.put("IDS-Issued","2021-04-07T13:09:42.306Z");
		messageAsMap.put("IDS-IssuerConnector","http://true-connector.com");
		messageAsMap.put("IDS-Id","https://w3id.org/idsa/autogen/artifactResponseMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f");
		messageAsMap.put("IDS-ModelVersion","4.0.0");
		messageAsMap.put("IDS-RequestedArtifact", "http:/true-connector/artifact/1");
		return messageAsMap;
	}
	
	public static Map<String, String> covnvertMapObjectToMapString(Map<String, Object> objectMap) {
		return objectMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue()));

	}
	
	public static String getMessageAsString(Message message) {
		try {
			return MultipartMessageProcessor.serializeToJsonLD(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
