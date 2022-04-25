FROM maven:3.8-openjdk-17-slim

EXPOSE 8090
EXPOSE 10015
EXPOSE 10016
EXPOSE 10020
EXPOSE 10025

RUN mkdir /yamcs
WORKDIR /yamcs

COPY pom.xml /yamcs
COPY src /yamcs/src

RUN mvn package

RUN mkdir /etc/yamcs
RUN mkdir -p /opt/yamcs/bin
RUN mkdir -p /opt/yamcs/lib
RUN mkdir -p /opt/yamcs/mdb
RUN mkdir -p /var/log/yamcs

RUN cp -r /yamcs/target/bundle-tmp/etc/* /etc/yamcs
RUN cp -r /yamcs/target/bundle-tmp/bin/* /opt/yamcs/bin
RUN cp -r /yamcs/target/bundle-tmp/lib/* /opt/yamcs/lib
RUN cp -r /yamcs/target/bundle-tmp/mdb/* /opt/yamcs/mdb

CMD ["/opt/yamcs/bin/yamcsd", "--etc-dir", "/etc/yamcs", "--data-dir", "/srv/yamcs", "--log", "3", "--log-config", "/etc/yamcs/logging.properties"]
