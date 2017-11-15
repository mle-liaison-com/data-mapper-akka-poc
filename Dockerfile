FROM docker.ci.liaison.com/alloy/jre:1.6.0

ARG APPLICATION_ID=akka-nucleus
ENV APPLICATION_ID $APPLICATION_ID

EXPOSE 2552
EXPOSE 8989

ADD service-bootstrap/build/distributions/$APPLICATION_ID.tar /opt/liaison/

ENTRYPOINT ["/opt/liaison/akka-nucleus/bin/service-bootstrap"]
