# Changelog
All notable changes to this project will be documented in this file.

## [1.14.9] - 2024-07-11

### Added

 - CORS configuration (for compatibility with UI)

### Changed

 - Updated firewall.properties
 - Multipart message library upgraded to 1.0.18
 - Websocket library upgraded to 1.0.18
 - Fix WSS SSL support
 - Upgrade GHA to use Node.js 20
 
 
## [1.14.8] - 2024-02-14

### Added

 - Audit logs stored in database
 - Column encrypted using AES/GCM/NoPadding
 - New endpoint for fetching audit events
 - requires to set environment variable with AES256-SECRET-KEY used in encryption algorithm
 
### Changed

 - Using JFrog for IDS dependency management
 
### Removed

 - Audit events are not stored into file with this change but in DB (H2 as default)

## [1.14.7] - 2024-01-19

### Added

 - Simple user management

## [1.14.6] - 2023-11-16

### Changed

 - cosign version upgrade with alpine image; must include --insecure-ignore-tlog flag

## [1.14.5] - 2023-10-30

### Changed

 - CertificationCheck now uses Cosign to verify if running version is signed

### Added

 - new property application.selfdescription.defaultEndpoint - used for custom URI where connector self-description is exposed

## [1.14.4] - 2023-10-05

### Changed

  - Fix extended validation bug

## [1.14.3] - 2023-09-12

### Changed

 - failed login is registered in logs (UserNotFoundException)
 - bumped spring-boot-parent to 2.5.14
 - hardcoded not to show stack trace in response
 - if DAPS certificate is not loaded, or certificate expired, connector will not start
 - changed how connectorId is used - now it is single property that needs to be set (application.connectorid)
 - if connectorId is not valid (blank) connector will not start

### Added

 - added Spring StrictHttpFirewall with firewall.property file; default - disabled
 - new property application.connectorid
 
## [1.14.2] - 2023-08-01

### Changed

 - Dockerfile improvements

## [1.14.1] - 2023-07-23

### Added

 - new processor at B-endpoint, for checking incoming info model version

### Changed

 - Hardcoded to use TLS communication only
 - Property application.disableSslVerification deleted, so trust store must be set and valid
 - Deleted next properties and set them to the hardcoded values:
 camel.springboot.main-run-controller = true, camel.component.http4.use-global-ssl-context-parameters = true, camel.component.jetty.use-global-ssl-context-parameters = true, server.ssl.enabled = true
 - refactor classes for handling TLS (server certificate and truststore) and Daps certificate
 - Security upgrade com.auth0:jwks-rsa from 0.21.1 to 0.22.1
 - Security upgrade net.logstash.logback:logstash-logback-encoder from 7.0.1 to 7.3
 - Security upgrade com.auth0:java-jwt from 3.19.1 to 3.19.3
 - Security upgrade org.bitbucket.b_c:jose4j:0.7.8 to 0.9.3

## [1.14.0] - 2023-07-14

### Added

 - added certification check
 - checksum for artifact/1 and artifact/big

