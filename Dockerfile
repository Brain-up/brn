FROM gradle:jdk8 as builder

WORKDIR /brn
ADD . /brn

RUN gradle clean bootJar --no-daemon

FROM openjdk:8-jdk-alpine
WORKDIR /brn
COPY --from=builder /brn/build/libs/epam-brn.jar /brn/