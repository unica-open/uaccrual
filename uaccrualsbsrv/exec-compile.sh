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
./mvn compile