### Changed

 - admin role now changed to apiUser, for ease of understanding
 - default user name now application.user.api.username=apiUser
 - upgraded org.json:json from 20210307 to 20230227 (Dependabot #4 - json stack overflow vulnerability)

### Removed

 - removed redundant and unused http endpoint that was running in parallel with https for Spring Boot; if http is needed please set server.ssl.enabled=false

## [1.13.1] - 2023-06-30

### Changed

 - updated GitHub Actions, simplified input parameters; skipping develop build when performing mvn release commits
 - upgraded Tomcat version to 9.0.76 due to small NIO exception bug when fetching Self Description

## [1.13.0] - 2023-06-16

### Removed

 - removed internal sd route (DataApp v0.2.7 mandatory)
 - auth for self description endpoint
 - add requestedArtifact as mandatory field for communication with UC App (UC App v1.7.0 mandatory)

## [1.12.0] - 2023-05-30

### Added

 - new property application.internalSelfdescriptionPort=
 - OCSP verification of receiver certificate

### Changed

 - version increase to align with docker images
 - internal/sd now always available on it's own port
 - all camel routes now only support POST method with the exception of internal/sd which is available only with GET
 - dependency vulnerabilities know checked by dependabot
 
### Removed

 - removed authorization from IDS endpoint (B endpoint)

## [0.3.0-SNAPSHOT] - 2023-04-06

### Changed

 - Support for DataApp message handler (http-header flow)
 - Requested for for this ECC version requires 0.2.0-SNAPSHOT+ version of DataApp
 - Support for GitHub Workflow (release process, docker image signing)
 - New UsageControl Platoon docker image (temurin based) rdlabengpa/ids_uc_data_app_platoon:v1.5 in GitHub Actions
 - Add self-description for WSS flow
 - Rename application.wss.server.port to application.wss-server-port
 - Add messageType to WS-WS test case
 - Add test1.csv as DataResource to selfdescription
 - Multipart message library upgrade to 1.0.17
 - infomodel dependency now managed via Multipart Library (excluded infomodel from idscp2 dependencies)

## [0.2.3-SNAPSHOT] - 2023-01-23

### Changed

 - Camel version from 3.18.0 to 3.19.0 (The default TLS protocol is changed from TLSv1.2 to TLSv1.3 in Camel JSSE support)
 - Multipart message library upgrade from 1.0.14-SNAPSHOT to 1.0.15-SNAPSHOT (memory cleaner in MMP)
 - Switch to base docker image eclipse-temurin:11-jre-alpine
 - Fix in messageToHeaders method in HttpHeaderServiceImpl
 
### Added

 - When requesting DAPS token, transportCertsSha256 from server public key will be sent
 - Logic for checking TransportCertsSha256 hash value from jwToken
 - readme file TRANSPORTCERTSSHA256.md with explanation
 - new property for enabling/disabling new logic for extended token validation 
 
 ```
 application.extendedTokenValidation=false
 ```

## [0.2.2-SNAPSHOT] - 2022-12-29

### Notes

 - version remains the same since nothing is done to the code

### Changed

 - GitHub Actions now uses the develop Data App image from Docker Hub (rdlabengpa/ids_be_data_app:develop) instead of building it to speed up the process
 - GitHub Actions tests now split to work in parallel
 
### Added

 - added GHA tests.md document with brief explanation about the GitHub Actions tests

## [0.2.2-SNAPSHOT] - 2022-12-19

### Added

 - added GitHub Actions tests for contract negotiation
 - properties that will differentiate 2 users:
  Self Description API user, with ADMIN role; used to manipulate Self Description API
  Connector user, with CONNECTOR role; used to authenticate and authorize IDS message interaction with Connector (B-endpoint)
 - Camel Policy, used to authenticate camel routes
 
```
#API management credentials
application.user.api.username=admin
application.user.api.password=$2a$10$MQ5grDaIqDpBjMlG78PFduv.AMRe9cs0CNm/V4cgUubrqdGTFCH3m
application.user.connector.username=connector
application.user.connector.password=$2a$10$MQ5grDaIqDpBjMlG78PFduv.AMRe9cs0CNm/V4cgUubrqdGTFCH3m
#number of consecutive failed attempts
application.user.lock.maxattempts=5
# duration for how long user will be locked
application.user.lock.duration=30
# time unit used for locking user, possible values are: SECONDS,MINUTES,HOURS,DAYS
application.user.lock.unit=MINUTES
```

### Changed
 - Refactor GitHub Actions tests
 - using ids-comm dependency from Frauenhofer repository
 
### Removed
 - removed GitHub Actions tests for payload only
 - local maven dependencies

## [0.2.1-SNAPSHOT] - 2022-12-08

### Added
- New properties for Password Validator:

```
## Password Validator
application.password.validator.minLength=8
application.password.validator.maxLength=16
application.password.validator.minUpperCase=1
application.password.validator.minLowerCase=1
application.password.validator.minDigit=1
application.password.validator.minSpecial=1
```

- added a Password Validator to verify that the password meets the desired criteria



## [0.2.0-SNAPSHOT] - 2022-12-02

 
 ```
 ### Clearng-House
application.clearinghouse.isEnabledClearingHouse=false
application.clearinghouse.username=
application.clearinghouse.password=
application.clearinghouse.baseUrl=
application.clearinghouse.logEndpoint=/messages/log/
application.clearinghouse.processEndpoint=/process/
```

### Changed

 - updated Clearing House logic now compliant with Frauenhofer CH
 - header propagation was not properly done when sending Broker messages, thats fixed now
 
### Removed

 - ENG Clearing House dependency removed

## [0.1.22-SNAPSHOT] - 2022-10-31

### Added
 - New endpoint (password protected) for getting hash value for SelfDescription API password

### Changed

 - banner is now packed with jar, to avoid need to manually change it in dockerized version 

## [0.1.21-SNAPSHOT] - 2022-10-26

### Added
 - added audit logging
 
### Changed
 - updated documentation with new functionality
 - added log lines to track of connector setup: REST, WSS, IDSCPv2

## [0.1.20-SNAPSHOT] - 2022-10-07

### Added
 - logic for locking API user after consecutive failed attempts
 - new properties to configure user locking functionality
 
### Changed
 - updated documentation with new functionality
 - added log lines to track of connector setup: REST, WSS, IDSCPv2

## [0.1.19-SNAPSHOT] - 2022-10-04

### Added
 - added new GHA tests for payload extraction logic

## [0.1.18-SNAPSHOT] - 2022-09-15

### Added
 - added new junit tests
 
### Changed
  - small code refactoring

## [0.1.17-SNAPSHOT] - 2022-08-05

### Added
 - added new GHA tests for big payload

### Changed
  - mixed and form responses are now passed through OutputStream to avoid org.apache.http.ContentTooLongException
  - updated existing GHA tests with new .env files

## [0.1.16-SNAPSHOT] - 2022-08-03

### Changed
 - Upgraded versions for following dependencies:
	camel version update
	async-http-client
	com.auth0
	bcprov-jdk15on
	com.squareup.okhttp3
	com.squareup.okio
 - Dockerfile base image version upgrade


## [0.1.15-SNAPSHOT] - 2022-08-02

### Changed
 - Rejection message is now always created from the request message
 
### Removed
 - removed DAT from rejection message

## [0.1.14-SNAPSHOT] - 2022-07-18

### Added
 - added new service for interacting with Platoon Usage Control dataApp
 - new property, *application.usageControlVersion*, which is used to configure which UC dataApp to use, platoon or mydata

### Changed
 - refined logic for modify/de-modify payload, now only for ArtifactResponseMessage
 - updated logic for Receiver, send response, HttpEntity instead String representation of multipart/form-data
 - reverted change for DAPS jwks URL, compatibility with Omejdn (not using default jwks URL)
 - added public key in Self Description document
 - policy upload is now done when we get the correct response instead of doing it before sending request

### Removed
 - parseReceivedResponseMessage processor is removed, parsing of the response is done on handleResponse, not as separate step

## [0.1.13-SNAPSHOT] - 2022-05-27

### Changed
 - Self Description typo fix 
 
### Added
 - added property and functionality to skip protocol validation

## [0.1.12-SNAPSHOT] - 2022-05-19
 
### Changed
 - New version of Multipart Message Library
 - Modified self-description document to use dateTimeStamp instead dateTime, compatibility with standard 

## [0.1.11-SNAPSHOT] - 2022-02-21
 
### Changed
 - Replaced swagger with springdoc and updated documentation 

## [0.1.10-SNAPSHOT] - 2021-12-03
 
### Changed
 - UC properties not required when disabled (can remain blank)

## [0.1.9-SNAPSHOT] - 2021-12-02

### Added
 - encode-decode payload on ECC boundaries - default is set to false - will not enforce encoding-decoding
 - added new property
	**application.encodeDecodePayload=false**
	to manage newly added logic
 
## [0.1.8-SNAPSHOT] - 2021-11-23

### Added
 - default contract offer,artifact and resource for self description
 
### Removed
 - removed support for DAPS v1 token

## [0.1.7-SNAPSHOT] - 2021-11-05
 
### Added
 - now supporting file sending over multipart/form

## [0.1.6-SNAPSHOT] - 2021-11-05
 
### Changed
 - modification for improved docker image creation

## [0.1.5-SNAPSHOT] - 2021-10-15
 
### Changed
 - minor exception and exception handling changes

## [0.1.4-SNAPSHOT] - 2021-10-12
 
### Changed
 - updated camel framework to 3.11.2
 
### Removed
 - removed stand-alone com.google.protobuf dependencies
 - removed org.checkerframework dependency
 - removed unnecessary exceptions

## [0.1.3-SNAPSHOT] - 2021-10-11
 
### Changed
 - removed info-model property from property file; it will be read from multipart processor message library

## [0.1.2-SNAPSHOT] - 2021-10-11
 
### Changed
 - RejectionMessageService dependency injection now done through constructor

## [0.1.1-SNAPSHOT] - 2021-10-08
 
### Changed
 - TestUtilMessageService now everywhere replaced with UtilMessageService

## [0.1.0-SNAPSHOT] - 2021-09-16
 
### Changed
 - infomodel version has been changed to 4.1.1
 
 ### Added
 - mandatory fields/properties to RejectionMessages 
 
## [0.0.27-SNAPSHOT] - 2021-08-02

### Changed
 - updated the Daps interaction: in case of disabled Daps interaction is no more needed to define keystore, truststore and the related properties
 - disabled fetch token on start up in case of Daps interaction disabled

## [0.0.26-SNAPSHOT] - 2021-07-30
 
### Changed
 - updated logic for using objectMapper to convert from IDS Message to Map and creating Map from incoming headers to convert back to IDS Message
 
## [0.0.25-SNAPSHOT] - 2021-07-23
 
### Added
 - added API endpoints and logic for dynamic modification of Self Description document
 - new properties</br>
	 application.selfdescription.filelocation</br>
	 spring.security.user.name</br>
	 spring.security.user.password
 
### Changed
 - updated developer information with missing dependency
 
## [0.0.24-SNAPSHOT] - 2021-07-08
 
### Added
 - added CI build GitHub Actions PAT, repository for MMP 
 
### Changed
 - Make use of TestUtilMessage service from Multipart Message library
 - updated versions for Multipart Message library
 - updated version for WebSocket Message Streamer library

## [0.0.23-SNAPSHOT] - 2021-07-01
 
### Added
 - moved model , service, config, autoconfig and exception packages from Camel Interceptor Ucapp directly to Execution Core Container

### Changed
 - removed Camel Interceptor Ucapp dependency from pom.xml 

 
