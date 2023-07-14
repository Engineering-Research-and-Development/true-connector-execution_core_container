# Audit events in TRUE Connector

## Audit event types

TRUE Connector has list of audit events which can be found in following table:

| TRUE Connector audit event type | Description |
| ----------- | ----------- |
|TRUE_CONNECTOR_EVENT | Default TRUE Connector event |
|HTTP_REQUEST_RECEIVED | Http request received |
|USER_AUTHORIZATION_FAILURE | Authorization failure |
|USER_AUTHORIZATION_SUCCESS | Authorization success |
|USER_BLOCKED | User blocked |
|SELF_DESCRIPTION | Self description requested |
|CONTRACT_OFFER | Contract offer requested |
|CONTRACT_OFFER_CREATED | Contract offer created |
|CONTRACT_OFFER_CREATION_FAILED | Contract offer creation failed |
|CONTRACT_OFFER_UPDATED | Contract offer updated |
|CONTRACT_OFFER_UPDATE_FAILED | Contract offer update failed |
|CONTRACT_OFFER_DELETED | Contract offer deleted |
|OFFERED_RESOURCE | Offered resource requested |
|OFFERED_RESOURCE_CREATED | Offered resource created |
|OFFERED_RESOURCE_CREATION_FAILED | Offered resource creation failed |
|OFFERED_RESOURCE_UPDATED | Offered resource updated |
|OFFERED_RESOURCE_UPDATE_FAILED | Offered resource update failed |
|OFFERED_RESOURCE_UPDATED | Offered resource updated |
|OFFERED_RESOURCE_DELETED | Offered resource deleted |
|REPRESENTATION | Representation requested |
|REPRESENTATION_CREATED | Representation created |
|REPRESENTATION_CREATION_FAILED | Representation creation failed |
|REPRESENTATION_UPDATED | Representation updated |
|REPRESENTATION_UPDATE_FAILED | Representation update failed |
|REPRESENTATION_DELETED | Representation deleted |
|EXCEPTION_BAD_REQUEST | Bad request |
|EXCEPTION_NOT_FOUND | Entity not found |
|EXCEPTION_SERVER_ERROR | Server error |
|EXCEPTION_GENERAL | General exception |
|CONNECTOR | Connector event | 
|CONNECTOR_REQUEST | Connector received message | 
|CONNECTOR_RESPONSE | Connector received response | 
|CONNECTOR_SEND | Connector message forwarding |
|CONNECTOR_SEND_DATAAPP | Connector message forwarding - dataApp |
|CONNECTOR_TOKEN_FETCH_SUCCESS | Connector obtained DAT token|
|CONNECTOR_TOKEN_FETCH_FAILURE | Connector could not obtain DAT token|
|CONNECTOR_VALIDATED_TOKEN_SUCCESS | Connector validated DAT token successfully |
|CONNECTOR_VALIDATED_TOKEN_FAIL | Connector failed to validate DAT token |
|CONNECTOR_CLEARING_HOUSE_SUCCESS | Connector registered transaction to clearing house|
|CONNECTOR_CLEARING_HOUSE_FAILURE | Connector could not register transaction to clearing house|
|CONNECTOR_CONTRACT_AGREEMENT_SUCCESS | Connector contract agreement upload success |
|CONNECTOR_CONTRACT_AGREEMENT_FAILED | Connector contract agreement upload failed |
|CONNECTOR_POLICY_ENFORCEMENT_SUCCESS | Connector successfully enforces policy |
|CONNECTOR_POLICY_ENFORCEMENT_FAILED | Connector failed to enforce policy |
|CONNECTOR_BROKER_REGISTER | Connector register connector to Broker |
|CONNECTOR_BROKER_UPDATE | Connector update Broker registration |
|CONNECTOR_BROKER_PASSIVATE | Connector passivate connector on Broker |
|CONNECTOR_BROKER_DELETE | Connector delete connector from Broker |
|CONNECTOR_BROKER_QUERY | Connector query to Broker |
|CONNECTOR_INTERNAL_HEALTHY | Connector internal state is healthy|
|CONNECTOR_INTERNAL_UNHEALTHY |Connector internal state is unhealthy|
|CONNECTOR_EXTERNAL_HEALTHY |Connector external state is healthy|
|CONNECTOR_EXTERNAL_UNHEALTHY |Connector external state is unhealthy|

## Audit event configuration

User can configure TRUE Connector to include or exclude some events to be logged or not. This can be done with following property in application.property file:

```
auditableEvents=ALL,NONE,SELF_DESCRIPTION,SELF_DESCRIPTION_ALL,CONTRACT_OFFER,OFFERED_RESOURCE,REPRESENTATION,USER,EXCEPTION,CONNECTOR
```

User has possibility to turn on all audit events, by setting property to *ALL*, or to turn off all events by setting the property to *NONE*, or make combination to turn on only specific events, by setting up correct property.

