#!/bin/bash
# SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
# SPDX-License-Identifier: EUPL-1.2


source set-java-home.sh

# Build accrualcommonsrv prima di accrualsbsrv
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
./mvn -DskipTests spring-boot:run -Dspring-boot.run.profiles=local -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5007"
