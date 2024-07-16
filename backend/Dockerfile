FROM maven:3.9.8-eclipse-temurin-21-alpine

WORKDIR /app

# Copy the pom.xml and application code
COPY pom.xml .

# Download all dependencies, including plugins, and prepare for offline use
RUN mvn dependency:go-offline -B

# Copy the application source code
COPY src ./src

# Verify the project to ensure all dependencies are downloaded
RUN mvn verify -DskipTests -B

# Perform a clean build of the project offline
RUN mvn clean install -U -DskipTests --offline

# Command to run the Spring Boot application using Maven
CMD ["mvn", "spring-boot:run", "--offline"]
