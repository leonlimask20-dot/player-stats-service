# Multi-stage build para gerar o JAR otimizado do Quarkus (fast-jar)
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn clean package -DskipTests

# O Quarkus gera um "fast-jar" com estrutura otimizada em target/quarkus-app/
# Inicialização mais rápida que o JAR tradicional (uber-jar)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/quarkus-app/lib/ ./lib/
COPY --from=build /app/target/quarkus-app/*.jar ./
COPY --from=build /app/target/quarkus-app/app/ ./app/
COPY --from=build /app/target/quarkus-app/quarkus/ ./quarkus/
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]
