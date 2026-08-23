# Backend Dockerfile for Kotlin/Ktor multi-service application
# Multi-stage build for optimized image size

# Stage 1: Build
FROM gradle:9.4.0-jdk21 AS builder

WORKDIR /home/gradle/project

# Copy the entire project (filtered by .dockerignore)
COPY . .

# Argument to select the backend service to build (training-service for MVP)
ARG SERVICE_NAME
ENV SERVICE_NAME=${SERVICE_NAME}

# Build the specific application
RUN gradle -p fit-bridge-be/${SERVICE_NAME} :app-ktor:shadowJar --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl

ARG SERVICE_NAME
ARG SERVICE_PORT=8080

# Cgroup & container memory awareness for JVM
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# Copy the built JAR from builder stage
COPY --from=builder /home/gradle/project/fit-bridge-be/${SERVICE_NAME}/app-ktor/build/libs/*-all.jar app.jar

# Document the port configured by the selected Ktor service.
EXPOSE ${SERVICE_PORT}

# Run the application
ENTRYPOINT ["java", "-cp", "app.jar", "io.ktor.server.tomcat.jakarta.EngineMain"]
