#!/bin/sh
#./gradlew build -PwithAngular=true -PuseChromium=true
./gradlew build -PwithAngular=true
docker build -t angular2guy/angularportfoliomgr:latest --build-arg JAR_FILE=build/libs/portfoliomgr.jar --no-cache .
docker run -p 8080:8080 --network="host" angular2guy/angularportfoliomgr:latest