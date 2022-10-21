package it.eng.idsa.businesslogic.audit;

public enum TrueConnectorEventType {

	TRUE_CONNECTOR_EVENT("Default TrueConnector event"),
	HTTP_REQUEST_RECEIVED("Http request received"),
	USER_AUTHORIZATION_FAILURE("Authorization failure"),
	USER_AUTHORIZATION_SUCCESS("Authorization success"),
	USER_BLOCKED("User blocked"),
	SELF_DESCRIPTION("Self description requested"),
	CONTRACT_OFFER("Contract offer requested"),
	CONTRACT_OFFER_CREATED("Contract offer created"),
	CONTRACT_OFFER_UPDATED("Contract offer updated"),
	CONTRACT_OFFER_DELETED("Contract offer deleted"),
	OFFERED_RESOURCE("Offered resource requested"),
	OFFERED_RESOURCE_CREATED("Offered resource created"),
	OFFERED_RESOURCE_UPDATED("Offered resource updated"),
	OFFERED_RESOURCE_DELETED("Offered resource deleted"),
	REPRESENTATION("Representation requested"),
	REPRESENTATION_CREATED("Representation created"),
	REPRESENTATION_UPDATED("Representation updated"),
	REPRESENTATION_DELETED("Representation deleted"),
	EXCEPTION_BAD_REQUEST("Bad request"),
	EXCEPTION_NOT_FOUND("Entity not found"),
	EXCEPTION_SERVER_ERROR("Server error"),
	EXCEPTION_GENERAL("General exception"),
	// Camel
	CONNECTOR("Connector event"), 
	CONNECTOR_REQUEST("Connector received message"), 
	CONNECTOR_RESPONSE("Connector received response"), 
	CONNECTOR_SEND("Connector message forwarding"),
	CONNECTOR_SEND_DATAAPP("Connector message forwarding - dataApp"),
	CONNECTOR_TOKEN_FETCH_SUCCESS("Connector obtained DAT token"),
	CONNECTOR_TOKEN_FETCH_FAILURE("Connector could not obtain DAT token"),
	CONNECTOR_TOKEN_VALIDATED_SUCCESS("Connector validated DAT token successfuly"),
	CONNECTOR_TOKEN_VALIDATED_FAILURE("Connector failed to validate DAT token"),
	CONNECTOR_CLEARING_HOUSE_SUCCESS("Connector registered transaction to clearing house"),
	CONNECTOR_CLEARING_HOUSE_FAILURE("Connector could not register transaction to clearing house"),
	CONNECTOR_CONTRACT_AGREEMENT_SUCCESS("Connector contract agreement uplaod success"),
	CONNECTOR_CONTRACT_AGREEMENT_FAILED("Connector contract agreement uplaod failed"),
	CONNECTOR_POLICY_ENFORCEMENT_SUCCESS("Connector successfuly enforces policy"),
	CONNECTOR_POLICY_ENFORCEMENT_FAILED("Connector failed to enforce policy"),
	CONNECTOR_BROKER_REGISTER("Connector register connector to Broker"),
	CONNECTOR_BROKER_UPDATE("Connector update Broker registration"),
	CONNECTOR_BROKER_PASSIVATE("Connector passivate connector on Broker"),
	CONNECTOR_BROKER_DELETE("Connector delete connector from Broker"),
	CONNECTOR_BROKER_QUERY("Connector query to Broker");
	
	private final String text;

	TrueConnectorEventType(final String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}
}
