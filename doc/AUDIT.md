# Audit events in TRUE Connector

Audit events are stored in database (H2 with default configuration, possible to replace with PostgreSQL), this way tampering of the logs is prohibited. Entries in database are done only by the Execution Core Container, and ECC exposes protected endpoint, for API user, to fetch all audit logs, or audit logs for specific date:

```
https://localhost:8090/api/audit/
```

or for specific date

```
https://localhost:8090/api/audit/?date=2024-02-12
```

NOTE: date format must be in YYYY-MM-DD format. Otherwise response will be https 400 - bad request.

## Audit event types

TRUE Connector has list of audit events which can be found in following table:

| TRUE Connector audit event type | Description |
| ----------- | ----------- |
|TRUE_CONNECTOR_EVENT | Default TRUE Connector event |
|HTTP_REQUEST_RECEIVED | Http request received |
|USER_AUTHORIZATION_FAILURE | Authorization failure |
|USER_AUTHORIZATION_SUCCESS | Authorization success |
|USER_AUTHENTICATION_FAILURE | Authentication failure |
|USER_AUTHENTICATION_SUCCESS | Authentication success |
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
| CONTRACT_OFFER | CONTRACT_OFFER, CONTRACT_OFFER_CREATED, CONTRACT_OFFER_CREATION_FAILED, CONTRACT_OFFER_UPDATED, CONTRACT_OFFER_UPDATE_FAILED, CONTRACT_OFFER_DELETED | Events related  with **Contract offer** manipulation (requested, created, updated, deleted) |
| OFFERED_RESOURCE | OFFERED_RESOURCE, OFFERED_RESOURCE_CREATED, OFFERED_RESOURCE_CREATION_FAILED, OFFERED_RESOURCE_UPDATED, OFFERED_RESOURCE_UPDATE_FAILED, OFFERED_RESOURCE_DELETED | Events related  with **Offered resource** manipulation (requested, created, updated, deleted) |
| REPRESENTATION | REPRESENTATION, REPRESENTATION_CREATED, REPRESENTATION_CREATION_FAILED, REPRESENTATION_UPDATED, REPRESENTATION_UPDATE_FAILED, REPRESENTATION_DELETED | Events related  with **Representation** manipulation (requested, created, updated, deleted) |
| CONNECTOR | CONNECTOR_REQUEST, CONNECTOR_RESPONSE, CONNECTOR_SEND, CONNECTOR_SEND_DATAAPP, CONNECTOR_TOKEN_FETCH_SUCCESS,CONNECTOR_TOKEN_FETCH_FAILURE , CONNECTOR_VALIDATED_TOKEN_SUCCESS, CONNECTOR_TOKEN_VALIDATED_FAILURE, CONNECTOR_CLEARING_HOUSE_SUCCESS,CONNECTOR_CLEARING_HOUSE_FAILURE , CONNECTOR_CONTRACT_AGREEMENT_SUCCESS, CONNECTOR_CONTRACT_AGREEMENT_FAILED, CONNECTOR_POLICY_ENFORCEMENT_SUCCESS, CONNECTOR_POLICY_ENFORCEMENT_FAILED, CONNECTOR_BROKER_REGISTER, CONNECTOR_BROKER_UPDATE, CONNECTOR_BROKER_PASSIVATE, CONNECTOR_BROKER_DELETE, CONNECTOR_BROKER_QUERY, CONNECTOR_INTERNAL_HEALTHY, CONNECTOR_INTERNAL_UNHEALTHY, CONNECTOR_EXTERNAL_HEALTHY, CONNECTOR_EXTERNAL_UNHEALTHY| Events related with message exchange process |
| USER | USER_AUTHORIZATION_FAILURE, USER_AUTHORIZATION_SUCCESS, USER_BLOCKED | User activity related events |
| EXCEPTION | EXCEPTION_BAD_REQUEST, EXCEPTION_NOT_FOUND, EXCEPTION_SERVER_ERROR, EXCEPTION_GENERAL | All events that are translated to invalid state (errors) |

