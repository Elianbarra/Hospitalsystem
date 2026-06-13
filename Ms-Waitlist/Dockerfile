# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk-noble AS builder
WORKDIR /app

COPY gradle/ gradle/
COPY gradlew settings.gradle.kts build.gradle.kts ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon -q

COPY src ./src
RUN ./gradlew clean bootJar --no-daemon -x test

# Selecciona el fat JAR (excluye el plain JAR que Gradle también genera)
RUN find /app/build/libs -name "*.jar" ! -name "*plain*" -exec cp {} /app/application.jar \;

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-noble AS runtime
WORKDIR /app

COPY --from=builder /app/application.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
