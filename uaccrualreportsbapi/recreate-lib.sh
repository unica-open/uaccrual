#!/bin/bash
# SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
# SPDX-License-Identifier: EUPL-1.2

# Script per ricreare la cartella lib/ copiando i JAR dal Maven locale

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LIB_DIR="$SCRIPT_DIR/lib"
MVN_REPO="$HOME/.m2/repository"

echo "========================================="
echo "RECREATE LIB DIRECTORY FROM MAVEN LOCAL"
echo "========================================="

# Crea cartella lib/
mkdir -p "$LIB_DIR"

# Copia Batik CSS
echo "Copying batik-css..."
cp "$MVN_REPO/org/apache/xmlgraphics/batik-css/1.18/batik-css-1.18.jar" \
   "$LIB_DIR/org.apache.batik.css_1.18.0.v20241009-1200.jar"

# Copia Batik I18N
echo "Copying batik-i18n..."
cp "$MVN_REPO/org/apache/xmlgraphics/batik-i18n/1.18/batik-i18n-1.18.jar" \
   "$LIB_DIR/org.apache.batik.i18n_1.18.0.v20241009-1200.jar"

# Copia Batik Util
echo "Copying batik-util..."
cp "$MVN_REPO/org/apache/xmlgraphics/batik-util/1.18/batik-util-1.18.jar" \
   "$LIB_DIR/org.apache.batik.util_1.18.0.v20241009-1200.jar"

# Copia Datatools Connectivity ODA
echo "Copying org.eclipse.datatools.connectivity.oda..."
cp "$MVN_REPO/org/eclipse/datatools/org.eclipse.datatools.connectivity.oda/3.7.0/org.eclipse.datatools.connectivity.oda-3.7.0.jar" \
   "$LIB_DIR/org.eclipse.datatools.connectivity.oda_3.7.0.202311071249.jar"

# Copia Datatools Connectivity ODA Consumer
echo "Copying org.eclipse.datatools.connectivity.oda.consumer..."
cp "$MVN_REPO/org/eclipse/datatools/org.eclipse.datatools.connectivity.oda.consumer/3.5.0/org.eclipse.datatools.connectivity.oda.consumer-3.5.0.jar" \
   "$LIB_DIR/org.eclipse.datatools.connectivity.oda.consumer_3.5.0.202311071249.jar"

# Copia Datatools Connectivity ODA Design
echo "Copying org.eclipse.datatools.connectivity.oda.design..."
cp "$MVN_REPO/org/eclipse/datatools/org.eclipse.datatools.connectivity.oda.design/3.6.0/org.eclipse.datatools.connectivity.oda.design-3.6.0.jar" \
   "$LIB_DIR/org.eclipse.datatools.connectivity.oda.design_3.6.0.202311071249.jar"

# Copia Datatools Connectivity ODA Profile
echo "Copying org.eclipse.datatools.connectivity.oda.profile..."
cp "$MVN_REPO/org/eclipse/datatools/org.eclipse.datatools.connectivity.oda.profile/3.5.0/org.eclipse.datatools.connectivity.oda.profile-3.5.0.jar" \
   "$LIB_DIR/org.eclipse.datatools.connectivity.oda.profile_3.5.0.202311071249.jar"

# Copia Datatools Connectivity
echo "Copying org.eclipse.datatools.connectivity..."
cp "$MVN_REPO/org/eclipse/datatools/org.eclipse.datatools.connectivity/1.15.0/org.eclipse.datatools.connectivity-1.15.0.jar" \
   "$LIB_DIR/org.eclipse.datatools.connectivity_1.15.0.202311071249.jar"

# Copia Eclipse Orbit XML APIs
echo "Copying eclipse-orbit-xml-apis..."
cp "$MVN_REPO/ch/reportingsoft/birt/eclipse-orbit-xml-apis/1.0.0/eclipse-orbit-xml-apis-1.0.0.jar" \
   "$LIB_DIR/org.eclipse.orbit.xml-apis-ext_1.0.0.v20240917-0534.jar"

echo ""
echo "========================================="
echo "LIB DIRECTORY RECREATED!"
echo "========================================="
echo ""
echo "Files in lib/:"
ls -1 "$LIB_DIR"
