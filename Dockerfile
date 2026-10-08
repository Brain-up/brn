FROM gradle:jdk17 AS builder

WORKDIR /brn
ADD . /brn

RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /brn
COPY --from=builder /brn/build/libs/epam-brn.jar /brn/