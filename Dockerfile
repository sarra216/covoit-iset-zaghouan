# ---------- Étape 1 : compiler l'application avec Maven ----------
FROM maven:3.9-eclipse-temurin-17 AS construction
WORKDIR /app
# Les dépendances d'abord : Docker les garde en cache tant que pom.xml ne change pas
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
# Les tests tournent dans le job « test » du pipeline ; ici on ne fait que construire le .jar
RUN mvn -B -q package -DskipTests

# ---------- Étape 2 : image légère qui ne contient que Java et le .jar ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
# L'application ne tourne pas en administrateur (root) dans le conteneur :
# si quelqu'un exploite une faille, il n'a que les droits de cet utilisateur limité
RUN useradd --system --create-home covoit && mkdir -p /app/logs && chown covoit /app/logs
USER covoit
COPY --from=construction /app/target/*.jar app.jar

# Le pipeline passe le commit (--build-arg VERSION=$CI_COMMIT_SHORT_SHA) : visible dans GET /health
ARG VERSION=dev
ENV APP_VERSION=$VERSION \
    SPRING_PROFILES_ACTIVE=prod \
    TZ=Africa/Tunis
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
