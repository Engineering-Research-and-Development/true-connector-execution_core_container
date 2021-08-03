# Changelog
All notable changes to this project will be documented in this file.

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

 
