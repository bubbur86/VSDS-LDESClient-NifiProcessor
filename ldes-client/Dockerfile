# syntax=docker/dockerfile:1

FROM maven:3.8.5-openjdk-18 AS builder
ARG VERSION_DEFAULT=1.0-SNAPSHOT
ENV VERSION=${VERSION_DEFAULT}
ARG UID_DEFAULT=1000
ENV UID=${UID_DEFAULT}
ARG GID_DEFAULT=1000
ENV GID=${GID_DEFAULT}
ARG HOME_DIR_DEFAULT=/opt/ldesclient
ENV HOME_DIR=${HOME_DIR_DEFAULT}

LABEL maintainer="Agentschap Digitaal Vlaanderen <digitaal.vlaanderen@vlaanderen.be>"
LABEL site="https://github.com/Informatievlaanderen?q=vsds"

# CLONE REPO
FROM builder as clone-stage
RUN git clone https://github.com/Informatievlaanderen/VSDS-LDESClient-NifiProcessor.git ldes-client-bundle
WORKDIR /ldes-client-bundle
RUN git switch feature/VSDSPUB-230-client-cli

# BUILD DELIVERABLES
FROM builder as build-stage
WORKDIR /build
COPY --from=clone-stage ldes-client-bundle/ .
RUN mvn install assembly:single

# SETUP CLIENT CLI DOCKER
FROM builder as deploy-stage
ARG GID
ARG UID
ARG HOME_DIR
ARG VERSION
WORKDIR ${HOME_DIR}
ADD ./scripts/ldesclient.sh ${HOME_DIR}
COPY --from=build-stage /build/ldes-client/target/ldes-client-${VERSION}-jar-with-dependencies.jar ${HOME_DIR}/ldes-client.jar
# SETUP USER
RUN groupadd -g ${GID} ldesclient || groupmod -n ldesclient `getent group ${GID} | cut -d: -f1`
RUN useradd --shell /bin/bash -u ${UID} -g ${GID} -d ${HOME_DIR} -m ldesclient
RUN mkdir -p ${HOME_DIR}/
RUN chown -R ldesclient:ldesclient ${HOME_DIR}
USER ldesclient

ENTRYPOINT ["/opt/ldesclient/ldesclient.sh"]
