# ----------------------------------------------------------------------------
# (C) Copyright IBM Corp. 2016, 2022
#
# SPDX-License-Identifier: Apache-2.0
# ----------------------------------------------------------------------------
# Stage: Base

FROM openliberty/open-liberty:22.0.0.10-kernel-slim-java11-openj9-ubi as base

USER root
RUN yum install -y unzip
RUN install -d -o 1001 /opt/fhir-server
USER 1001

COPY target/fhir-server-distribution.zip /tmp/
RUN unzip -qq /tmp/fhir-server-distribution.zip -d /tmp && \
    /tmp/fhir-server-dist/install.sh /opt/ol && \
    mv /tmp/fhir-server-dist/tools /opt/fhir-server/tools
COPY src/main/docker/fhir-server/bootstrap.properties /opt/ol/wlp/usr/servers/defaultServer/
COPY src/main/docker/fhir-server/bootstrap.sh /opt/fhir-server/
# ----------------------------------------------------------------------------
# Stage: Runnable

FROM openliberty/open-liberty:22.0.0.10-kernel-slim-java11-openj9-ubi

ARG VERBOSE=true
ARG FHIR_SERVER_VERSION=5.0.0-SNAPSHOT

# The following labels are required:
LABEL name='LinuxForHealth FHIR Server'
LABEL version="$FHIR_SERVER_VERSION"
LABEL summary="LinuxForHealth FHIR Server with OpenJ9 and UBI 8"
LABEL description="The LinuxForHealth FHIR Server is a modular Java implementation of the HL7 FHIR specification that supports versions R4 and R4B with a focus on performance and configurability."

ENV FHIR_CONFIG_HOME=/opt/ol/wlp/usr/servers/defaultServer \
    WLP_LOGGING_CONSOLE_SOURCE=message,trace,accessLog,ffdc,audit \
    WLP_LOGGING_CONSOLE_LOGLEVEL=info \
    WLP_LOGGING_CONSOLE_FORMAT=SIMPLE \
    WLP_LOGGING_MESSAGE_SOURCE="" \
    WLP_LOGGING_MESSAGE_FORMAT=JSON \
    TRACE_FILE=stdout \
    TRACE_FORMAT=BASIC

COPY target/LICENSE /licenses/

COPY --chown=1001:0 --from=base /opt/ol/wlp/usr/servers/defaultServer/server.xml /opt/ol/wlp/usr/servers/defaultServer/
COPY --chown=1001:0 --from=base /opt/ol/wlp/usr/servers/defaultServer/configDropins /opt/ol/wlp/usr/servers/defaultServer/configDropins

RUN features.sh

COPY --chown=1001:0 --from=base /opt/ol/wlp/usr /opt/ol/wlp/usr

RUN configure.sh && \
    mkdir -p /output/bulkdata

COPY --chown=1001:0 --from=base /opt/fhir-server /opt/fhir-server

RUN mkdir -p /config/configDropins/overrides && \
    chmod -R 775 /config/configDropins/overrides && \
    chmod -R 775 /opt/ol/wlp/usr/servers/defaultServer/configDropins/defaults

# This block ensures the latest software is picked up.
USER root
RUN yum update -y && \
    yum clean all && \
    rm -rf /var/cache/yum
USER 1001

# Set the working directory to the liberty defaultServer
WORKDIR ${FHIR_CONFIG_HOME}

ENTRYPOINT ["/opt/fhir-server/bootstrap.sh"]
CMD ["/opt/ol/wlp/bin/server", "run", "defaultServer"]