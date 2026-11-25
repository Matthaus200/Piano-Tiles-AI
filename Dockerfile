# Usado para publicar o sistema no Render (ou qualquer hospedagem com Docker).
# A chave NÃO entra na imagem: ela é configurada como variável de ambiente OPENROUTER_API_KEY na hospedagem.

# 1) Compila o projeto
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw -q -B package -DskipTests

# 2) Roda só o .jar (imagem menor)
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
