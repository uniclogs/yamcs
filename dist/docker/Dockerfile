FROM openjdk:20-ea-17-jdk-bullseye

# Image Args
ENV USER "yamcs"

ARG YAMCS_APP
ENV YAMCS_APP "/opt/yamcs"

ARG YAMCS_DATA
ENV YAMCS_DATA "/srv/yamcs"

ARG YAMCS_CONFIGS
ENV YAMCS_CONFIGS "/etc/yamcs"

ARG YAMCS_LOGS
ENV YAMCS_LOGS "/var/log/yamcs"

# POSIX Ports
EXPOSE 8090
EXPOSE 10010
EXPOSE 10015
EXPOSE 10016
EXPOSE 10020
EXPOSE 10025
EXPOSE 10026

# Install dependencies
RUN apt-get -y update \
    && apt-get -y install \
    opensysusers \
    vim \
    tcpdump \
    && rm -rf /var/lib/apt/lists/*

# Create the system user/group
ADD --chown=root:root ./yamcs.conf /usr/lib/sysusers.d
RUN systemd-sysusers

# Create relevant directories
RUN mkdir -p ${YAMCS_APP} ${YAMCS_DATA} ${YAMCS_CONFIGS} ${YAMCS_LOGS}

# Copy in all of the relevant files
ADD --chown=${USER}:${USER} ./yamcs/bin ${YAMCS_APP}/bin
ADD --chown=${USER}:${USER} ./yamcs/lib ${YAMCS_APP}/lib
ADD --chown=${USER}:${USER} ./yamcs/mdb ${YAMCS_APP}/mdb
ADD --chown=root:root ./yamcs/etc ${YAMCS_CONFIGS}
RUN mv /etc/yamcs/prod/* /etc/yamcs && rmdir /etc/yamcs/prod
RUN ln -s ${YAMCS_APP}/bin/yamcsd /usr/bin && \
    ln -s ${YAMCS_APP}/bin/yamcsadmin /usr/bin

# Fix Ownership Permissions
RUN chown -R ${USER}:${USER} ${YAMCS_APP}
RUN chown -R ${USER}:${USER} ${YAMCS_DATA}
RUN chown -R ${USER}:${USER} ${YAMCS_LOGS}

# Fix Access Permissions
RUN chmod -R 755 ${YAMCS_APP}/bin
RUN chmod -R 644 ${YAMCS_CONFIGS} ${YAMCS_LOGS}
RUN chmod 755 ${YAMCS_CONFIGS} ${YAMCS_LOGS}

# Generate HMAC key and set it as an ARG variable
USER root
RUN apt-get update && apt-get install -y openssl
RUN openssl rand -hex 32 > /tmp/hmac_key.txt
RUN chown ${USER}:${USER} /tmp/hmac_key.txt
ARG HMAC_KEY
RUN HMAC_KEY=$(cat /tmp/hmac_key.txt) && echo "HMAC_KEY=${HMAC_KEY}" > /tmp/hmac_key.env
RUN rm /tmp/hmac_key.txt

# Set the HMAC_KEY environment variable
ENV HMAC_KEY=$HMAC_KEY

# Entry Point
USER ${USER}
ENTRYPOINT /usr/bin/yamcsd --data-dir ${YAMCS_DATA} --etc-dir ${YAMCS_CONFIGS} --log-config ${YAMCS_CONFIGS}/logging.properties --log 3