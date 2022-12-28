FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
RUN sh -c 'touch /app.jar'
ARG JAR_FILE
ADD backend/build/libs/${JAR_FILE} /app.jar
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+UseStringDeduplication"
ENTRYPOINT exec java $JAVA_OPTS -Dliquibase.duplicateFileMode=WARN -Djava.security.egd=file:/dev/./urandom -jar /app.jar