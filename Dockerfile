# ---------- Estágio 1: compila (imagem grande, só existe durante o build) ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Copia só o pom primeiro: o Docker reaproveita o cache das dependências enquanto o pom não mudar.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Estágio 2: executa (imagem pequena, só JRE) ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Usuário sem privilégios: boa prática de segurança.
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/*.jar app.jar
USER app
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
