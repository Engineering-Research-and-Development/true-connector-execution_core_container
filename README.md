# ENG Execution Core Container

[![License: AGPL](https://img.shields.io/github/license/Engineering-Research-and-Development/true-connector-execution_core_container.svg)](https://opensource.org/licenses/AGPL-3.0)
[![CI](https://github.com/Engineering-Research-and-Development/true-connector-execution_core_container/workflows/ECC/badge.svg)](https://github.com/Engineering-Research-and-Development/true-connector-execution_core_container/actions/workflows/ECC.yml) 
[![Docker badge](https://img.shields.io/docker/pulls/rdlabengpa/ids_execution_core_container.svg)](https://hub.docker.com/r/rdlabengpa/ids_execution_core_container)
<br/>

The ENG Execution Core Container, based on the IDS Base Connector, is the core component of an IDS Connector enabling:
* the data exchange between connectors, using HTTPS, WS over HTTPS, IDSCP2 (beta)
* interaction with the AISEC Fraunhofer DAPS Service for requiring and validating a token
* communication with the ENG Clearing House for registering transactions

![Execution Core Container Architecture](doc/connector_schema_v1.1.PNG?raw=true "ENG Execution Core Container Architecture")

## How to Configure and Run

The configuration should be performed customizing the following variables in the **docker-compose** file:
* **DATA_APP_ENDPOINT=192.168.56.1:8083/incoming-data-app/dataAppIncomingMessageReceiver** DataAPP endpoint for receiveing data (F endpoint in the above picture)
* **MULTIPART=mixed** DataAPP endpoint Content Type (choose mixed for Multipart/mixed or form for Multipart/form-data) 
* Edit external port if need (default values: **8086** for **web sockets over HTTPS**, **8090** for **http**, **8887** for **A endpoint** and  **8889** for **B endpoint**)
* Forward-To protocol validation can be enabled by setting the property **application.enableProtocolValidation** to *true*. If you have this enabled please refer to the following step.
* Forward-To protocol validation can be changed by editing **application.validateProtocol**. Default value is *true* and Forward-To URL must be set like http(https,wss)://example.com, if you choose *false* Forward-To URL can be set like http(https,wss)://example.com or just example.com and the protocol chosen (from application.properties)will be automatically set (it will be overwritten! example: http://example.com will be wss://example if you chose wss in the properties). 

If you want to use your own certificate for the AISEC Fraunhofer DAPS server: 
* Put **DAPS certificates** into the cert folder and edit related settings (i.e., *application.keyStoreName*, *application.keyStorePassword*) into the *resources/application.properties* file

Finally, run the application:

*  Execute `docker-compose up`


## Endpoints
The Execution Core Container will use two ports (http and https) as described by the Docker Compose File.
It will expose the following endpoints (both over https):

```
/incoming-data-app/multipartMessageBodyBinary to receive data (MultiPartMessage) with binary body from Data App (the A endpoint in the above picture)
/incoming-data-app/multipartMessageBodyFormData to receive data (MultiPartMessage) with form-data body from Data App (the A endpoint in the above picture)
/incoming-data-app/multipartMessageHttpHeader to receive data (MultiPartMessage) represented with IDS-Message using http headers and payload in body from Data App (the A endpoint in the above picture)
/data to receive data (IDS Message) from a sender connector (the B endpoint in the above picture)
```

Furthermore, just for testing it will expose (http and https):

```
/about/version returns business logic version 
```

Encode plain text password (used to get new hashed password for SelfDescription API). This endpoint is password protected, so you will have to use default credentials to get new password, using this endpoint, and then replace default password.

```
/notification/password/{password}
```

## Configuration
The ECC supports three different way to exchange data:
*  **REST endpoints** enabled if *IDSCP2=false* and *WS_OVER_HTTPS=false*
*  **IDSCP2** enabled if *IDSCP2=true* and *WS_INTERNAL=false* (use https on the edge) or *IDSCP2=true* and *WS_INTERNAL=true* (use WS on the edge)
*  **Web Socket over HTTPS** enabled if *WS_OVER_HTTPS=true* and *IDSCP2=false*

## How to Test
The reachability could be verified using the following endpoints:
*  **http://{IP_ADDRESS}:{HTTP_PUBLIC_PORT}/about/version**

Keeping the provided docker-compose will be:
*  **http://{IP_ADDRESS}:8090/about/version**


The sender DataApp should send a request using the following schema, specifying in the Forward-To header the destination connector URL:

## How to Exchange Data
### REST endpoints
#### Multipart/mixed - Example 

```
curl --location --request POST 'https://{IPADDRESS}:{A_ENDPOINT_PUBLIC_PORT}/incoming-data-app/multipartMessageBodyBinary' \
--header 'Content-Type: multipart/mixed; boundary=CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6' \
--header 'Forward-To: {RECEIVER_IP_ADDRESS}:{B_ENDPOINT_PUBLIC_PORT}/data' 
--data-raw ' --CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6
Content-Disposition: form-data; name="header"
Content-Type: application/json; charset=UTF-8
Content-Length: 1293
   {
  "@context" : {
    "ids" : "https://w3id.org/idsa/core/",
    "idsc" : "https://w3id.org/idsa/code/"
  },
  "@type" : "ids:ArtifactRequestMessage",
  "@id" : "https://w3id.org/idsa/autogen/artifactRequestMessage/36cdbc3c-993d-4efe-a9bd-a88f400ff3f6",
  "ids:transferContract" : {
    "@id" : "http://w3id.org/engrd/connector/examplecontract"
  },
  "ids:correlationMessage" : {
    "@id" : "http://w3id.org/artifactRequestMessage/1a421b8c-3407-44a8-aeb9-253f145c869a"
  },
  "ids:securityToken" : {
    "@type" : "ids:DynamicAttributeToken",
    "@id" : "https://w3id.org/idsa/autogen/dynamicAttributeToken/4f81873e-ca33-47ea-b777-b5485fc53253",
    "ids:tokenValue" : "DummyTokenValue",
    "ids:tokenFormat" : {
      "@id" : "https://w3id.org/idsa/code/JWT"
    }
  },
  "ids:modelVersion" : "4.1.0",
  "ids:issued" : {
    "@value" : "2021-11-24T15:09:01.276+01:00",
    "@type" : "http://www.w3.org/2001/XMLSchema#dateTimeStamp"
  },
  "ids:issuerConnector" : {
    "@id" : "http://w3id.org/engrd/connector"
  },
  "ids:senderAgent" : {
    "@id" : "http://sender.agent/sender"
  },
  "ids:recipientAgent" : [ ],
  "ids:requestedArtifact" : {
    "@id" : "http://w3id.org/engrd/connector/artifact/test1.csv"
  },
  "ids:recipientConnector" : [ ]
}

--CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6
Content-Disposition: form-data; name="payload"
Content-Type: application/json
Content-Length: 50
{"catalog.offers.0.resourceEndpoints.path":"/pet2"}

--CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6--'
```

Keeping the provided configuration:

<details>
  <summary>Multipart mixed request</summary>

```
curl --location --request POST 'https://localhost:8887/incoming-data-app/multipartMessageBodyBinary' \
--header 'Content-Type: multipart/mixed; boundary=CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6' \
--header 'Forward-To: https://localhost:8889/data' 
--data-raw ' --CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6
Content-Disposition: form-data; name="header"
Content-Type: application/json; charset=UTF-8
Content-Length: 1293
   {
  "@context" : {
    "ids" : "https://w3id.org/idsa/core/",
    "idsc" : "https://w3id.org/idsa/code/"
  },
  "@type" : "ids:ArtifactRequestMessage",
  "@id" : "https://w3id.org/idsa/autogen/artifactRequestMessage/36cdbc3c-993d-4efe-a9bd-a88f400ff3f6",
  "ids:transferContract" : {
    "@id" : "http://w3id.org/engrd/connector/examplecontract"
  },
  "ids:correlationMessage" : {
    "@id" : "http://w3id.org/artifactRequestMessage/1a421b8c-3407-44a8-aeb9-253f145c869a"
  },
  "ids:securityToken" : {
    "@type" : "ids:DynamicAttributeToken",
    "@id" : "https://w3id.org/idsa/autogen/dynamicAttributeToken/4f81873e-ca33-47ea-b777-b5485fc53253",
    "ids:tokenValue" : "DummyTokenValue",
    "ids:tokenFormat" : {
      "@id" : "https://w3id.org/idsa/code/JWT"
    }
  },
  "ids:modelVersion" : "4.1.0",
  "ids:issued" : {
    "@value" : "2021-11-24T15:09:01.276+01:00",
    "@type" : "http://www.w3.org/2001/XMLSchema#dateTimeStamp"
  },
  "ids:issuerConnector" : {
    "@id" : "http://w3id.org/engrd/connector"
  },
  "ids:senderAgent" : {
    "@id" : "http://sender.agent/sender"
  },
  "ids:recipientAgent" : [ ],
  "ids:requestedArtifact" : {
    "@id" : "http://w3id.org/engrd/connector/artifact/test1.csv"
  },
  "ids:recipientConnector" : [ ]
}

--CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6
Content-Disposition: form-data; name="payload"
Content-Type: application/json
Content-Length: 50
{"catalog.offers.0.resourceEndpoints.path":"/pet2"}

--CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6--'
```

</details>

#### Multipart/form-data - Example

```
curl --location --request POST 'https://{IPADDRESS}:{A_ENDPOINT_PUBLIC_PORT}/incoming-data-app/multipartMessageBodyFormData' \
--header 'Content-Type: multipart/mixed; boundary=CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6' \
--header 'Forward-To: {RECEIVER_IP_ADDRESS}:{B_ENDPOINT_PUBLIC_PORT}/data' \
--form 'header="{
  \"@context\" : {
    \"ids\" : \"https://w3id.org/idsa/core/\",
    \"idsc\" : \"https://w3id.org/idsa/code/\"
  },
  \"@type\" : \"ids:ArtifactRequestMessage\",
  \"@id\" : \"https://w3id.org/idsa/autogen/artifactRequestMessage/36cdbc3c-993d-4efe-a9bd-a88f400ff3f6\",
  \"ids:transferContract\" : {
    \"@id\" : \"http://w3id.org/engrd/connector/examplecontract\"
  },
  \"ids:correlationMessage\" : {
    \"@id\" : \"http://w3id.org/artifactRequestMessage/1a421b8c-3407-44a8-aeb9-253f145c869a\"
  },
  \"ids:securityToken\" : {
    \"@type\" : \"ids:DynamicAttributeToken\",
    \"@id\" : \"https://w3id.org/idsa/autogen/dynamicAttributeToken/4f81873e-ca33-47ea-b777-b5485fc53253\",
    \"ids:tokenValue\" : \"DummyTokenValue\",
    \"ids:tokenFormat\" : {
      \"@id\" : \"https://w3id.org/idsa/code/JWT\"
    }
  },
  \"ids:modelVersion\" : \"4.1.0\",
  \"ids:issued\" : {
    \"@value\" : \"2021-11-24T15:09:01.276+01:00\",
    \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"
  },
  \"ids:issuerConnector\" : {
    \"@id\" : \"http://w3id.org/engrd/connector\"
  },
  \"ids:senderAgent\" : {
    \"@id\" : \"http://sender.agent/sender\"
  },
  \"ids:recipientAgent\" : [ ],
  \"ids:requestedArtifact\" : {
    \"@id\" : \"http://w3id.org/engrd/connector/artifact/test1.csv\"
  },
  \"ids:recipientConnector\" : [ ]
}"' \
--form 'payload="{\"catalog.offers.0.resourceEndpoints.path\":\"/pet2\"}";type=application/json; charset=UTF-8'
```


Keeping the provided configuration:

<details>
  <summary>Multipart form request</summary>

```
curl --location --request POST 'https://localhost:8887/incoming-data-app/multipartMessageBodyFormData' \
--header 'Content-Type: multipart/mixed; boundary=CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6' \
--header 'Forward-To: https://localhost:8889/data' \
--form 'header="{
  \"@context\" : {
    \"ids\" : \"https://w3id.org/idsa/core/\",
    \"idsc\" : \"https://w3id.org/idsa/code/\"
  },
  \"@type\" : \"ids:ArtifactRequestMessage\",
  \"@id\" : \"https://w3id.org/idsa/autogen/artifactRequestMessage/36cdbc3c-993d-4efe-a9bd-a88f400ff3f6\",
  \"ids:transferContract\" : {
    \"@id\" : \"http://w3id.org/engrd/connector/examplecontract\"
  },
  \"ids:correlationMessage\" : {
    \"@id\" : \"http://w3id.org/artifactRequestMessage/1a421b8c-3407-44a8-aeb9-253f145c869a\"
  },
  \"ids:securityToken\" : {
    \"@type\" : \"ids:DynamicAttributeToken\",
    \"@id\" : \"https://w3id.org/idsa/autogen/dynamicAttributeToken/4f81873e-ca33-47ea-b777-b5485fc53253\",
    \"ids:tokenValue\" : \"DummyTokenValue\",
    \"ids:tokenFormat\" : {
      \"@id\" : \"https://w3id.org/idsa/code/JWT\"
    }
  },
  \"ids:modelVersion\" : \"4.1.0\",
  \"ids:issued\" : {
    \"@value\" : \"2021-11-24T15:09:01.276+01:00\",
    \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"
  },
  \"ids:issuerConnector\" : {
    \"@id\" : \"http://w3id.org/engrd/connector\"
  },
  \"ids:senderAgent\" : {
    \"@id\" : \"http://sender.agent/sender\"
  },
  \"ids:recipientAgent\" : [ ],
  \"ids:requestedArtifact\" : {
    \"@id\" : \"http://w3id.org/engrd/connector/artifact/test1.csv\"
  },
  \"ids:recipientConnector\" : [ ]
}"' \
--form 'payload="{\"catalog.offers.0.resourceEndpoints.path\":\"/pet2\"}";type=application/json; charset=UTF-8'
```
</details>

#### HTTP-Header - Example

```
curl --location --request POST 'https://{IPADDRESS}:{A_ENDPOINT_PUBLIC_PORT}/incoming-data-app/multipartMessageHttpHeader' \
--header 'Content-Type: text/plain' \
--header 'Forward-To: {RECEIVER_IP_ADDRESS}:8889/data' \
--header 'IDS-Messagetype: ids:ArtifactRequestMessage' \
--header 'IDS-Id: https://w3id.org/idsa/autogen/artifactResponseMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f' \
--header 'IDS-Issued: 2021-11-24T13:09:42.306Z' \
--header 'IDS-IssuerConnector: http://w3id.org/engrd/connector/' \
--header 'IDS-ModelVersion: 4.1.0' \
--header 'IDS-RequestedArtifact: http://w3id.org/engrd/connector/artifact/1' \
--header 'IDS-SecurityToken-Id: https://w3id.org/idsa/autogen/958a6a2a-5a94-4cf9-ad72-b39c59ee8955' \
--header 'IDS-SecurityToken-TokenFormat: https://w3id.org/idsa/code/JWT' \
--header 'IDS-SecurityToken-TokenValue: DummyTokenValue' \
--header 'IDS-SecurityToken-Type: ids:DynamicAttributeToken' \
--header 'IDS-SenderAgent: http://sender.agent.com/' \
--data-raw '{"catalog.offers.0.resourceEndpoints.path":"/pet2"}'
```


Keeping the provided configuration:

<details>
  <summary>Http header request</summary>

```
curl --location --request POST 'https://localhost:8887/incoming-data-app/multipartMessageHttpHeader' \
--header 'Content-Type: text/plain' \
--header 'Forward-To: https//localhost:8889/data' \
--header 'IDS-Messagetype: ids:ArtifactRequestMessage' \
--header 'IDS-Id: https://w3id.org/idsa/autogen/artifactResponseMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f' \
--header 'IDS-Issued: 2021-11-24T13:09:42.306Z' \
--header 'IDS-IssuerConnector: http://w3id.org/engrd/connector/' \
--header 'IDS-ModelVersion: 4.1.0' \
--header 'IDS-RequestedArtifact: http://w3id.org/engrd/connector/artifact/1' \
--header 'IDS-SecurityToken-Id: https://w3id.org/idsa/autogen/958a6a2a-5a94-4cf9-ad72-b39c59ee8955' \
--header 'IDS-SecurityToken-TokenFormat: https://w3id.org/idsa/code/JWT' \
--header 'IDS-SecurityToken-TokenValue: DummyTokenValue' \
--header 'IDS-SecurityToken-Type: ids:DynamicAttributeToken' \
--header 'IDS-SenderAgent: http://sender.agent.com/' \
--data-raw '{"catalog.offers.0.resourceEndpoints.path":"/pet2"}'
```

</details>

The receiver connector will receive the request to the specified "*Forward-To*" URL, process data and finally send data to the *DATA_APP_ENDPOINT* as specified in its docker-compose. 
The data will be sent to the Data App using a body request as specified by the MULTIPART environment variable in the docker-compose.

### IDSCP2
IDSCP2 is used only between ECCs.
Follow the REST endpoint or WS examples, put the server hostname/ip address in the Forward-To header (*wss/https://{RECEIVER_IP_ADDRESS/Hostname}:{WS_PUBLIC_PORT}*).

### Web Socket over HTTPS
Follow the REST endpoint examples, taking care to use *wss://{RECEIVER_IP_ADDRESS}:{WS_PUBLIC_PORT}* in the Forward-To header.

### Broker

Information on how to interact with a Broker, can be found on following [link](doc/BROKER.md)

### Self Description API 
To manage your Self Description Document please check following [link](doc/SELF_DESCRIPTION.md)

### Audit logging
Audit events logging can be configured following [this document](doc/AUDIT.md)

### Connector health check

Connector health check functionality can be found in [this document](doc/HEALTHCHECK.md)

## Developer guide section

### How to build Execution Core Container
Clone projects from the following links and run mvn clean install

[Multipart Message library](https://github.com/Engineering-Research-and-Development/true-connector-multipart_message_library)

[WebSocket Message Streamer library](https://github.com/Engineering-Research-and-Development/market4.0-websocket_message_streamer)

[Clearing house model](https://github.com/Engineering-Research-and-Development/market4.0-clearing_house/tree/infomodel_4.0.6)

[Execution Core Container](https://github.com/Engineering-Research-and-Development/true-connector-execution_core_container)

There is one more dependency that needs to be added to local maven repository. It can be located in 

```
ci\.m2\repository\de\fraunhofer\aisec\ids\ids-comm\1.1.0\

```
 Copy following folder you your local .m2 repository folder.
