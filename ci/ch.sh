echo "Downloading and installing Clearing-House Model..."
git clone -b infomodel_4.0.6 https://github.com/Engineering-Research-and-Development/market4.0-clearing_house.git
cd market4.0-clearing_house
mvn install -DskipTests
cd ..
echo "Clearing-House Model installed!"