#!/bin/bash
# SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
# SPDX-License-Identifier: EUPL-1.2

source ./set-java-home.sh

VERSION=$(./get_version.sh) || exit 1

# Verifica se serve ricompilare (controlla file modificati dopo il JAR build)
JAR_FILE="target/accrualcommonsrv-${VERSION}.jar"

NEEDS_BUILD=false

if [ ! -f "$JAR_FILE" ]; then
    echo "JAR build non trovato, build necessaria"
    NEEDS_BUILD=true
else
    # Cerca file .java o pom.xml modificati dopo il JAR
    CHANGED_FILES=$(find src/main -name "*.java" -newer "$JAR_FILE" -o -name "pom.xml" -newer "$JAR_FILE" 2>/dev/null)
    if [ -n "$CHANGED_FILES" ]; then
        echo "Rilevati file modificati:"
        echo "$CHANGED_FILES"
        NEEDS_BUILD=true
    else
        echo "accrualcommonsrv: nessun file modificato (skip build)"
    fi
fi

if [ "$NEEDS_BUILD" = true ]; then
    ./mvn clean install && ./install-local.sh
fi
