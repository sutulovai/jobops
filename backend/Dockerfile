# syntax=docker/dockerfile:1
# ── Build stage ───────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy wrapper + build scripts first so the dependency layer is cached.
COPY gradlew settings.gradle.kts gradle.properties build.gradle.kts ./
COPY gradle ./gradle

# Resolve and cache all dependencies.
# --mount=type=cache persists ~/.gradle across builds on this machine,
# so Gradle and all JARs are downloaded only once — even when source changes.
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon -q || true

# Copy source and build the fat JAR.
COPY src ./src
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar --no-daemon -x test

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# curl is used by the Docker healthcheck.
RUN apk add --no-cache curl

RUN addgroup -S jobops && adduser -S jobops -G jobops
COPY --from=builder /app/build/libs/*.jar app.jar
RUN mkdir -p /var/lib/jobops/storage && chown -R jobops:jobops /var/lib/jobops
USER jobops

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
