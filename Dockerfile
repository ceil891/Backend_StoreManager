# Stage 1: Build Application with memory limits for Render free tier
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Set MAVEN_OPTS to limit memory usage during build (fits within 512MB free tier)
ENV MAVEN_OPTS="-Xmx400m"

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build jar package skipping tests
RUN mvn clean package -DskipTests -Dmaven.test.skip=true

# Stage 2: Runtime Environment
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy compiled jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose Spring Boot port
EXPOSE 8080

# Run Spring Boot application with optimized JVM flags for Render 512MB free tier
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-Xms64m", "-Xmx180m", "-XX:MetaspaceSize=128m", "-XX:MaxMetaspaceSize=192m", "-XX:ReservedCodeCacheSize=36m", "-Xss256k", "-XX:+UseSerialGC", "-jar", "app.jar"]
