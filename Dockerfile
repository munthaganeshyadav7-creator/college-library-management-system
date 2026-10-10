FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and source from backend directory
COPY backend/pom.xml ./pom.xml
RUN mvn dependency:go-offline -B

COPY backend/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENV PORT=8085
EXPOSE 8085

CMD ["sh", "-c", "java -Dserver.port=${PORT:-8085} -jar app.jar"]
