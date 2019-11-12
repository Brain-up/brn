FROM java:8

RUN mkdir /brn

WORKDIR brn

ADD . /brn
