#!/bin/bash
# infos at https://learning.postman.com/docs/postman/collection-runs/command-line-integration-with-newman/
export TEST_TIMEOUT=120000 #2 min in ms
export ITERATIONS=1
newman run ./ci/tests/HTTPS-HTTPS.json --timeout-request ${TEST_TIMEOUT} --insecure --bail --verbose