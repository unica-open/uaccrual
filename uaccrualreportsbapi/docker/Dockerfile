# SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
# SPDX-License-Identifier: EUPL-1.2

FROM docker-trusted.ecosis.csi.it/eclipse-temurin:17.0.8.1_1-jdk
LABEL it.csi.codice_progetto="accrual" \
      it.csi.codice_componente="accrualreportsbapi"
RUN mkdir /deployments \
    && chown 1001 /deployments \
    && chmod "g+rwX" /deployments \
    && chown 1001:root /deployments
RUN mkdir /logs \
    && chown 1001 /logs \
    && chmod "g+rwX" /logs \
    && chown 1001:root /logs
COPY  ./docker/run-java.sh /deployments/run-java.sh

RUN chown 1001 /deployments/run-java.sh \
    && chmod 540 /deployments/run-java.sh

WORKDIR /deployments
EXPOSE 10180
USER 1001

ENTRYPOINT ["/deployments/run-java.sh"]
