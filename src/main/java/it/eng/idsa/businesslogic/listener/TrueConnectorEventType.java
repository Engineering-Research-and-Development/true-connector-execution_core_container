package it.eng.idsa.businesslogic.listener;

public enum TrueConnectorEventType {

	HTTP_REQUEST_RECEIVED("Http request received"),
	AUTHORIZATION_FAILURE("Authorization failure"),
	AUTHORIZATION_SUCCESS("Authorization success"),
	USER_BLOCEKD("User blocked"),
	CONTRACT_OFFER_CREATED("Contract offer created"),
	CONTRACT_OFFER_UPDATED("Contract offer updated"),
	CONTRACT_OFFER_DELETED("Contract offer deleted"),
	OFFERED_RESOURCE_CREATED("Offered resource created"),
	OFFERED_RESOURCE_UPDATED("Offered resource updated"),
	OFFERED_RESOURCE_DELETED("Offered resource deleted"),
	REPRESENTATION_CREATED("Representation created"),
	REPRESENTATION_UPDATED("Representation updated"),
	REPRESENTATION_DELETED("Representation deleted"),
	BAD_REQUEST("Bad request"),
	NOT_FOUND("Entity not found");
	
	private final String text;

	TrueConnectorEventType(final String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}
}
