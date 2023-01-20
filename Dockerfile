# Start with a base image containing Java runtime

FROM ibm-semeru-runtimes:open-11-jre

# Add Maintainer Info
LABEL maintainer="gabriele.deluca@eng.it"

#RUN apk add --no-cache wget
RUN  apt-get update \
  && apt-get install -y wget \
  && rm -rf /var/lib/apt/lists/

# Make port 8443 available to the world outside this container
EXPOSE 8449

RUN mkdir /home/nobody
RUN mkdir /var/log/ecc
WORKDIR /home/nobody

# The application's jar file
COPY target/dependency-jars /home/nobody/dependency-jars

# Add the application's jar to the container
ADD target/application.jar /home/nobody/application.jar

RUN chown -R nobody:nogroup /home/nobody
RUN chown -R nobody:nogroup /var/log/ecc

USER 65534

# Run the jar file
ENTRYPOINT java -jar /home/nobody/application.jar

#Healthy Status
HEALTHCHECK --interval=5s --retries=3 --timeout=10s \

CMD wget -O /dev/null http://localhost:8081/about/version  || exit 1
