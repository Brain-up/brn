FROM gradle:jdk8

WORKDIR /brn
ADD . /brn

RUN gradle clean bootJar --no-daemon
