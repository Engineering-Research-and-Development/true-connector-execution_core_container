# Changelog
All notable changes to this project will be documented in this file.

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
 - updated the Daps interaction: in case of disabled Daps interaction is no more needed to define keystore, trustore and the related properties
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

 
