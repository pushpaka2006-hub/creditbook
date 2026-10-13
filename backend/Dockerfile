
# Stage 1: Build the Spring Boot application
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy Maven configuration
COPY pom.xml .

# Copy Java source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the generated JAR file
COPY --from=build /app/target/*.jar app.jar

# Expose the default application port
EXPOSE 8080

# Start Spring Boot using Render's port
CMD ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]