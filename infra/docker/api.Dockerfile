# Build context: repository root (see infra/compose.yaml).
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY apps/api/gradlew apps/api/settings.gradle apps/api/build.gradle ./
COPY apps/api/gradle ./gradle
RUN ./gradlew --no-daemon dependencies > /dev/null
COPY apps/api/src ./src
RUN ./gradlew --no-daemon bootJar -x test

FROM eclipse-temurin:21-jre
RUN useradd --system --uid 10001 gymflow
WORKDIR /app
COPY --from=build /workspace/build/libs/gymflow-api-*.jar app.jar
USER gymflow
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
