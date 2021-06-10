#!/bin/bash
echo "Deploy to Docker Hub for developer"
sudo docker login -u ${DOCKER_USER} -p ${DOCKER_PASSWORD}
sudo docker tag rdlabengpa/execution_core_container_bl rdlabengpa/ids_execution_core_container:develop
sudo docker push rdlabengpa/ids_execution_core_container:develop