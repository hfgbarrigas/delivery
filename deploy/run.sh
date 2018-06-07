#!/bin/sh

#we can use environment variables derived from several places
#1)other scripts that run before this one
#2)kubernetes configMaps
#3)Docker run X X X

SERVICE_NAME="${SERVICE_NAME:-service}"
ENVIRONMENT="${ENVIRONMENT:-default}"
XMX="${XMX:-450M}"
XMS="${XMS:-350M}"
NETWORK_CACHE_TTL="${NETWORK_CACHE_TTL:-60}"

echo "Using: ENVIRONMENT = ${ENVIRONMENT}, XMX=${XMX}, XMS=${XMS}, NETWORK_CACHE_TTL=${NETWORK_CACHE_TTL}, to run ${SERVICE_NAME}.jar"

exec java \
-Xmx${XMX} -Xms${XMS} \
-jar \
-Dnetworkaddress.cache.ttl=$NETWORK_CACHE_TTL \
-Dspring.profiles.active=$ENVIRONMENT \
-Dspring.config.location=/usr/opt/service/.config/application-$ENVIRONMENT.properties \
-Denvironment=$ENVIRONMENT \
/usr/opt/service/${SERVICE_NAME}.jar
