FROM maven:3.6.0-jdk-11-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

# Definir a imagem base do Docker
FROM openjdk:17-oracle

# Definir o diretório de trabalho dentro do contêiner
WORKDIR /app
VOLUME /tmp
EXPOSE 8080
RUN mkdir -p /app/
RUN mkdir -p /app/logs/
ADD target/via-cep-brq-0.0.1-SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=container", "-jar", "/app/app.jar"]

# Copiar o arquivo JAR da aplicação para o diretório de trabalho
#COPY target/via-cep-brq-0.0.1-SNAPSHOT.jar /app/app.jar

# Expor a porta na qual a aplicação vai ser executada
#EXPOSE 8080

# Comando para executar a aplicação quando o contêiner iniciar
#CMD ["java", "-jar", "app.jar"]
