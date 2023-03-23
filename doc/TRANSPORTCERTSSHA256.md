# TransportCertsSha256 JWT validation


## jwToken

```
header:
{
  "typ": "at+jwt",
  "kid": "TCUFeCNazalKHg9KzrzLIAzRWTMDDWSa7LcM9ZwHMyo",
  "alg": "RS256"
}
payload:
{
  "aud": "idsc:IDS_CONNECTORS_ALL",
  "iss": "https://daps.aisec.fraunhofer.de",
  "sub": "54:3D:3A:3A:FC:DC:05:AB:88:60:9E:60:36:54keyid:CB:8C:C7:B6:85:79:A8:23:A6:CB:15:AB:17:50",
  "nbf": 1674038398,
  "iat": 1674038398,
  "jti": "MTQ4MDUzNTQ0NjQ3OTcxNzcxMjI=",
  "exp": 1674041998,
  "client_id": "54:3D:3A:3A:FC:DC:05:AB:88:60:9E:60:36:54keyid:CB:8C:C7:B6:85:79:A8:23:A6:CB:15:AB:17:50",
  "securityProfile": "idsc:BASE_SECURITY_PROFILE",
  "referringConnector": "http://ecc-consumer.demo",
  "@type": "ids:DatPayload",
  "@context": "https://w3id.org/idsa/contexts/context.jsonld",
  "transportCertsSha256": "a3cd813e1510ca64a9da****",
  "scopes": [
    "idsc:IDS_CONNECTOR_ATTRIBUTES_ALL"
  ]
}
```

## Prerequisite 
For extended token validation is that **public keys from connector itself and other connectors MUST be loaded into truststore.** Reason for this is that TRUEConnector will, during startup:
 - load all certificates from truststore
 - generate hash from certificate, using *MessageDigest* class.
 - use certificate's SubjectAlternativeName and populate map with SAN and hash. This map will later be used to perform extended jwToken validation.
 
From our example, TLS certificate should be for DNS domain with name *ecc-consumer.demo*, and when hash is calculated from certificate, it should be a3cd813e1510ca64a9da\**\**. Those 2 values will be put in map, like key-pair (ecc-consumer.demo, a3cd813e1510ca64a9da\**\**), that will be used in verify token phase.
 
## Validate jwToken

Once jwToken is received, either from DAPS or from other connector, it will be validated with following:

 - not expired
 - signature
 - transportCertsSha256 claim (extended validation - optional)
 
First two checks are mandatory while third one can be switched on or off, depending on property;
(setting this property to false should be used only in development purposes):
 
```
application.extendedTokenValidation=true
```

 
Extended validation will do the following:
 - get *referringConnector* claim (in our example - "http://ecc-consumer.demo")
 - get *transportCertsSha256* (in our example - "a3cd813e1510ca64a9da****")
 - check if map contains same hash value for referringConnector
 
 In our example, map should contain key-pair like following (populated in startup phase):
 
 (ecc-consumer.demo, a3cd813e1510ca64a9da****)
 
 If this evaluates as true, token is valid, otherwise, token is not valid.
 
