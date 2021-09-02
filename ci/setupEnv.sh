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