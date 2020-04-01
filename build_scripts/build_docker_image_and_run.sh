#!/bin/bash
docker build -t brn .

docker run --name brn -p 8081:8081 brn