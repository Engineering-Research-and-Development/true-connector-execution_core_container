#!/bin/bash

echo "Downloading certificate from private repository..."
git clone https://${GH_TOKEN}:x-oauth-basic@github.com/Engineering-Research-and-Development/private-files-repo.git
cp -a private-files-repo/. ./ci/docker/ecc_cert
echo "Certificate from private repository downloaded"

echo "Installing Newman CLI..."
sudo npm set strict-ssl false
sudo npm install -g newman@5.2.2
newman --version
echo "Newman installed, READY TO TEST..."

echo "Downloading and Installing docker-compose..."
sudo rm /usr/local/bin/docker-compose
sudo curl -L "https://github.com/docker/compose/releases/download/1.29.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
echo "docker-compose correctly installed"

sudo docker system prune --volumes -f

mkdir -p  $HOME/.m2/repository/de/fraunhofer/aisec
mkdir -p  $HOME/.m2/repository/de/fraunhofer/dataspaces
cp -rf ./ci/.m2/repository/de/fraunhofer/aisec/ids  $HOME/.m2/repository/de/fraunhofer/aisec
cp -rf ./ci/.m2/repository/de/fraunhofer/dataspaces/iese  $HOME/.m2/repository/de/fraunhofer/dataspaces
cp -f ./ci/.m2/settings/settings.xml  $HOME/.m2

echo "Installing Multipart Message Lib..."
git clone https://github.com/Engineering-Research-and-Development/true-connector-multipart_message_library.git
cd true-connector-multipart_message_library
mvn -U clean install
cd ..
echo "Installed  Multipart Message Lib"

echo "Installing websocket-message-streamer-lib..."
git clone https://github.com/Engineering-Research-and-Development/true-connector-websocket_message_streamer.git
cd true-connector-websocket_message_streamer
mvn -U clean install
cd ..
echo "Installed websocket-message-streamer-lib"

echo "Cloning and Creating Docker Container from Data-App repo..."
git clone https://github.com/Engineering-Research-and-Development/true-connector-basic_data_app.git
cd true-connector-basic_data_app
mvn -U clean install
sudo docker build -f Dockerfile -t rdlabengpa/ids_be_data_app:latest .
cd ..
echo "Data-App is ready to start"

echo "Downloading and installing Clearing-House Model..."
git clone https://github.com/Engineering-Research-and-Development/market4.0-clearing_house.git
cd market4.0-clearing_house
mvn install -DskipTests
cd ..
echo "Clearing-House Model installed!"

echo "Creating Docker Container for ECCs..."
mvn -U clean install -DskipTests
docker build -f Dockerfile -t rdlabengpa/execution_core_container_bl .