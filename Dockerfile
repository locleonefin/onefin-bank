FROM java:8-jdk-alpine

COPY ./target/main.jar /usr/app/

WORKDIR /usr/app

RUN sh -c 'touch main.jar'

ENTRYPOINT ["java","-jar","main.jar"]
