echo "Cloning and Creating Docker Container from Data-App repo..."
git clone -b proxy_wss_fix https://github.com/Engineering-Research-and-Development/true-connector-basic_data_app.git
cd true-connector-basic_data_app
mvn -U clean install
sudo docker build -f Dockerfile -t rdlabengpa/ids_be_data_app:latest .
cd ..
echo "Data-App is ready to start"