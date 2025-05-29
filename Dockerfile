# Etapa 1: Construcción con Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copia pom.xml y descarga dependencias
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia el resto del proyecto
COPY . .

# Compila el proyecto (genera un .jar en /app/target)
RUN mvn clean package -DskipTests

RUN ls -l /app/target

# Etapa 2: Imagen final más liviana
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copia el JAR compilado
COPY --from=build /app/target/*.war app.war

# Expone el puerto por defecto de Spring Boot
EXPOSE 8091

# Ejecuta la app
ENTRYPOINT ["java", "-jar", "app.war"]

