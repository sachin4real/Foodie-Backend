# Start from official Java image
FROM openjdk:23-jdk-slim

# Set working directory inside container
WORKDIR /app

# Copy the JAR file (you’ll build this using mvn package)
COPY target/orderpayment-0.0.1-SNAPSHOT.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
