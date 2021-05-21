#!/bin/bash
# infos at https://learning.postman.com/docs/postman/collection-runs/command-line-integration-with-newman/
export TEST_TIMEOUT=120000 #2 min in ms
export ITERATIONS=1
#newman run ./ci/tests/tests.json --insecure --timeout-request ${TEST_TIMEOUT} --iteration-count ${ITERATIONS} --bail

wget --no-check-certificate --quiet \
  --method POST \
  --timeout=0 \
  --header 'Content-Type: text/plain' \
  --body-data '{
    "multipart": "mixed",
    "Forward-To": "https://ecc-provider:8889/data",
	 "message": {
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
	},
	"payload" : {
		"catalog.offers.0.resourceEndpoints.path":"/pet2"
		}
}' \
   'https://localhost:8084/proxy'