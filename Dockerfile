# get minimal OS with oracle jdk8 installed
FROM maven:3.5.3-jdk-8-alpine

ADD deploy/run.sh run.sh
#ADD src src
#ADD pom.xml pom.xml

#compile
#RUN mvn clean install -P no-build-containers

# Create app directory on container
RUN mkdir -p /usr/opt/service

# copy app jar to container directory
COPY ./target/delivery*.jar /usr/opt/service/service.jar

# make port 8080 available
EXPOSE 8080

#execute run script
ENTRYPOINT exec sh "./run.sh"
