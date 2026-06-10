# Backend Dockerfile for Kotlin/Ktor multi-service application
# Multi-stage build for optimized image size

# Stage 1: Build
FROM gradle:9.4.0-jdk21 AS builder

WORKDIR /home/gradle/project

# Copy the entire project (filtered by .dockerignore)
COPY . .

# Argument to select the service to build (e.g. profile-service or training-service)
ARG SERVICE_NAME
ENV SERVICE_NAME=${SERVICE_NAME}

# Build the specific application
RUN gradle :${SERVICE_NAME}:app:build --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

ARG SERVICE_NAME

# Copy the built JAR from builder stage
COPY --from=builder /home/gradle/project/fit-bridge-be/${SERVICE_NAME}/app/build/libs/*-all.jar app.jar

# Expose application port (Ktor default)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
