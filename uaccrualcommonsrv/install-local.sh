#!/bin/bash
# SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
# SPDX-License-Identifier: EUPL-1.2

# Script per installare accrualcommonsrv nel repository Maven locale
# Da usare dopo la build per rendere disponibile la dipendenza

set -e  # Esce immediatamente in caso di errore

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VERSION=$("$SCRIPT_DIR/get_version.sh")
JAR="$SCRIPT_DIR/target/accrualcommonsrv-${VERSION}.jar"

[ ! -f "$JAR" ] && echo "Jar $JAR non trovato: eseguire prima la build" && exit 1

echo "========================================="
echo "INSTALL ACCRUALCOMMONSRV"
echo "========================================="

# Installa nel repository Maven locale
echo ""
echo "Installing accrualcommonsrv-${VERSION}.jar into local Maven repository..."
cd "$SCRIPT_DIR"
mvn install:install-file \
    -Dfile="$JAR" \
    -DgroupId=it.csi.accrual \
    -DartifactId=accrualcommonsrv \
    -Dversion=${VERSION} \
    -Dpackaging=jar

echo ""
echo "========================================="
echo "INSTALL COMPLETATO!"
echo "========================================="
