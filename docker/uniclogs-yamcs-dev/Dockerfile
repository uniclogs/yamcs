FROM maven:3.8-openjdk-17-slim

EXPOSE 8090
EXPOSE 10015
EXPOSE 10016
EXPOSE 10020
EXPOSE 10025

RUN mkdir /yamcs
WORKDIR /yamcs

CMD ["mvn", "yamcs:run"]
