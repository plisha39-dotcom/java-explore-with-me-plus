
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY . .

RUN mvn -B -pl stats/stats-server -am clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/stats/stats-server/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
