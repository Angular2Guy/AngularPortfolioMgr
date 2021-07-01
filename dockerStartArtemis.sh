#!/bin/sh
docker run -it -p 61616:61616 -p 8161:8161 -v <broker folder on host>:/var/lib/artemis-instance --name activemqartemis --memory="1g" --network="host" angular2guy/activemqartemis
#docker start activemqartemis
#docker stop activemqartemis