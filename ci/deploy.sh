#!/bin/bash
echo "Deploy to Docker Hub"
sudo docker login -u ${DOCKER_USER} -p ${DOCKER_PASSWORD}
sudo docker tag rdlabengpa/execution_core_container_bl rdlabengpa/execution_core_container_bl:develop
sudo docker rmi rdlabengpa/execution_core_container_bl:latest
sudo docker push rdlabengpa/execution_core_container_bl
