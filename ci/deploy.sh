#!/bin/bash

echo "Creating Docker Image from ECC repo..."
sudo docker build -f Dockerfile -t rdlabengpa/ids_execution_core_container:develop .
cd ..
echo "Data-App is ready"
echo "Starting deployment to Docker Hub"
sudo docker login -u ${DOCKER_USER} -p ${DOCKER_PASSWORD}
sudo docker push rdlabengpa/ids_execution_core_container:develop
echo "Data-App deployed to Docker Hub"