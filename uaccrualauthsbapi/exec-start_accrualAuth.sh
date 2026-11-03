#!/bin/bash
# SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
# SPDX-License-Identifier: EUPL-1.2

# Carica JAVA_HOME da buildfiles/local.properties
source ./set-java-home.sh

#./mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=dev"
export $(grep -v '^#' .env | xargs)
./mvn spring-boot:run -Dspring-boot.run.profiles=local -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
