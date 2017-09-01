FROM docker.ci.liaison.com/alloy/jre:1.2.0-0-772edc8123

ENV APPLICATION_ID g2-akka-nucleus

EXPOSE 8989

RUN mkdir /app
WORKDIR /app

# TODO publish & fetch using nexus ?
COPY service-bootstrap/build/distributions/$APPLICATION_ID.tar /app/
RUN tar -xvf /app/$APPLICATION_ID.tar

WORKDIR $APPLICATION_ID/bin

CMD ./service-bootstrap
