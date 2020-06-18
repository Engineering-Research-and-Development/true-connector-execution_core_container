#!/bin/bash
echo "Deploy to Docker Hub"
docker login -u ${DOCKER_USER} -p ${DOCKER_PASSWORD}
docker tag rdlabengpa/execution_core_container_bl delucagabriele/execution_core_container_bl:develop
docker push delucagabriele/execution_core_container_bl