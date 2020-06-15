#!/bin/bash
# infos at https://learning.postman.com/docs/postman/collection-runs/command-line-integration-with-newman/
export TEST_TIMEOUT=120000 #2 min in ms
export ITERATIONS=1
newman run ./travis/tests/tests-${NET}-${NETE}.json --insecure --timeout-request ${TEST_TIMEOUT} --iteration-count ${ITERATIONS} --bail