# ---------------- STAGE 1: BUILD ----------------
FROM gradle:8-jdk17 AS build

USER root
RUN rm -rf /home/gradle/.gradle && mkdir -p /home/gradle/.gradle && chown -R gradle:gradle /home/gradle

USER gradle
WORKDIR /app

COPY build.gradle settings.gradle ./
RUN gradle --no-daemon help || true

COPY src ./src

RUN gradle --stacktrace --no-daemon clean build -x test

# ---------------- STAGE 2: RUNTIME ----------------
FROM eclipse-temurin:17-jdk
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
