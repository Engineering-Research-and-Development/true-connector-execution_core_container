# Start with a base image containing Java runtime

# FROM openjdk:12-jdk-oraclelinux7
# FROM openjdk:11.0.7-jre
FROM openjdk:11.0.15-jre

# Add Maintainer Info
LABEL maintainer="gabriele.deluca@eng.it"

# Add a volume pointing to /tmp
VOLUME /tmp

# Make port 8443 available to the world outside this container
EXPOSE 8449

# The application's jar file
# ARG JAR_FILE=target/*.jar
COPY target/dependency-jars /run/dependency-jars

# Add the application's jar to the container
# ADD ${JAR_FILE} market4.0-execution_core_container_business_logic.jar
ADD target/application.jar /run/application.jar

# Run the jar file
# ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-cp", "application.jar:/config/", "org.springframework.boot.loader.JarLauncher"]
ENTRYPOINT java -jar run/application.jar

#Healthy Status
HEALTHCHECK --interval=5s --retries=3 --timeout=10s \

#CMD curl http://localhost:8081/about/version || exit 1
CMD wget -O /dev/null http://localhost:8081/about/version || exit 1