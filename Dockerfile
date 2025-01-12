FROM eclipse-temurin:21.0.5_11-jdk-ubi9-minimal
WORKDIR /cont
COPY ./pom.xml /cont
COPY ./src /cont/src
ADD ./target/gateway-microservice-0.0.1-SNAPSHOT.jar /gateway.jar
EXPOSE 9001
ENTRYPOINT ["java", "-jar", "/gateway.jar"]