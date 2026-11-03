#!/bin/bash
# SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
# SPDX-License-Identifier: EUPL-1.2

# Carica variabili d'ambiente da .env
export $(grep -v '^#' .env | xargs)

# Carica JAVA_HOME da buildfiles/local.properties
source ./set-java-home.sh

# Build accrualcommonsrv prima di accrualreportsbapi
echo "Building accrualcommonsrv..."
cd ../accrualcommonsrv
./exec-build.sh
if [ $? -ne 0 ]; then
    echo "Error building accrualcommonsrv. Exiting."
    exit 1
fi

cd -

# Avvia accrualreportsbapi
#./mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=dev"
./mvn -DskipTests spring-boot:run -Dspring-boot.run.profiles=local -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5008"
