# Stage 1: Build the Spring Boot application using Java 21
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy Maven wrapper and dependencies configuration
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
# Strip Windows CRLF line endings from mvnw if committed on Windows
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copy source and package the production JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Lightweight runtime image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/target/trading-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 5454

ENTRYPOINT ["java", "-Dserver.port=5454", "-jar", "app.jar"]
