package it.eng.idsa.businesslogic.audit;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@SuppressWarnings("serial")
public class EventTypeHandlerTest {
	
	@ParameterizedTest
	@EnumSource(TrueConnectorEventType.class)
	public void noAuditEventsSelected(TrueConnectorEventType eventType) {
		EventTypeHandler eventTypeHandler = new EventTypeHandler(new ArrayList<>() {{
			add("NONE");
		}});
		
		eventTypeHandler.configure();
		
		assertFalse(eventTypeHandler.shouldAuditEvent(eventType));
	}
	
	@ParameterizedTest
	@EnumSource(TrueConnectorEventType.class)
	public void allAuditEventsSelected(TrueConnectorEventType eventType) {
		EventTypeHandler eventTypeHandler = new EventTypeHandler(new ArrayList<>() {{
			add("ALL");
		}});
		
		eventTypeHandler.configure();
		
		assertTrue(eventTypeHandler.shouldAuditEvent(eventType));
	}
	
	@Test
	public void selfDescriptionAuditEventsSelected() {
		EventTypeHandler eventTypeHandler = new EventTypeHandler(new ArrayList<>() {{
			add("SELF_DESCRIPTION");
		}});
		
		eventTypeHandler.configure();
		
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.USER_AUTHORIZATION_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.USER_AUTHORIZATION_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.USER_BLOCKED));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.SELF_DESCRIPTION));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER_CREATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER_UPDATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER_DELETED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE_CREATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE_UPDATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE_DELETED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION_CREATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION_UPDATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION_DELETED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.EXCEPTION_BAD_REQUEST));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.EXCEPTION_NOT_FOUND));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_REGISTER));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_PASSIVATE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_UPDATE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_DELETE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_QUERY));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CLEARING_HOUSE_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CLEARING_HOUSE_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CONTRACT_AGREEMENT_FAILED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CONTRACT_AGREEMENT_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_FETCH_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_FETCH_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_POLICY_ENFORCEMENT_FAILED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_POLICY_ENFORCEMENT_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_REQUEST));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_RESPONSE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_SEND));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_SEND_DATAAPP));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_VALIDATED_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_VALIDATED_SUCCESS));
	}
	
	@Test
	public void selfDescriptionAllAuditEventsSelected() {
		EventTypeHandler eventTypeHandler = new EventTypeHandler(new ArrayList<>() {{
			add("SELF_DESCRIPTION_ALL");
		}});
		
		eventTypeHandler.configure();
		
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.USER_AUTHORIZATION_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.USER_AUTHORIZATION_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.USER_BLOCKED));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.SELF_DESCRIPTION));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER_CREATED));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER_UPDATED));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER_DELETED));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE_CREATED));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE_UPDATED));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE_DELETED));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION_CREATED));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION_UPDATED));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION_DELETED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.EXCEPTION_BAD_REQUEST));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.EXCEPTION_NOT_FOUND));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.EXCEPTION_GENERAL));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_REGISTER));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_PASSIVATE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_UPDATE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_DELETE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_QUERY));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CLEARING_HOUSE_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CLEARING_HOUSE_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CONTRACT_AGREEMENT_FAILED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CONTRACT_AGREEMENT_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_FETCH_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_FETCH_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_POLICY_ENFORCEMENT_FAILED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_POLICY_ENFORCEMENT_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_REQUEST));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_RESPONSE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_SEND));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_SEND_DATAAPP));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_VALIDATED_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_VALIDATED_SUCCESS));
	}
	
	@Test
	public void representationAuditEventsSelected() {
		EventTypeHandler eventTypeHandler = new EventTypeHandler(new ArrayList<>() {{
			add("REPRESENTATION");
		}});
		
		eventTypeHandler.configure();
		
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.USER_AUTHORIZATION_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.USER_AUTHORIZATION_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.USER_BLOCKED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.SELF_DESCRIPTION));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER_CREATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER_UPDATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER_DELETED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE_CREATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE_UPDATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE_DELETED));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION_CREATED));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION_UPDATED));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION_DELETED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.EXCEPTION_BAD_REQUEST));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.EXCEPTION_NOT_FOUND));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.EXCEPTION_GENERAL));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_REGISTER));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_PASSIVATE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_UPDATE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_DELETE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_QUERY));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CLEARING_HOUSE_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CLEARING_HOUSE_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CONTRACT_AGREEMENT_FAILED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CONTRACT_AGREEMENT_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_FETCH_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_FETCH_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_POLICY_ENFORCEMENT_FAILED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_POLICY_ENFORCEMENT_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_REQUEST));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_RESPONSE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_SEND));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_SEND_DATAAPP));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_VALIDATED_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_VALIDATED_SUCCESS));
	}
	
	@Test
	public void contractOfferAuditEventsSelected() {
		EventTypeHandler eventTypeHandler = new EventTypeHandler(new ArrayList<>() {{
			add("CONTRACT_OFFER");
		}});
		
		eventTypeHandler.configure();
		
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.USER_AUTHORIZATION_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.USER_AUTHORIZATION_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.USER_BLOCKED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.SELF_DESCRIPTION));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER_CREATED));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER_UPDATED));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER_DELETED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE_CREATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE_UPDATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE_DELETED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION_CREATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION_UPDATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION_DELETED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.EXCEPTION_BAD_REQUEST));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.EXCEPTION_NOT_FOUND));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.EXCEPTION_GENERAL));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_REGISTER));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_PASSIVATE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_UPDATE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_DELETE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_QUERY));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CLEARING_HOUSE_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CLEARING_HOUSE_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CONTRACT_AGREEMENT_FAILED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CONTRACT_AGREEMENT_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_FETCH_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_FETCH_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_POLICY_ENFORCEMENT_FAILED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_POLICY_ENFORCEMENT_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_REQUEST));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_RESPONSE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_SEND));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_SEND_DATAAPP));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_VALIDATED_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_VALIDATED_SUCCESS));
	}
	
	@Test
	public void offeredResourceAuditEventsSelected() {
		EventTypeHandler eventTypeHandler = new EventTypeHandler(new ArrayList<>() {{
			add("OFFERED_RESOURCE");
		}});
		
		eventTypeHandler.configure();
		
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.USER_AUTHORIZATION_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.USER_AUTHORIZATION_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.USER_BLOCKED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.SELF_DESCRIPTION));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER_CREATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER_UPDATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER_DELETED));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE_CREATED));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE_UPDATED));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE_DELETED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION_CREATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION_UPDATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION_DELETED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.EXCEPTION_BAD_REQUEST));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.EXCEPTION_NOT_FOUND));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.EXCEPTION_GENERAL));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_REGISTER));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_PASSIVATE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_UPDATE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_DELETE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_QUERY));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CLEARING_HOUSE_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CLEARING_HOUSE_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CONTRACT_AGREEMENT_FAILED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CONTRACT_AGREEMENT_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_FETCH_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_FETCH_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_POLICY_ENFORCEMENT_FAILED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_POLICY_ENFORCEMENT_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_REQUEST));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_RESPONSE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_SEND));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_SEND_DATAAPP));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_VALIDATED_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_VALIDATED_SUCCESS));
	}
	
	@Test
	public void userAuditEventsSelected() {
		EventTypeHandler eventTypeHandler = new EventTypeHandler(new ArrayList<>() {{
			add("USER");
		}});
		
		eventTypeHandler.configure();
		
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.USER_AUTHORIZATION_FAILURE));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.USER_AUTHORIZATION_SUCCESS));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.USER_BLOCKED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.SELF_DESCRIPTION));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER_CREATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER_UPDATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER_DELETED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE_CREATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE_UPDATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE_DELETED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION_CREATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION_UPDATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION_DELETED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.EXCEPTION_BAD_REQUEST));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.EXCEPTION_NOT_FOUND));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.EXCEPTION_GENERAL));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_REGISTER));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_PASSIVATE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_UPDATE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_DELETE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_QUERY));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CLEARING_HOUSE_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CLEARING_HOUSE_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CONTRACT_AGREEMENT_FAILED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CONTRACT_AGREEMENT_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_FETCH_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_FETCH_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_POLICY_ENFORCEMENT_FAILED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_POLICY_ENFORCEMENT_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_REQUEST));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_RESPONSE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_SEND));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_SEND_DATAAPP));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_VALIDATED_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_VALIDATED_SUCCESS));
	}
	
	@Test
	public void exceptionAuditEventsSelected() {
		EventTypeHandler eventTypeHandler = new EventTypeHandler(new ArrayList<>() {{
			add("EXCEPTION");
		}});
		
		eventTypeHandler.configure();
		
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.USER_AUTHORIZATION_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.USER_AUTHORIZATION_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.USER_BLOCKED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.SELF_DESCRIPTION));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER_CREATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER_UPDATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER_DELETED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE_CREATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE_UPDATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE_DELETED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION_CREATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION_UPDATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION_DELETED));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.EXCEPTION_BAD_REQUEST));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.EXCEPTION_NOT_FOUND));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.EXCEPTION_GENERAL));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_REGISTER));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_PASSIVATE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_UPDATE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_DELETE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_QUERY));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CLEARING_HOUSE_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CLEARING_HOUSE_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CONTRACT_AGREEMENT_FAILED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CONTRACT_AGREEMENT_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_FETCH_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_FETCH_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_POLICY_ENFORCEMENT_FAILED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_POLICY_ENFORCEMENT_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_REQUEST));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_RESPONSE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_SEND));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_SEND_DATAAPP));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_VALIDATED_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_VALIDATED_SUCCESS));
	}
	
	@Test
	public void connectorAuditEventsSelected() {
		EventTypeHandler eventTypeHandler = new EventTypeHandler(new ArrayList<>() {{
			add("CONNECTOR");
		}});
		
		eventTypeHandler.configure();
		
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.USER_AUTHORIZATION_FAILURE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.USER_AUTHORIZATION_SUCCESS));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.USER_BLOCKED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.SELF_DESCRIPTION));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER_CREATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER_UPDATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONTRACT_OFFER_DELETED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE_CREATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE_UPDATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.OFFERED_RESOURCE_DELETED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION_CREATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION_UPDATED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.REPRESENTATION_DELETED));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.EXCEPTION_BAD_REQUEST));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.EXCEPTION_NOT_FOUND));
		assertFalse(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.EXCEPTION_GENERAL));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_REGISTER));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_PASSIVATE));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_UPDATE));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_DELETE));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_BROKER_QUERY));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CLEARING_HOUSE_SUCCESS));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CLEARING_HOUSE_FAILURE));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CONTRACT_AGREEMENT_FAILED));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_CONTRACT_AGREEMENT_SUCCESS));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_FETCH_SUCCESS));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_FETCH_FAILURE));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_POLICY_ENFORCEMENT_FAILED));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_POLICY_ENFORCEMENT_SUCCESS));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_REQUEST));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_RESPONSE));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_SEND));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_SEND_DATAAPP));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_VALIDATED_FAILURE));
		assertTrue(eventTypeHandler.shouldAuditEvent(TrueConnectorEventType.CONNECTOR_TOKEN_VALIDATED_SUCCESS));
	}
	
}
