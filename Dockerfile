# Start with a base image containing Java runtime
FROM openjdk:11.0.7-jre
#FROM openjdk:12-jdk-oraclelinux7
# Add Maintainer Info
LABEL maintainer="gabriele.deluca@eng.it"



# Add a volume pointing to /tmp
VOLUME /tmp



# Make port 8443 available to the world outside this container
EXPOSE 8449



# The application's jar file
ARG JAR_FILE=target/*.jar


# Add the application's jar to the container
ADD ${JAR_FILE} market4.0-execution_core_container_business_logic.jar



# Run the jar file
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/market4.0-execution_core_container_business_logic.jar"]
#Healthy Status
HEALTHCHECK --interval=5s --retries=3 --timeout=10s \
  CMD wget -O /dev/null http://localhost:8081/about/version || exit 1
  #CMD curl http://localhost:8081/about/version || exit 1