# ===========================================
# Multi-stage build for optimized image size
# ===========================================

# ---------- Stage 1: Build the application ----------
FROM maven:3.9.11-eclipse-temurin-17-alpine AS build

# Set working directory
WORKDIR /app

# Copy Maven wrapper and configuration
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .

# Download dependencies first (layer caching)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy application source code
COPY src ./src

# Build the Spring Boot JAR (skip tests for faster build)
RUN ./mvnw clean package -DskipTests

# ---------- Stage 2: Create the runtime image ----------
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose the application port (as per application.properties)
EXPOSE 9090

# Set JVM options for containerized environments
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Optional health check — update endpoint if needed
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:9090/actuator/health || exit 1

# Run the Spring Boot application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
