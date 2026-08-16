# --- Etapa 1: compilar el proyecto con Maven ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cachear las dependencias en una capa separada (solo se re-descargan si pom.xml cambia)
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Etapa 2: imagen final, liviana, solo con el JRE ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Railway (y la mayoría de los hosting) inyectan la variable PORT en runtime;
# application.yml ya la usa via ${PORT:8080}.
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
