# ENG Execution Core Container

[![Build badge](https://api.travis-ci.com/Engineering-Research-and-Development/market4.0-execution_core_container_business_logic.svg)](https://travis-ci.com/github/Engineering-Research-and-Development/market4.0-execution_core_container_business_logic)<br/>
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
* /incoming-data-app/multipartMessageHttpHeader to receive data (MultiPartMessage) represented with IDS-Message using http headers and payload in body from Data App (the A endpoint in the above picture)
* /data to receive data (IDS Message) from a sender connector (the B endpoint in the above picture)
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
   --header 'Forward-To: {RECEIVER_IP_ADDRESS}:{B_ENDPOINT_PUBLIC_PORT}/data' 
   --data-binary '@/home/eng/MultipartMessageDataExample1.txt'
```


Keeping the provided docker-compose will be:
```
curl -k -request POST 'https://{IPADDRESS}:8887/incoming-data-app/multipartMessageBodyBinary' 
   --header 'Content-Type: multipart/mixed; boundary=CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6' 
   --header 'Forward-To: {RECEIVER_IP_ADDRESS}:8889/data' 
   --data-binary '@/home/eng/MultipartMessageDataExample1.txt'
```

Or you can also use the following:
```
curl --location --request POST 'https://{IPADDRESS}:{A_ENDPOINT_PUBLIC_PORT}/incoming-data-app/multipartMessageBodyBinary' \
--header 'Content-Type: multipart/mixed; boundary=CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6' \
--header 'Forward-To: {RECEIVER_IP_ADDRESS}:{B_ENDPOINT_PUBLIC_PORT}/data' 
--data-raw '--CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6
Content-Disposition: form-data; name="header"
Content-Type: application/json; charset=UTF-8
Content-Length: 333
   {
  "@context" : {
    "ids" : "https://w3id.org/idsa/core/"
  },
  "@type" : "ids:ArtifactRequestMessage",
  "@id" : "https://w3id.org/idsa/autogen/artifactRequestMessage/76481a41-8117-4c79-bdf4-9903ef8f825a",
  "ids:issued" : {
    "@value" : "2020-11-25T16:43:27.051+01:00",
    "@type" : "http://www.w3.org/2001/XMLSchema#dateTimeStamp"
  },
  "ids:modelVersion" : "4.0.0",
  "ids:issuerConnector" : {
    "@id" : "http://w3id.org/engrd/connector/"
  },
  "ids:requestedArtifact" : {
   "@id" : "http://w3id.org/engrd/connector/artifact/1"
  }
}

--CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6
Content-Disposition: form-data; name="payload"
Content-Type: application/json
Content-Length: 50
{"catalog.offers.0.resourceEndpoints.path":"/pet2"}


--CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6--'
```

Keeping the provided docker-compose will be:
```
curl --location --request POST 'https://{IPADDRESS}:8887/incoming-data-app/multipartMessageBodyBinary' \
--header 'Content-Type: multipart/mixed; boundary=CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6' \
--header 'Forward-To: {RECEIVER_IP_ADDRESS}:8889/data' 
--data-raw '--CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6
Content-Disposition: form-data; name="header"
Content-Type: application/json; charset=UTF-8
Content-Length: 333
   {
  "@context" : {
    "ids" : "https://w3id.org/idsa/core/"
  },
  "@type" : "ids:ArtifactRequestMessage",
  "@id" : "https://w3id.org/idsa/autogen/artifactRequestMessage/76481a41-8117-4c79-bdf4-9903ef8f825a",
  "ids:issued" : {
    "@value" : "2020-11-25T16:43:27.051+01:00",
    "@type" : "http://www.w3.org/2001/XMLSchema#dateTimeStamp"
  },
  "ids:modelVersion" : "4.0.0",
  "ids:issuerConnector" : {
    "@id" : "http://w3id.org/engrd/connector/"
  },
  "ids:requestedArtifact" : {
   "@id" : "http://w3id.org/engrd/connector/artifact/1"
  }
}

--CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6
Content-Disposition: form-data; name="payload"
Content-Type: application/json
Content-Length: 50
{"catalog.offers.0.resourceEndpoints.path":"/pet2"}


--CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6--'
```

#### Multipart/form-data - Example
```
curl --location --request POST 'https://{IPADDRESS}:{A_ENDPOINT_PUBLIC_PORT}/incoming-data-app/multipartMessageBodyFormData' \
--header 'Content-Type: multipart/mixed; boundary=CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6' \
--header 'Forward-To: {RECEIVER_IP_ADDRESS}:{B_ENDPOINT_PUBLIC_PORT}/data' \
--form 'header="{
  \"@context\" : {
    \"ids\" : \"https://w3id.org/idsa/core/\"
  },
  \"@type\" : \"ids:ArtifactRequestMessage\",
  \"@id\" : \"https://w3id.org/idsa/autogen/artifactRequestMessage/a44d95c4-b4e7-47aa-b3d0-214f41150de8\",
  \"ids:issuerConnector\" : {
    \"@id\" : \"http://w3id.org/engrd/connector/\"
  },
  \"ids:issued\" : {
    \"@value\" : \"2020-11-25T16:33:13.502+01:00\",
    \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"
  },
  \"ids:modelVersion\" : \"4.0.0\",
  \"ids:requestedArtifact\" : {
    \"@id\" : \"http://w3id.org/engrd/connector/artifact/1\"
  }
}
";type=application/json; charset=UTF-8' \
--form 'payload="{\"catalog.offers.0.resourceEndpoints.path\":\"/pet2\"}";type=application/json; charset=UTF-8'
```


Keeping the provided docker-compose will be:
```
curl --location --request POST 'https://{IPADDRESS}:8887/incoming-data-app/multipartMessageBodyFormData' \
--header 'Content-Type: multipart/mixed; boundary=CQWZRdCCXr5aIuonjmRXF-QzcZ2Kyi4Dkn6' \
--header 'Forward-To: {RECEIVER_IP_ADDRESS}:8889/data' \
--form 'header="{
  \"@context\" : {
    \"ids\" : \"https://w3id.org/idsa/core/\"
  },
  \"@type\" : \"ids:ArtifactRequestMessage\",
  \"@id\" : \"https://w3id.org/idsa/autogen/artifactRequestMessage/a44d95c4-b4e7-47aa-b3d0-214f41150de8\",
  \"ids:issuerConnector\" : {
    \"@id\" : \"http://w3id.org/engrd/connector/\"
  },
  \"ids:issued\" : {
    \"@value\" : \"2020-11-25T16:33:13.502+01:00\",
    \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"
  },
  \"ids:modelVersion\" : \"4.0.0\",
  \"ids:requestedArtifact\" : {
    \"@id\" : \"http://w3id.org/engrd/connector/artifact/1\"
  }
}
";type=application/json; charset=UTF-8' \
--form 'payload="{\"catalog.offers.0.resourceEndpoints.path\":\"/pet2\"}";type=application/json; charset=UTF-8'
```

#### HTTP-Header - Example
```
curl --location --request POST 'https://{IPADDRESS}:{A_ENDPOINT_PUBLIC_PORT}/incoming-data-app/multipartMessageHttpHeader' \
--header 'Content-Type: text/plain' \
--header 'Forward-To: {RECEIVER_IP_ADDRESS}:{B_ENDPOINT_PUBLIC_PORT}/data' \
--header 'IDS-Messagetype: ids:ArtifactRequestMessage' \
--header 'IDS-Id: https://w3id.org/idsa/autogen/artifactResponseMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f' \
--header 'IDS-Issued: 2019-05-27T13:09:42.306Z' \
--header 'IDS-IssuerConnector: http://w3id.org/engrd/connector/' \
--header 'IDS-ModelVersion: 4.0.0' \
--header 'IDS-RequestedArtifact: http://w3id.org/engrd/connector/artifact/1' \
--data-raw '{"catalog.offers.0.resourceEndpoints.path":"/pet2"}'
```


Keeping the provided docker-compose will be:
```
curl --location --request POST 'https://{IPADDRESS}:8887/incoming-data-app/multipartMessageHttpHeader' \
--header 'Content-Type: text/plain' \
--header 'Forward-To: {RECEIVER_IP_ADDRESS}:8889/data' \
--header 'IDS-Messagetype: ids:ArtifactRequestMessage' \
--header 'IDS-Id: https://w3id.org/idsa/autogen/artifactResponseMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f' \
--header 'IDS-Issued: 2019-05-27T13:09:42.306Z' \
--header 'IDS-IssuerConnector: http://w3id.org/engrd/connector/' \
--header 'IDS-ModelVersion: 4.0.0' \
--header 'IDS-RequestedArtifact: http://w3id.org/engrd/connector/artifact/1' \
--data-raw '{"catalog.offers.0.resourceEndpoints.path":"/pet2"}'
```
An example of Multipart Message data (aligned to the IDS Information Model) can be found in the examples folder.

The receiver connector will receive the request to the specified "*Forward-To*" URL, process data and finally send data to the *DATA_APP_ENDPOINT* as specified in its docker-compose. 
The data will be sent to the Data App using a body request as specified by the MULTIPART environment variable in the docker-compose.

### IDSCP
Follow the REST endpoint examples, taking care to use *idscp://{RECEIVER_IP_ADDRESS}:{WS_PUBLIC_PORT}* in the Forward-To header.

### Web Socket over HTTPS
Follow the REST endpoint examples, taking care to use *wss://{RECEIVER_IP_ADDRESS}:{WS_PUBLIC_PORT}* in the Forward-To header.
