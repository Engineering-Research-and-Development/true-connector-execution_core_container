#!/bin/bash
echo "Deploy to Docker Hub"
docker login -u ${DOCKER_USER} -p ${DOCKER_PASSWORD}
docker tag rdlabengpa/execution_core_container_bl rdlabengpa/execution_core_container_bl:develop
docker rmi rdlabengpa/execution_core_container_bl:latest
docker push rdlabengpa/execution_core_container_bl
