#!/bin/bash
docker build -t brn .

docker run --name brn -p 8080:8080 brn