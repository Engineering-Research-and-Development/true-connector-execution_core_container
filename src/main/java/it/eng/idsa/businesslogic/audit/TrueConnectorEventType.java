package it.eng.idsa.businesslogic.audit;

public enum TrueConnectorEventType {

	TRUE_CONNECTOR_EVENT("Default TrueConnector event"),
	HTTP_REQUEST_RECEIVED("Http request received"),
	AUTHORIZATION_FAILURE("Authorization failure"),
	AUTHORIZATION_SUCCESS("Authorization success"),
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
	BAD_REQUEST("Bad request"),
	NOT_FOUND("Entity not found"),
	SERVER_ERROR("Server error"),
	/// Camel
	CONNECTOR("Connector event"), 
	CONNECTOR_REQUEST("Connector received message"), 
	CONNECTOR_RESPONSE("Connector received response"), 
	CONNECTOR_SEND("Connector message forwarding"),
	CONNECTOR_SEND_DATAAPP("Connector message forwarding - dataApp"),
	CONNECTOR_FETCH_TOKEN("Connector obtained DAT token"),
	CONNECTOR_VALIDATED_TOKEN_SUCCESS("Connector validated DAT token successfully"),
	CONNECTOR_VALIDATED_TOKEN_FAIL("Connector failed to validate DAT token"),
	CONNECTOR_CLEARING_HOUSE("Connector registers transaction to clearing house"),
	CONNECTOR_CONTRACT_AGREEMENT_SUCCESS("Connector contract agreement upload success"),
	CONNECTOR_CONTRACT_AGREEMENT_FAILED("Connector contract agreement upload failed"),
	CONNECTOR_POLICY_ENFORCEMENT_SUCCESS("Connector successfully enforces policy"),
	CONNECTOR_POLICY_ENFORCEMENT_FAILED("Connector failed to enforce policy"),
	CONNECTOR_BROKER_REGISTER("Connector register connector to Broker"),
	CONNECTOR_BROKER_UPDATE("Connector update Broker registration"),
	CONNECTOR_BROKER_UNREGISTER("Connector clearing house");
	
	private final String text;

	TrueConnectorEventType(final String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}
}
