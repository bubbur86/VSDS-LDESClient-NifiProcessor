# syntax=docker/dockerfile:1

#
# INSTALL nifi
#
ARG NIFI_STAGE_MAVEN
ARG NIFI_STAGE_NIFI
ARG NIFI_BUILD_ARGS
ARG NIFI_CONF_DIR

FROM ${NIFI_STAGE_MAVEN:-maven:3.8.6-openjdk-18} AS builder
FROM ${NIFI_STAGE_NIFI:-apache/nifi:1.17.0} as nifi

LABEL maintainer="Agentschap Digitaal Vlaanderen <digitaal.vlaanderen@vlaanderen.be>"
LABEL site="https://github.com/Informatievlaanderen?q=vsds"

# CLONE REPO
FROM builder as clone-stage
RUN git clone https://github.com/Informatievlaanderen/VSDS-LDESClient-NifiProcessor.git ldes-client-bundle
WORKDIR /ldes-client-bundle

# BUILD DELIVERABLES
FROM builder as build-stage
ARG NIFI_BUILD_ARGS
WORKDIR /build
COPY --from=clone-stage ldes-client-bundle/ .
RUN mvn install assembly:single ${NIFI_BUILD_ARGS}

# NIFI: setup nar
FROM nifi as configure-stage
WORKDIR /opt/nifi/nifi-current
COPY --from=build-stage /build/ldes-client-wrappers/ldes-client-wrappers-nifi/target/*.nar ./lib/

# NIFI: configure
ARG NIFI_CONF_DIR
COPY ${NIFI_CONF_DIR}/ /opt/nifi/nifi-current/conf_template/

