#!/bin/bash
# SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
# SPDX-License-Identifier: EUPL-1.2

# Carica variabili d'ambiente da .env
export $(grep -v '^#' .env | xargs)

# Salva la directory corrente
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

# Carica JAVA_HOME da buildfiles/local.properties
source ./set-java-home.sh

# Build accrualcommonsrv prima di accrualsbsrv
echo "Building accrualcommonsrv..."
cd ../accrualcommonsrv
bash ./exec-build.sh
if [ $? -ne 0 ]; then
    echo "Error building accrualcommonsrv. Exiting."
    exit 1
fi

# Torna alla directory del progetto accrualsbsrv
cd "$SCRIPT_DIR"
bash ./mvn spring-boot:run -DskipTests -Dspring-boot.run.profiles=local -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006"
