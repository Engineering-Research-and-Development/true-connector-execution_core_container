echo "Installing Multipart Message Lib..."
git clone -b TestUtilMessageService_removal https://github.com/Engineering-Research-and-Development/true-connector-multipart_message_library.git
cd true-connector-multipart_message_library
mvn -U clean install
cd ..
echo "Installed  Multipart Message Lib"