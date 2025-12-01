FROM eclipse-temurin:21-jdk
# Create and change to the app directory.
WORKDIR /app

# Copy local code to the container image.
COPY . ./
RUN ./gradlew build
CMD ["sh", "-c", "java -jar build/libs/dune-backend-1.0-SNAPSHOT.jar"]
