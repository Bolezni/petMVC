# ---------- build ----------
FROM eclipse-temurin:21-jdk-alpine AS build
RUN apk add --no-cache bash unzip curl
WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies --quiet || true

COPY src ./src
RUN ./gradlew --no-daemon bootJar -x test

# ---------- runtime ----------
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl tzdata \
    && addgroup -S app \
    && adduser -S -G app app

WORKDIR /app
COPY --from=build /app/build/libs/*.jar /app/app.jar
RUN mkdir -p /data/uploads && chown -R app:app /data /app

USER app
EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]