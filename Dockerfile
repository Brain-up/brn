FROM gradle:jdk17 AS builder

WORKDIR /brn
ADD . /brn

RUN ./gradlew clean bootJar --no-daemon

FROM openjdk:17-ea-22
WORKDIR /brn
COPY --from=builder /brn/build/libs/epam-brn.jar /brn/