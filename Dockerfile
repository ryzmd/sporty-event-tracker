# -----------------------------------------------------------------------------
# Stage 1: Build the Application
# -----------------------------------------------------------------------------
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# 1. Copy dependency definitions first to leverage Docker layer caching
#    (If src changes but dependencies don't, we skip re-downloading jars)
COPY build.gradle settings.gradle ./

# 2. Copy source code
COPY src ./src

# 3. Build the JAR (Skipping tests to speed up container build time)
RUN gradle bootJar --no-daemon -x test

# -----------------------------------------------------------------------------
# Stage 2: Create the Runtime Image
# -----------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 1. Security: Create a non-root user group and user
RUN addgroup -S spring && adduser -S spring -G spring

# 2. Copy the built artifact from the 'build' stage
#    We use a wildcard (*.jar) so we don't have to hardcode version numbers
COPY --from=build /app/build/libs/*.jar app.jar

# 3. Security: Change ownership of the app to the non-root user
RUN chown spring:spring app.jar

# 4. Switch to non-root user
USER spring:spring

# 5. Expose the application port
EXPOSE 8080

# 6. Healthcheck
#    Used by Docker Compose to know when the service is actually ready
HEALTHCHECK --interval=30s --timeout=3s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 7. Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]