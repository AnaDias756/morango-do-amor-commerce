# Multi-stage build para otimizar o tamanho da imagem final

# Stage 1: Build
FROM maven:3.9.4-eclipse-temurin-17 AS build

WORKDIR /app

# Copiar arquivos de configuração do Maven
COPY pom.xml .
COPY src ./src

# Build da aplicação
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

# Instalar dependências necessárias
RUN apk add --no-cache \
    curl \
    tzdata

# Configurar timezone
ENV TZ=America/Sao_Paulo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Criar usuário não-root para segurança
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Criar diretórios necessários
RUN mkdir -p /app/logs && \
    chown -R appuser:appgroup /app

WORKDIR /app

# Copiar o JAR da aplicação do stage de build
COPY --from=build /app/target/*.jar app.jar

# Ajustar permissões
RUN chown appuser:appgroup app.jar

# Mudar para usuário não-root
USER appuser

# Configurações de JVM otimizadas
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Expor porta da aplicação
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# Comando para executar a aplicação
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Labels para metadados
LABEL maintainer="Morango do Amor <dev@morangodoamor.com.br>" \
      version="1.0.0" \
      description="Sistema de Pedidos - Morango do Amor" \
      org.opencontainers.image.source="https://github.com/morango-amor/order-system"