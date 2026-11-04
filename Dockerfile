FROM eclipse-temurin:17-jre-jammy

ARG JAR_FILE=target/oracle-suspiciousmembers-service-0.0.1-SNAPSHOT
ARG MODULE=loracle-suspiciousmembers-service

ENV APP_HOME=/app \
    JAVA_OPTS=""

WORKDIR ${APP_HOME}
COPY ${JAR_FILE} app.jar

RUN mkdir -p /app/config /app/Archive
COPY src/main/resources/application.properties /app/config/application.properties
ENV SPRING_CONFIG_ADDITIONAL_LOCATION=file:/app/config/application.properties

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
