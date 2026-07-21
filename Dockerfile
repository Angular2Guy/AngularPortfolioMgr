FROM eclipse-temurin:25-jdk-alpine
VOLUME /tmp
RUN sh -c 'touch /app.jar'
ARG JAR_FILE
ADD backend/build/libs/${JAR_FILE} /app.jar
# params for 2GB of container memory limit
# ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=65 -XX:MaxGCPauseMillis=50 -XX:+UseStringDeduplication -XX:MaxDirectMemorySize=64m -XX:+ExitOnOutOfMemoryError"
# params for 1GB of container memory limit
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=50 -XX:MaxMetaspaceSize=128m -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:+UseStringDeduplication -XX:MaxDirectMemorySize=64m -XX:+ExitOnOutOfMemoryError"
ENTRYPOINT exec java $JAVA_OPTS -Dliquibase.duplicateFileMode=WARN -Djava.security.egd=file:/dev/./urandom -jar /app.jar