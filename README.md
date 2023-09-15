# ENG Execution Core Container

[![License: AGPL](https://img.shields.io/github/license/Engineering-Research-and-Development/true-connector-execution_core_container.svg)](https://opensource.org/licenses/AGPL-3.0)
[![CI](https://github.com/Engineering-Research-and-Development/true-connector-execution_core_container/workflows/ECC/badge.svg)](https://github.com/Engineering-Research-and-Development/true-connector-execution_core_container/actions/workflows/ECC.yml) 
[![Docker badge](https://img.shields.io/docker/pulls/rdlabengpa/ids_execution_core_container.svg)](https://hub.docker.com/r/rdlabengpa/ids_execution_core_container)
<br/>

The ENG Execution Core Container, based on the IDS Base Connector, is the core component of an IDS Connector enabling:
* the data exchange between connectors, using HTTPS, WS over HTTPS, IDSCP2 (beta)
* interaction with the AISEC Fraunhofer DAPS Service for requiring and validating a token
* communication with the Fraunhofer Clearing House for registering transactions

![Execution Core Container Architecture](doc/connector_schema_v1.1.PNG?raw=true "ENG Execution Core Container Architecture")

## How to Configure and Run

The configuration should be performed customizing the following variables in the **docker-compose** file:
* **DATA_APP_ENDPOINT=https://localhost:8083/data** DataAPP endpoint for receiving data (F endpoint in the above picture)
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

Following endpoints are open for all (credentials are not required), since in real scenario, they will be accessible only from dataApp:

```
/incoming-data-app/multipartMessageBodyBinary to receive data (MultiPartMessage) with binary body from Data App (the A endpoint in the above picture)
/incoming-data-app/multipartMessageBodyFormData to receive data (MultiPartMessage) with form-data body from Data App (the A endpoint in the above picture)
/incoming-data-app/multipartMessageHttpHeader to receive data (MultiPartMessage) represented with IDS-Message using http headers and payload in body from Data App (the A endpoint in the above picture)
/data to receive data (IDS Message) from a sender connector (the B endpoint in the above picture)
```

while 'B-endpoint' is public, and exposed to the world.

```
/data
```

More information about security and user credentials can be found in this [link](doc/SECURITY.md)


Furthermore, just for testing it will expose (http and https):

```
/about/version
```
returns business logic version.

## Configuration
The ECC supports three different way to exchange data:
*  **REST endpoints** enabled if *IDSCP2=false* and *WS_OVER_HTTPS=false*
*  **IDSCP2** enabled if *IDSCP2=true* and *WS_INTERNAL=false* (use https on the edge) or *IDSCP2=true* and *WS_INTERNAL=true* (use WS on the edge)
*  **Web Socket over HTTPS** enabled if *WS_OVER_HTTPS=true* and *IDSCP2=false*

## Firewall <a name="firewall"></a>

Execution Core Container allows setting up HttpFirewall through Spring Security. To turn it on/off, please take a look at following property: 

```
#Firewall
application.firewall.isEnabled=true
```

If firewall is enabled, it will read properties defined in `firewall.properties` file which easily can be modified by needs of setup.

```
#Set which HTTP methods should be allowed
allowedMethods=GET,POST
#Set if a backslash "\" or a URL encoded backslash "%5C" should be allowed in the path or not
allowBackSlash=true
#Set if a slash "/" that is URL encoded "%2F" should be allowed in the path or not
allowUrlEncodedSlash=true
#Set if double slash "//" that is URL encoded "%2F%2F" should be allowed in the path or not
allowUrlEncodedDoubleSlash=true
#Set if semicolon is allowed in the URL (i.e. matrix variables)
allowSemicolon=true
#Set if a percent "%" that is URL encoded "%25" should be allowed in the path or not
allowUrlEncodedPercent=true
#if a period "." that is URL encoded "%2E" should be allowed in the path or not
allowUrlEncodedPeriod=true
```
*IMPORTANT:* If you're not an expert, the strong advice is to keep values at their default values. If you decide to change values, pay special attention to allowHeaderNames and allowHeaderValues, since those set values are exclusive and considered as only values that should be present in the header.

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
--header 'Forward-To: {RECEIVER_IP_ADDRESS}:{B_ENDPOINT_PUBLIC_PORT}/data' \
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
  "ids:modelVersion" : "4.2.7",
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
--header 'Forward-To: https://localhost:8889/data' \
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
  "ids:modelVersion" : "4.2.7",
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
  \"ids:modelVersion\" : \"4.2.7\",
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
  \"ids:modelVersion\" : \"4.2.7\",
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
--header 'IDS-ModelVersion: 4.2.7' \
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
--header 'IDS-ModelVersion: 4.2.7' \
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

## Building the execution core container

**Requirements:**

 `Java11` `Apache Maven`
 
To build the execution core container you will have to do one of the following:

**Solution 1**

Use provided libraries on GitHub Package. To do so, you will have to modify Apache Maven settings.xml file like following:

Add in servers section:

```xml
<servers>
  <server>
    <id>github</id> 
    <username>some_username</username>
    <password>{your GitHub Personal Access Token}</password> 
  </server>
</servers>
```

How to get GH PAT, you can check following [link](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token)


**Solution 2**

 * Clone [Multipart Message Library](https://github.com/Engineering-Research-and-Development/true-connector-multipart_message_library) 
 * Once this project is cloned, run `mvn clean install`

This will install an internal library that is needed by the execution core container project.

After that you can run `mvn clean package` in the root of the execution core container project, to build it.


**NOTE:** If you proceed with Solution 2, pay attention to the Multipart Message Library version in pom.xml file, and check if the same version is used in the execution core container pom.xml, if not modify them according to the one from the clone repository.

### Running ECC from IDE

If you wish to run ECC from IDE, please address following prerequisite:

 * Add src/main/resources directory to class path; either by making this change in Run configuration in IDE or comment out exclude section in pom.xml. This resource directory is removed from final jar, to externalize property files from docker image.
 
 * Copy ssl-server.jks file from [TRUE Connector](https://github.com/Engineering-Research-and-Development/true-connector/blob/main/ecc_cert/ssl-server.jks) repo into some directory on the file system
 
 * Configure property application.targetDirectory= to point to the file containing ssl-server.jks file
 * Configure DAPS related properties, if DAPS will be used.
 
 ```
application.dapsUrl= 
application.keyStoreName=
application.keyStorePassword=
application.keystoreAliasName=
application.trustStoreName=
application.trustStorePassword=
application.connectorUUID=
application.dapsJWKSUrl=
```
 * Choose one of 2 profiles: SENDER or RECEIVER.
 * Start application 
 
### Creating docker image

Once you build the execution core container, if required, you can build docker image, by executing following command, from terminal, inside the root of the project:

```
docker build -t some_tag .
```
### GitHub Workflow

This repository implements following branch management:

![diagram](doc/workflow//github_actions_workflow.drawio.png?raw=true "GitHub Workflow diagram")

and has several GitHub action files to support such functionality. Those files are located in:

*.github\workflows\*  

#### feature_hotfix.yml

Used when code is pushed to branch prefixed with feature/ or hotfix/

Customize GHA to fit your needs. For now, just run mvn clean package

#### develop.yml

Executed when code is pushed to develop branch.

#### maven_release.yml

Manual trigger of the GHA.</br>
Perform mvn release:prepare and mvn release:perform.

Input parameters:</br>
release version</br>
next development version</br>
tag version

#### docker-publish.yml

Manual trigger of the GHA.</br>
Build docker image, and push it to dockerhub.</br>
Sign with cosign.

Input parameters:<br/>
versionName<br/>
tagMessage

### Issue management

Create issue in Issue tab in GitHub repo, before starting to work on new functionality. It would be nice to provide task breakdown, with estimation. Try not to have activities that are longer than 8 hours. If such activity is present in task breakdown, please split this activity in 2 or more sub activities, trying that new sub activities are not bigger than 8 hours.

Example could be like following:

~~Implement new ServiceA - 16h~~</br>
Implement method A in ServiceA 4h</br>
Implement method B in ServiceA 3h</br>
Implement method C in ServiceA 7h</br>
Implement method D in ServiceA 2h</br>
Write unit tests for ServiceA - 6 hours</br>
Update documentation - 2 hours</br>


### Working on new feature

If you need to work on new feature, be sure first to pull changes from origin, and create new branch, following the naming convention from develop branch.

### Creating Pull Request

When creating pull request, double check if PR will merge to develop branch and not master. If this is not the case, change destination branch to be develop.

## Code coverage

Code coverage is checked by using jacoco plugin.

![Execution Core Container Code Coverage](doc/jacoco.jpg?raw=true "ENG Execution Core Container Code coverage")

For more up to date information about code coverage, you can check report after you build a project. Report can be found in 

```
target\site\jacoco\index.html
```
