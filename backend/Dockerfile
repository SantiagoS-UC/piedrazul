# Imagen de producción del backend. Se construye en dos etapas para que la imagen final solo
# lleve el JRE y el jar, sin Maven ni el código fuente.

FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
# Las dependencias se descargan en una capa aparte: si solo cambia el código, Docker reutiliza esa capa.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B dependency:go-offline
COPY src/ src/
# Las pruebas ya corrieron en el pipeline de integración continua.
RUN ./mvnw -B package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 piedrazul
COPY --from=build /app/target/*.jar app.jar
USER piedrazul
# Ajustado al plan gratuito de Render (512 MB): el heap usa como máximo el 75 % de la memoria.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -Xss512k"
EXPOSE 8080
ENV PORT=8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
