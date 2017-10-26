FROM docker.ci.liaison.com/alloy/jre:1.6.0

ENV APPLICATION_ID g2-akka-nucleus

EXPOSE 2552
EXPOSE 8989

ADD service-bootstrap/build/distributions/$APPLICATION_ID.tar /opt/liaison/

WORKDIR /opt/liaison/$APPLICATION_ID/bin

CMD ./service-bootstrap
