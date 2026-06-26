FROM eclipse-temurin:25-jre-noble
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "app.jar"]
