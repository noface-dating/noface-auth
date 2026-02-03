FROM eclipse-temurin:21-jre
LABEL authors="kyw10987"

WORKDIR /app

COPY build/libs/*.jar app.jar

# EXECUTE
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-jar","-Dserver.port=443","-Dserver.ssl.key-store=/app/duri-server.p12","-Dserver.ssl.key-store-password=Duriduri!","-Dserver.ssl.key-store-type=PKCS12","-Dserver.ssl.key-alias=duri","app.jar"]
