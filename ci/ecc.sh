echo "Creating Docker Container for ECCs..."
mvn -U clean install -DskipTests
docker build -f Dockerfile -t rdlabengpa/execution_core_container_bl .