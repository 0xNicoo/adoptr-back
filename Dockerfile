# Java 25 matches the Gradle toolchain used by this project.
FROM eclipse-temurin:25-jdk-noble AS build
WORKDIR /workspace

COPY gradlew build.gradle settings.gradle ./
COPY gradle/ ./gradle/
RUN chmod +x gradlew
COPY src/main/ ./src/main/

# Tests require PostgreSQL and run separately, not during image construction.
RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:25-jre-noble AS runtime
WORKDIR /app

# Leave memory for metaspace, threads and native allocations in small instances.
ENV JAVA_TOOL_OPTIONS="-XX:InitialRAMPercentage=20.0 -XX:MaxRAMPercentage=60.0"
COPY --from=build --chown=10001:10001 /workspace/build/libs/*.jar /app/app.jar
USER 10001:10001

# Default local port; Render supplies PORT at runtime.
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
