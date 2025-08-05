# Use a lightweight base image with Java
FROM openjdk:17-jdk-slim

# Create a directory for the app
WORKDIR /app

# Copy the JAR file into the container
COPY target/QR-Code-Apps-0.0.1-SNAPSHOT.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java","-jar","/app/app.jar"]
