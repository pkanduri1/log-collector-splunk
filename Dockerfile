# Use a multi-stage build to keep the image small
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
# Run stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/log-collector-0.0.1-SNAPSHOT.jar app.jar

# Copy Splunk certificate and import it into Java truststore
COPY splunk.crt /tmp/splunk.crt
RUN keytool -import -trustcacerts -noprompt \
    -alias splunk-server \
    -file /tmp/splunk.crt \
    -keystore $JAVA_HOME/lib/security/cacerts \
    -storepass changeit && \
    rm /tmp/splunk.crt

EXPOSE 8080
ENTRYPOINT ["java", \
    "-Djdk.internal.httpclient.disableHostnameVerification=true", \
    "-Djavax.net.ssl.trustAll=true", \
    "-Dcom.sun.net.ssl.checkRevocation=false", \
    "-jar", "app.jar"]