## Example of Audit log entry

With default configuration, events will be logged in log file, in JSON format, like following:

```
[
    {
        "id": 1,
        "timestamp": "2024-02-12T11:02:34.567174",
        "event": "UsernamePasswordAuthenticationToken [Principal=User{id='7d330566-3c3b-4358-99aa-77fd09e6fec7', username='apiUser', password='[PROTECTED]', role='API_USER', accountNonExpired=true, accountNonLocked=true, credentialsNonExpired=true, enabled=true}, Credentials=[PROTECTED], Authenticated=true, Details=WebAuthenticationDetails [RemoteIpAddress=0:0:0:0:0:0:0:1, SessionId=null], Granted Authorities=[ROLE_API_USER]]"
    },
    {
        "id": 2,
        "timestamp": "2024-02-12T11:03:33.26411",
        "event": "UsernamePasswordAuthenticationToken [Principal=User{id='7d330566-3c3b-4358-99aa-77fd09e6fec7', username='apiUser', password='[PROTECTED]', role='API_USER', accountNonExpired=true, accountNonLocked=true, credentialsNonExpired=true, enabled=true}, Credentials=[PROTECTED], Authenticated=true, Details=WebAuthenticationDetails [RemoteIpAddress=0:0:0:0:0:0:0:1, SessionId=null], Granted Authorities=[ROLE_API_USER]]"
    },
    {
        "id": 3,
        "timestamp": "2024-02-12T11:04:47.443094",
        "event": "UsernamePasswordAuthenticationToken [Principal=User{id='7d330566-3c3b-4358-99aa-77fd09e6fec7', username='apiUser', password='[PROTECTED]', role='API_USER', accountNonExpired=true, accountNonLocked=true, credentialsNonExpired=true, enabled=true}, Credentials=[PROTECTED], Authenticated=true, Details=WebAuthenticationDetails [RemoteIpAddress=0:0:0:0:0:0:0:1, SessionId=null], Granted Authorities=[ROLE_API_USER]]"
    },
        {
        "id": 4,
        "timestamp": "2024-02-12T11:54:09.858569",
        "event": "AuditEvent [timestamp=2024-02-12T10:54:09.857568300Z, principal=idsUser, type=CONNECTOR_REQUEST, data={http.message=de.fraunhofer.iais.eis.ArtifactRequestMessageImpl, correlationId=ba5228e6-648c-44ad-aa85-a1ce0d8af809, http.method=POST}]"
    },
    {
        "id": 5,
        "timestamp": "2024-02-12T11:54:09.896071",
        "event": "AuditEvent [timestamp=2024-02-12T10:54:09.895003300Z, principal=idsUser, type=CONNECTOR_SEND, data={http.message=de.fraunhofer.iais.eis.ArtifactRequestMessageImpl, correlationId=ba5228e6-648c-44ad-aa85-a1ce0d8af809, http.method=POST}]"
    },
    {
        "id": 90,
        "timestamp": "2024-02-12T11:59:51.69915",
        "event": "AuditEvent [timestamp=2024-02-12T10:59:51.698151Z, principal=apiUser, type=HTTP_REQUEST_RECEIVED, data={http.headers={authorization=******, content-length=2968, resource=https://w3id.org/idsa/autogen/textResource/67ce1330-41fb-421a-8166-268746be5f17, host=localhost:8443, content-type=application/json, connection=keep-alive, accept-encoding=gzip, deflate, br, user-agent=PostmanRuntime/7.36.1, accept=*/*}, payload={\r\n    \"@context\": {\r\n        \"ids\": \"https://w3id.org/idsa/core/\",\r\n        \"idsc\": \"https://w3id.org/idsa/code/\"\r\n    },\r\n    \"@type\": \"ids:ContractOffer\",\r\n    \"@id\": \"https://w3id.org/idsa/autogen/contractOffer/a6cc0285-c948-48f2-9fa9-59bad3dbd825\",\r\n    \"ids:permission\": [\r\n        {\r\n            \"@type\": \"ids:Permission\",\r\n            \"@id\": \"https://w3id.org/idsa/autogen/permission/48047208-39df-4efe-881b-a2c444cf139a\",\r\n            \"ids:target\": {\r\n                \"@id\": \"http://w3id.org/engrd/connector/artifact/test\"\r\n            },\r\n            \"ids:description\": [\r\n                {\r\n                    \"@value\": \"provide-access\",\r\n                    \"@type\": \"http://www.w3.org/2001/XMLSchema#string\"\r\n                }\r\n            ],\r\n            \"ids:action\": [\r\n                {\r\n                    \"@id\": \"https://w3id.org/idsa/code/USE\"\r\n                }\r\n            ],\r\n            \"ids:title\": [\r\n                {\r\n                    \"@value\": \"Example Usage Policy\",\r\n                    \"@type\": \"http://www.w3.org/2001/XMLSchema#string\"\r\n                }\r\n            ],\r\n            \"ids:preDuty\": [],\r\n            \"ids:constraint\": [\r\n                {\r\n                    \"@type\": \"ids:Constraint\",\r\n                    \"@id\": \"https://w3id.org/idsa/autogen/constraint/f2cdadc7-3ac9-4eda-a7cf-c60822d53311\",\r\n                    \"ids:rightOperand\": {\r\n                        \"@value\": \"2024-02-12T10:59:51.570Z\",\r\n                        \"@type\": \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n                    },\r\n                    \"ids:leftOperand\": {\r\n                        \"@id\": \"https://w3id.org/idsa/code/POLICY_EVALUATION_TIME\"\r\n                    },\r\n                    \"ids:operator\": {\r\n                        \"@id\": \"https://w3id.org/idsa/code/AFTER\"\r\n                    }\r\n                },\r\n                {\r\n                    \"@type\": \"ids:Constraint\",\r\n                    \"@id\": \"https://w3id.org/idsa/autogen/constraint/79309e91-54eb-4325-aff7-3909d71626b3\",\r\n                    \"ids:rightOperand\": {\r\n                        \"@value\": \"2024-04-12T09:59:51.557Z\",\r\n                        \"@type\": \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n                    },\r\n                    \"ids:leftOperand\": {\r\n                        \"@id\": \"https://w3id.org/idsa/code/POLICY_EVALUATION_TIME\"\r\n                    },\r\n                    \"ids:operator\": {\r\n                        \"@id\": \"https://w3id.org/idsa/code/BEFORE\"\r\n                    }\r\n                }\r\n            ]\r\n        }\r\n    ],\r\n    \"ids:provider\": {\r\n        \"@id\": \"https://w3id.org/engrd/connector/\"\r\n    },\r\n    \"ids:contractStart\": {\r\n        \"@value\": \"2024-02-12T10:59:51.570Z\",\r\n        \"@type\": \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n    },\r\n    \"ids:contractDate\": {\r\n        \"@value\": \"2024-02-12T10:59:51.570Z\",\r\n        \"@type\": \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n    }\r\n}, http.path=https://localhost:8443/api/contractOffer/, correlationId=4cea31f9-e444-43f4-abc9-cb113a122b23, http.method=POST}]"
    },
    {
        "id": 91,
        "timestamp": "2024-02-12T11:59:51.753177",
        "event": "AuditEvent [timestamp=2024-02-12T10:59:51.751176500Z, principal=apiUser, type=CONTRACT_OFFER_CREATED, data={http.headers={authorization=******, content-length=2968, resource=https://w3id.org/idsa/autogen/textResource/67ce1330-41fb-421a-8166-268746be5f17, host=localhost:8443, content-type=application/json, connection=keep-alive, accept-encoding=gzip, deflate, br, user-agent=PostmanRuntime/7.36.1, accept=*/*}, http.path=https://localhost:8443/api/contractOffer/, correlationId=4cea31f9-e444-43f4-abc9-cb113a122b23, http.method=POST}]"
    }
```
