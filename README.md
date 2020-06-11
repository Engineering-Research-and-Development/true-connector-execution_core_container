# ENG Execution Core Container

The ENG Execution Core Container, based on the IDS Base Connector, is the core component of an IDS Connector enabling:
* the data exchange between connectors, using HTTPS, WS over HTTPS, IDSCP (beta)
* interaction with the AISEC Fraunhofer DAPS Service for requiring and validating a token
* communication with the ENG Clearing House for registering transactions

![Execution Core Container Architecture](connector_schema_v1.1.PNG?raw=true "ENG Execution Core Container Architecture")

## How to Configurate and Run

The configuration should be performed customizing the following variables in the **docker-compose** file:
* **DATA_APP_ENDPOINT=192.168.56.1:8083/incoming-data-app/dataAppIncomingMessageReceiver** DataAPP endpoint for receiveing data (F endpoint in the above picture)
* **MULTIPART=mixed** DataAPP endpoint Content Type (choose mixed for Multipart/mixed or form for Multipart/form-data) 
* Edit external port if need (default values: **8086** for **web sockets IDSCP and WS over HTTPS**, **8090** for **http**, **8887** for **A endpoint** and  **8889** for **B endpoint**)

If you want to use your own certificate for the AISEC Fraunhofer DAPS server: 
* Put **DAPS certificates** into the cert folder and edit related settings (i.e., *application.keyStoreName*, *application.keyStorePassword*) into the *resources/application.properties* file

Finally, run the application:

*  Execute `docker-compose up &`


## Endpoints
The Execution Core Container will use two ports (http and https) as described by the Docker Compose File.
It will expose the following endpoints (both over https):
```
* /incoming-data-app/multipartMessageBodyBinary to receive data (MultiPartMessage) with binary body from Data App (the A endpoint in the above picture)
* /incoming-data-app/multipartMessageBodyFormData to receive data (MultiPartMessage) with form-data body from Data App (the A endpoint in the above picture)
* /incoming-data-channel/receivedMessage to receive data (IDS Message) from a sender connector (the B endpoint in the above picture)
```
Furthermore, just for testing it will expose (http and https):
```
* /about/version returns business logic version 
```


## Configuration
The ECC supports three different way to exchange data:
*  **REST endpoints** enabled if *IDSCP=false* and *WS_OVER_HTTPS=false*
*  **IDSCP** enabled if *IDSCP=true* and *WS_OVER_HTTPS=false*
*  **Web Socket over HTTPS** enabled if *WS_OVER_HTTPS=true* and *IDSCP=false*

## How to Test
The reachability could be verified using the following endpoints:
*  **http://{IP_ADDRESS}:{HTTP_PUBLIC_PORT}/about/version**

Keeping the provided docker-compose will be:
*  **http://{IP_ADDRESS}:8090/about/version**


The sender DataApp should send a request using the following schema, specifing in the Forward-To header the destination connector URL:

## How to Exchange Data
### REST endpoints
#### Multipart/mixed - Example 
```
curl -k -request POST 'https://{IPADDRESS}:{A_ENDPOINT_PUBLIC_PORT}/incoming-data-app/multipartMessageBodyBinary' 
   --header 'Content-Type: multipart/mixed; boundary=CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6' 
   --header 'Forward-To: {RECEIVER_IP_ADDRESS}:{B_ENDPOINT_PUBLIC_PORT}/incoming-data-channel/receivedMessage' 
   --data-binary '@/home/eng/MultipartMessageDataExample1.txt'
```


Keeping the provided docker-compose will be:
```
curl -k -request POST 'https://{IPADDRESS}:8887/incoming-data-app/multipartMessageBodyBinary' 
   --header 'Content-Type: multipart/mixed; boundary=CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6' 
   --header 'Forward-To: {RECEIVER_IP_ADDRESS}:8889/incoming-data-channel/receivedMessage' 
   --data-binary '@/home/eng/MultipartMessageDataExample1.txt'
```

#### Multipart/form-data - Example
```
curl -k --location --request POST 'https://{IPADDRESS}:{A_ENDPOINT_PUBLIC_PORT}/incoming-data-app/multipartMessageBodyFormData' 
    --header 'Forward-To: {RECEIVER_IP_ADDRESS}:{B_ENDPOINT_PUBLIC_PORT}/incoming-data-channel/receivedMessage' 
    --form 'header={
    "@type" : "ids:ArtifactResponseMessage",
    "issued" : "2019-05-27T13:09:42.306Z",
    "issuerConnector" : "http://iais.fraunhofer.de/ids/mdm-connector",
    "correlationMessage" : "http://industrialdataspace.org/connectorUnavailableMessage/1a421b8c-3407-44a8-aeb9-253f145c869a",
    "transferContract" : "https://mdm-connector.ids.isst.fraunhofer.de/examplecontract/bab-bayern-sample/",
    "modelVersion" : "1.0.2-SNAPSHOT",
    "@id" : "https://w3id.org/idsa/autogen/artifactResponseMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f"
  }
  ' --form 'payload={"catalog.offers.0.resourceEndpoints.path":"/pet"}'
```


Keeping the provided docker-compose will be:
```
curl -k --location --request POST 'https://{IPADDRESS}:8887/incoming-data-app/multipartMessageBodyFormData' 
    --header 'Forward-To: {RECEIVER_IP_ADDRESS}:8889/incoming-data-channel/receivedMessage' 
    --form 'header={
    "@type" : "ids:ArtifactResponseMessage",
    "issued" : "2019-05-27T13:09:42.306Z",
    "issuerConnector" : "http://iais.fraunhofer.de/ids/mdm-connector",
    "correlationMessage" : "http://industrialdataspace.org/connectorUnavailableMessage/1a421b8c-3407-44a8-aeb9-253f145c869a",
    "transferContract" : "https://mdm-connector.ids.isst.fraunhofer.de/examplecontract/bab-bayern-sample/",
    "modelVersion" : "1.0.2-SNAPSHOT",
    "@id" : "https://w3id.org/idsa/autogen/artifactResponseMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f"
  }
  ' --form 'payload={"catalog.offers.0.resourceEndpoints.path":"/pet"}'
```
An example of Multipart Message data (aligned to the IDS Information Model) can be found in the examples folder.

The receiver connector will receive the request to the specified "*Forward-To*" URL, process data and finally send data to the *DATA_APP_ENDPOINT* as specified in its docker-compose. 
The data will be sent to the Data App using a body request as specified by the MULTIPART environment variable in the docker-compose.

### IDSCP
Follow the REST endpoint examples, taking care to use *idscp://{RECEIVER_IP_ADDRESS}:{WS_PUBLIC_PORT}* in the Forward-To header.

### Web Socket over HTTPS
Follow the REST endpoint examples, taking care to use *wss://{RECEIVER_IP_ADDRESS}:{WS_PUBLIC_PORT}* in the Forward-To header.
