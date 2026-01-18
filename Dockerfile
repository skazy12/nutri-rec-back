# Dockerfile (runtime-only)
# Requiere que ya exista el jar en: target/*.jar

FROM eclipse-temurin:17-jre

WORKDIR /app

# Copiamos el jar ya construido localmente
COPY target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]