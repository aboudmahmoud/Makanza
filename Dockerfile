FROM gradle:8.10-jdk21 AS build
WORKDIR /app
COPY . .
RUN ./gradlew :server:installDist --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/server/build/install/server /app
EXPOSE 8080
CMD ["/app/bin/server"]