Here is the binding of properties and which events are covered with it:

| Audit property | TRUE Connector audit event type | Note |
| ----------- | ----------- | ----------- |  
| SELF_DESCRIPTION | SELF_DESCRIPTION | Event when connector's Self description is requested |
| SELF_DESCRIPTION_ALL | SELF_DESCRIPTION, CONTRACT_OFFER, OFFERED_RESOURCE, OFFERED_RESOURCE, REPRESENTATION | All events related with Self Description activities, include events wrapped up with properties from the list|
| CONTRACT_OFFER | CONTRACT_OFFER,CONTRACT_OFFER_CREATED, CONTRACT_OFFER_UPDATED,CONTRACT_OFFER_DELETED | Events related  with **Contract offer** manipulation (requested, created, updated, deleted) |
| OFFERED_RESOURCE | OFFERED_RESOURCE, OFFERED_RESOURCE_CREATED, OFFERED_RESOURCE_UPDATED, OFFERED_RESOURCE_DELETED | Events related  with **Offered resource** manipulation (requested, created, updated, deleted) |
| REPRESENTATION | REPRESENTATION, REPRESENTATION_CREATED, REPRESENTATION_UPDATED, REPRESENTATION_DELETED | Events related  with **Representation** manipulation (requested, created, updated, deleted) |
| CONNECTOR | CONNECTOR_REQUEST, CONNECTOR_RESPONSE, CONNECTOR_SEND, CONNECTOR_SEND_DATAAPP, CONNECTOR_TOKEN_FETCH_SUCCESS,CONNECTOR_TOKEN_FETCH_FAILURE , CONNECTOR_VALIDATED_TOKEN_SUCCESS, CONNECTOR_TOKEN_VALIDATED_FAILURE, CONNECTOR_CLEARING_HOUSE_SUCCESS,CONNECTOR_CLEARING_HOUSE_FAILURE , CONNECTOR_CONTRACT_AGREEMENT_SUCCESS, CONNECTOR_CONTRACT_AGREEMENT_FAILED, CONNECTOR_POLICY_ENFORCEMENT_SUCCESS, CONNECTOR_POLICY_ENFORCEMENT_FAILED, CONNECTOR_BROKER_REGISTER, CONNECTOR_BROKER_UPDATE, CONNECTOR_BROKER_PASSIVATE, CONNECTOR_BROKER_DELETE, CONNECTOR_BROKER_QUERY, CONNECTOR_INTERNAL_HEALTHY, CONNECTOR_INTERNAL_UNHEALTHY, CONNECTOR_EXTERNAL_HEALTHY, CONNECTOR_EXTERNAL_UNHEALTHY| Events related with message exchange process |
| USER | USER_AUTHORIZATION_FAILURE, USER_AUTHORIZATION_SUCCESS, USER_BLOCKED | User activity related events |
| EXCEPTION | EXCEPTION_BAD_REQUEST, EXCEPTION_NOT_FOUND, EXCEPTION_SERVER_ERROR, EXCEPTION_GENERAL | All events that are translated to invalid state (errors) |

## Example of Audit log entry

With default configuration, events will be logged in log file, in JSON format, like following:

```
{
	"@timestamp": "2022-10-18T10:54:23.317+02:00",
	"@version": "1",
	"message": "TRUE Connector Audit Event was received",
	"logger_name": "JSON",
	"thread_name": "task-1",
	"level": "INFO",
	"level_value": 20000,
	"connectorRole": "Sender",
	"event.type": "CONNECTOR_REQUEST",
	"event.principal": "idsUser",
	"event": {
		"timestamp": 1666083263.293266,
		"principal": "idsUser",
		"type": "CONNECTOR_REQUEST",
		"data": {
			"http.message": "de.fraunhofer.iais.eis.ArtifactRequestMessageImpl",
			"http.method": "POST"
		}
	}
}
{
	"@timestamp": "2022-10-18T10:54:23.473+02:00",
	"@version": "1",
	"message": "TRUE Connector Audit Event was received",
	"logger_name": "JSON",
	"thread_name": "task-2",
	"level": "INFO",
	"level_value": 20000,
	"connectorRole": "Sender",
	"event.type": "CONNECTOR_FETCH_TOKEN",
	"event.principal": "idsUser",
	"event": {
		"timestamp": 1666083263.473887,
		"principal": "idsUser",
		"type": "CONNECTOR_FETCH_TOKEN",
		"data": {
			"http.message": "de.fraunhofer.iais.eis.ArtifactRequestMessageImpl",
			"http.method": "POST"
		}
	}
}

```

User can modify default logging configuration and change logger to output data to ELK stack (Elasticsearch, Logstash, and Kibana)

// TODO - how to configure logback to log to ELK
