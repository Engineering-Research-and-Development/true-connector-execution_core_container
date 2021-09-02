echo "Installing websocket-message-streamer-lib..."
git clone -b infomodel_4.0.6 https://github.com/Engineering-Research-and-Development/true-connector-websocket_message_streamer.git
cd true-connector-websocket_message_streamer
mvn -U clean install
cd ..
echo "Installed websocket-message-streamer-lib"