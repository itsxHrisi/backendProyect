# Usa una imagen base de Java
FROM openjdk:17-jdk-slim

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo .jar generado en la construcción de la aplicación
COPY target/proyectefinal-0.0.1-SNAPSHOT.war proyectefinal.war

# Expone el puerto en el que se ejecutará la aplicación
EXPOSE 8091

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "proyectefinal.war"]