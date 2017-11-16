FROM docker.ci.liaison.com/alloy/jre:1.6.0

ARG APPLICATION_ID=akka-nucleus
ENV APPLICATION_ID $APPLICATION_ID
ENV JVM_MEMORY="-Xms1024m -Xmx1024m"
ENV JVM_GARBAGE="-XX:+UseG1GC"

EXPOSE 2552
EXPOSE 8989

ADD service-bootstrap/build/distributions/$APPLICATION_ID.tar /opt/liaison/

ENTRYPOINT ./opt/liaison/$APPLICATION_ID/bin/service-bootstrap
