
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY build/libs/*.jar app.jar

RUN useradd -m springuser && chown -R springuser /app

USER springuser

EXPOSE 8080

ENTRYPOINT ["java", "-Xms128m", "-Xmx384m", "-jar", "app.jar"]
