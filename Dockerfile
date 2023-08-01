# Start with a base image containing Java runtime
#FROM ibm-semeru-runtimes:open-11-jre
#openjdk:21-ea-3-jdk-slim
FROM eclipse-temurin:11-jre-alpine

# Add Maintainer Info
LABEL maintainer="gabriele.deluca@eng.it"

RUN apk add --no-cache wget openssl curl
#RUN  apt-get update \
#  && apt-get install -y wget \
#  && rm -rf /var/lib/apt/lists/

RUN mkdir -p /home/nobody/data/sd && mkdir -p /home/nobody/data/log/ecc


WORKDIR /home/nobody

# The application's jar file
COPY target/dependency-jars /home/nobody/app/dependency-jars

# Add the application's jar to the container
ADD target/application.jar /home/nobody/app/application.jar

RUN chown -R nobody:nogroup /home/nobody

USER 65534

# Run the jar file
ENTRYPOINT java -jar /home/nobody/app/application.jar

#Healthy Status
HEALTHCHECK --interval=5s --retries=12 --timeout=10s CMD curl --fail -k https://localhost:8449/about/version || exit 1
